/*
 * Copyright (c) 2020 Adventech <info@adventech.io>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.cryart.sabbathschool.lessons.ui.readings

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableInt
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import app.ss.lessons.data.model.SSContextMenu
import app.ss.lessons.data.repository.lessons.LessonsRepository
import app.ss.models.SSLessonInfo
import app.ss.models.SSRead
import app.ss.models.SSReadComments
import app.ss.models.SSReadHighlights
import com.cryart.sabbathschool.bible.SSBibleVersesActivity
import com.cryart.sabbathschool.core.extensions.context.colorPrimary
import com.cryart.sabbathschool.core.extensions.context.colorPrimaryDark
import com.cryart.sabbathschool.core.extensions.context.isDarkTheme
import com.cryart.sabbathschool.core.extensions.coroutines.debounceUntilLast
import com.cryart.sabbathschool.core.misc.DateHelper
import com.cryart.sabbathschool.core.misc.SSConstants
import com.cryart.sabbathschool.core.misc.SSEvent
import com.cryart.sabbathschool.core.misc.SSHelper
import com.cryart.sabbathschool.core.model.SSReadingDisplayOptions
import com.cryart.sabbathschool.core.model.colorTheme
import com.cryart.sabbathschool.lessons.BuildConfig
import com.cryart.sabbathschool.lessons.R
import com.cryart.sabbathschool.lessons.databinding.SsReadingActivityBinding
import com.cryart.sabbathschool.lessons.ui.readings.options.SSReadingDisplayOptionsView
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import ru.beryukhov.reactivenetwork.ReactiveNetwork
import ru.beryukhov.reactivenetwork.internet.observing.InternetObservingSettings
import timber.log.Timber
import java.util.ArrayList

class SSReadingViewModel @AssistedInject constructor(
    private val lessonsRepository: LessonsRepository,
    @Assisted private val ssLessonIndex: String,
    @Assisted private val dataListener: DataListener,
    @Assisted private val ssReadingActivityBinding: SsReadingActivityBinding
) : SSReadingView.ContextMenuCallback, SSReadingView.HighlightsCommentsCallback, ViewModel() {

    private var ssLessonInfo: SSLessonInfo? = null
    private var ssReadIndexInt = 0
    private val ssReads: ArrayList<SSRead> = arrayListOf()
    private val ssReadHighlights: ArrayList<SSReadHighlights> = arrayListOf()
    private val ssReadComments: ArrayList<SSReadComments> = arrayListOf()
    private var ssTotalReadsCount = 0
    private var ssReadsDownloaded = false
    private var highlightId = 0
    val lessonTitle: String get() = ssLessonInfo?.lesson?.title ?: ""
    val lessonShareIndex: String get() = ssLessonInfo?.shareIndex() ?: ""
    val ssLessonLoadingVisibility = ObservableInt(View.INVISIBLE)
    val ssLessonOfflineStateVisibility = ObservableInt(View.INVISIBLE)
    val ssLessonErrorStateVisibility = ObservableInt(View.INVISIBLE)
    val ssLessonCoordinatorVisibility = ObservableInt(View.INVISIBLE)

    val primaryColor: Int
        get() = context.colorPrimary

    val secondaryColor: Int
        get() = context.colorPrimaryDark

    init {
        loadLessonInfo()
        if (!BuildConfig.DEBUG) {
            checkConnection()
        }
    }

    private val context: Context get() = ssReadingActivityBinding.root.context
    // lateinit var dataListener: DataListener
    //  lateinit var ssReadingActivityBinding: SsReadingActivityBinding

    private val verseClickWithDebounce: (verse: String) -> Unit =
        debounceUntilLast(
            scope = ViewTreeLifecycleOwner.get(ssReadingActivityBinding.root)?.lifecycleScope ?: MainScope()
        ) { verse ->
            val intent = SSBibleVersesActivity.launchIntent(
                context,
                verse,
                ssReads[ssReadingActivityBinding.ssReadingViewPager.currentItem].index
            )
            context.startActivity(intent)
        }

    private fun checkConnection() {
        val inetOptions = InternetObservingSettings.builder()
            .host(DEFAULT_PING_HOST)
            .port(DEFAULT_PING_PORT)
            .timeout(DEFAULT_PING_TIMEOUT_IN_MS)
            .initialInterval(DEFAULT_INITIAL_PING_INTERVAL_IN_MS)
            .interval(DEFAULT_PING_INTERVAL_IN_MS)
            .build()
        ReactiveNetwork().observeInternetConnectivity(inetOptions).onEach { connected ->
            if (!connected && ssLessonInfo == null) {
                try {
                    ssLessonOfflineStateVisibility.set(View.VISIBLE)
                    ssLessonErrorStateVisibility.set(View.INVISIBLE)
                    ssLessonLoadingVisibility.set(View.INVISIBLE)
                    ssLessonCoordinatorVisibility.set(View.INVISIBLE)
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }
        }.launchIn(CoroutineScope(Dispatchers.IO))
    }

    private fun loadLessonInfo() = viewModelScope.launch {
        ssLessonLoadingVisibility.set(View.VISIBLE)
        ssLessonOfflineStateVisibility.set(View.INVISIBLE)
        ssLessonErrorStateVisibility.set(View.INVISIBLE)
        ssLessonCoordinatorVisibility.set(View.INVISIBLE)

        val lessonInfoResource = lessonsRepository.getLessonInfo(ssLessonIndex)
        val lessonInfo = lessonInfoResource.data ?: run {
            ssLessonErrorStateVisibility.set(View.VISIBLE)
            ssLessonLoadingVisibility.set(View.INVISIBLE)
            ssLessonOfflineStateVisibility.set(View.INVISIBLE)
            ssLessonCoordinatorVisibility.set(View.INVISIBLE)
            return@launch
        }
        ssLessonInfo = lessonInfo
        dataListener.onLessonInfoChanged(lessonInfo)

        ssTotalReadsCount = lessonInfo.days.size

        if (lessonInfo.days.isNotEmpty()) {

            val today = DateTime.now().withTimeAtStartOfDay()
            for ((idx, ssDay) in lessonInfo.days.withIndex()) {
                val startDate = DateHelper.parseDate(ssDay.date)
                if (startDate?.isEqual(today) == true && ssReadIndexInt < 6) {
                    ssReadIndexInt = idx
                }
                downloadHighlights(ssDay.index, idx)
                downloadComments(ssDay.index, idx)

                val resource = lessonsRepository.getDayRead(dayIndex = ssDay.index)
                resource.data?.let { ssReads.add(it) }
            }

            ssReadsDownloaded = true
            dataListener.onReadsDownloaded(ssReads, ssReadHighlights, ssReadComments, ssReadIndexInt)

            try {
                ssLessonCoordinatorVisibility.set(View.VISIBLE)
                ssLessonLoadingVisibility.set(View.INVISIBLE)
                ssLessonOfflineStateVisibility.set(View.INVISIBLE)
                ssLessonErrorStateVisibility.set(View.INVISIBLE)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    private fun downloadHighlights(dayIndex: String, index: Int) = viewModelScope.launch {
        /*mDatabase.child(SSConstants.SS_FIREBASE_HIGHLIGHTS_DATABASE)
            .child(userUuid)
            .child(dayIndex)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val ssReadHighlights = dataSnapshot.getValue(SSReadHighlights::class.java) ?: SSReadHighlights(dayIndex)
                    downloadComments(dayIndex, index, ssReadHighlights)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Timber.e(databaseError.toException())
                }
            })*/
        // val resource = lessonsRepository.getReadHighlights(dayIndex)
        ssReadHighlights.add(index, SSReadHighlights(dayIndex))
    }

    private fun downloadComments(dayIndex: String, index: Int) = viewModelScope.launch {
        /*mDatabase.child(SSConstants.SS_FIREBASE_COMMENTS_DATABASE)
            .child(userUuid)
            .child(dayIndex)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val ssReadComments = SSReadComments("", emptyList())
                    downloadRead(dayIndex, index, ssReadHighlights, ssReadComments)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Timber.e(databaseError.toException())
                }
            })*/

        // val resource = lessonsRepository.getComments(dayIndex)
        ssReadComments.add(index, SSReadComments("", emptyList()))
    }

    private fun downloadRead(dayIndex: String, index: Int) = viewModelScope.launch {
        val resource = lessonsRepository.getDayRead(dayIndex = dayIndex)
        val read = resource.data ?: return@launch
        ssReads.add(read)

        /* mDatabase.child(SSConstants.SS_FIREBASE_READS_DATABASE)
             .child(dayIndex)
             .addValueEventListener(object : ValueEventListener {
                 override fun onDataChange(dataSnapshot: DataSnapshot) {
                     if (dataSnapshot.value != null) {
                         if (ssReadsLoadedCounter < ssTotalReadsCount && ssReads.size >= index) {
                             val ssRead = SSRead()
                             ssReads.add(index, ssRead)
                             ssReadHighlights.add(index, _ssReadHighlights)
                             ssReadComments.add(index, _ssReadComments)
                         }
                         ssReadsLoadedCounter++
                         if (ssReadsLoadedCounter == ssTotalReadsCount && !ssReadsDownloaded) {
                             ssReadsDownloaded = true
                             dataListener.onReadsDownloaded(ssReads, ssReadHighlights, ssReadComments, ssReadIndexInt)
                         }
                     }
                     try {
                         ssLessonCoordinatorVisibility.set(View.VISIBLE)
                         ssLessonLoadingVisibility.set(View.INVISIBLE)
                         ssLessonOfflineStateVisibility.set(View.INVISIBLE)
                         ssLessonErrorStateVisibility.set(View.INVISIBLE)
                     } catch (e: Exception) {
                         Timber.e(e)
                     }
                 }

                 override fun onCancelled(databaseError: DatabaseError) {
                     try {
                         ssLessonCoordinatorVisibility.set(View.VISIBLE)
                         ssLessonLoadingVisibility.set(View.INVISIBLE)
                         ssLessonOfflineStateVisibility.set(View.INVISIBLE)
                         ssLessonErrorStateVisibility.set(View.INVISIBLE)
                     } catch (e: Exception) {
                         Timber.e(e)
                     }
                 }
             })*/
    }

    private val currentSSReadingView: SSReadingView?
        get() {
            val view = ssReadingActivityBinding.ssReadingViewPager
                .findViewWithTag<View>("ssReadingView_" + ssReadingActivityBinding.ssReadingViewPager.currentItem)
            return view?.findViewById(R.id.ss_reading_view)
        }

    val cover: String
        get() = ssLessonInfo?.lesson?.cover ?: ""

    override fun onSelectionStarted(x: Float, y: Float, highlightId: Int) {
        onSelectionStarted(x, y)
        this.highlightId = highlightId
    }

    @SuppressWarnings("deprecation")
    override fun onSelectionStarted(posX: Float, posY: Float) {
        val scrollView: NestedScrollView = ssReadingActivityBinding.ssReadingViewPager
            .findViewWithTag("ssReadingView_" + ssReadingActivityBinding.ssReadingViewPager.currentItem)
        val y = posY - scrollView.scrollY + ssReadingActivityBinding.ssReadingViewPager.top

        val metrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.display?.getRealMetrics(metrics)
        } else {
            (context as? Activity)?.windowManager?.defaultDisplay?.getMetrics(metrics)
        }
        val params = ssReadingActivityBinding.ssContextMenu.ssReadingContextMenu.layoutParams as ViewGroup.MarginLayoutParams
        val contextMenuWidth = ssReadingActivityBinding.ssContextMenu.ssReadingContextMenu.width
        val contextMenuHeight = ssReadingActivityBinding.ssContextMenu.ssReadingContextMenu.height
        val screenWidth: Int = metrics.widthPixels
        val margin: Int = SSHelper.convertDpToPixels(context, 50)
        val jumpMargin: Int = SSHelper.convertDpToPixels(context, 60)
        var contextMenuX = posX.toInt() - contextMenuWidth / 2
        var contextMenuY = scrollView.top + y.toInt() - contextMenuHeight - margin
        if (contextMenuX - margin < 0) {
            contextMenuX = margin
        }
        if (contextMenuX + contextMenuWidth + margin > screenWidth) {
            contextMenuX = screenWidth - margin - contextMenuWidth
        }
        if (contextMenuY - margin < 0) {
            contextMenuY += contextMenuHeight + jumpMargin
        }
        params.setMargins(contextMenuX, contextMenuY, 0, 0)
        ssReadingActivityBinding.ssContextMenu.ssReadingContextMenu.layoutParams = params
        ssReadingActivityBinding.ssContextMenu.ssReadingContextMenu.visibility = View.VISIBLE
        highlightId = 0
    }

    override fun onSelectionFinished() {
        ssReadingActivityBinding.ssContextMenu.ssReadingContextMenu.visibility = View.INVISIBLE
    }

    override fun onHighlightsReceived(ssReadHighlights: SSReadHighlights) {
    }

    override fun onCommentsReceived(ssReadComments: SSReadComments) {
    }

    override fun onVerseClicked(verse: String) = verseClickWithDebounce(verse)

    interface DataListener {
        fun onLessonInfoChanged(ssLessonInfo: SSLessonInfo)
        fun onReadsDownloaded(ssReads: List<SSRead>, ssReadHighlights: List<SSReadHighlights>, ssReadComments: List<SSReadComments>, ssReadIndex: Int)
    }

    fun onDisplayOptionsClick() {
        val ssReadingDisplayOptionsView = SSReadingDisplayOptionsView()
        val fragmentManager = (context as? FragmentActivity)?.supportFragmentManager ?: return
        ssReadingDisplayOptionsView.show(fragmentManager, ssReadingDisplayOptionsView.tag)

        val index = ssReads.getOrNull(ssReadingActivityBinding.ssReadingViewPager.currentItem) ?: return
        SSEvent.track(
            context,
            SSConstants.SS_EVENT_READ_OPTIONS_OPEN,
            hashMapOf(SSConstants.SS_EVENT_PARAM_READ_INDEX to index)
        )
    }

    fun highlightYellow() {
        highlightSelection(SSContextMenu.HIGHLIGHT_YELLOW)
    }

    fun highlightOrange() {
        highlightSelection(SSContextMenu.HIGHLIGHT_ORANGE)
    }

    fun highlightGreen() {
        highlightSelection(SSContextMenu.HIGHLIGHT_GREEN)
    }

    fun highlightBlue() {
        highlightSelection(SSContextMenu.HIGHLIGHT_BLUE)
    }

    fun underline() {
        highlightSelection(SSContextMenu.UNDERLINE)
    }

    fun unHighlightSelection() {
        val ssReadingView = currentSSReadingView
        if (ssReadingView != null) {
            ssReadingView.ssReadViewBridge.unHighlightSelection(highlightId)
            ssReadingView.selectionFinished()
        }
    }

    private fun highlightSelection(color: String) {
        val ssReadingView = currentSSReadingView
        if (ssReadingView != null) {
            ssReadingView.ssReadViewBridge.highlightSelection(color, highlightId)
            ssReadingView.selectionFinished()
        }
        highlightId = 0
    }

    fun copy() {
        val ssReadingView = currentSSReadingView
        if (ssReadingView != null) {
            ssReadingView.ssReadViewBridge.copy()
            ssReadingView.selectionFinished()
        }
    }

    fun paste() {
        val ssReadingView = currentSSReadingView
        ssReadingView?.ssReadViewBridge?.paste()
    }

    fun share() {
        val ssReadingView = currentSSReadingView
        if (ssReadingView != null) {
            ssReadingView.ssReadViewBridge.share()
            ssReadingView.selectionFinished()
        }
    }

    fun search() {
        currentSSReadingView?.apply {
            ssReadViewBridge.search()
            selectionFinished()
        }
    }

    fun onSSReadingDisplayOptions(ssReadingDisplayOptions: SSReadingDisplayOptions) {
        currentSSReadingView?.updateReadingDisplayOptions(ssReadingDisplayOptions)
        val parent = currentSSReadingView?.parent as? ViewGroup
        parent?.setBackgroundColor(ssReadingDisplayOptions.colorTheme(parent.context))

        for (i in 0 until ssTotalReadsCount) {
            if (i == ssReadingActivityBinding.ssReadingViewPager.currentItem) continue
            val view = ssReadingActivityBinding.ssReadingViewPager.findViewWithTag<View?>("ssReadingView_$i")
            if (view != null) {
                val readingView = view.findViewById<SSReadingView>(R.id.ss_reading_view)
                readingView.updateReadingDisplayOptions(ssReadingDisplayOptions)
            }
        }
    }

    fun reloadContent() {
        loadLessonInfo()
        checkConnection()
    }

    companion object {
        private const val DEFAULT_PING_HOST = "www.google.com"
        private const val DEFAULT_PING_PORT = 80
        private const val DEFAULT_PING_INTERVAL_IN_MS = 2000
        private const val DEFAULT_INITIAL_PING_INTERVAL_IN_MS = 500
        private const val DEFAULT_PING_TIMEOUT_IN_MS = 2000

        /**
         * Pass true if this view should be visible in light theme
         * false if it should be visible in dark theme
         */
        @JvmStatic
        @BindingAdapter("showInLightTheme")
        fun setVisibility(view: View, show: Boolean) {
            val isLightTheme = !view.context.isDarkTheme()
            view.isVisible = (show && isLightTheme) || (!show && !isLightTheme)
        }

        fun provideFactory(
            assistedFactory: ReadingViewModelFactory,
            lessonIndex: String,
            dataListener: DataListener,
            ssReadingActivityBinding: SsReadingActivityBinding
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {

            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(
                    lessonIndex, dataListener, ssReadingActivityBinding
                ) as T
            }
        }
    }
}

@AssistedFactory
interface ReadingViewModelFactory {
    fun create(
        lessonIndex: String,
        dataListener: SSReadingViewModel.DataListener,
        ssReadingActivityBinding: SsReadingActivityBinding
    ): SSReadingViewModel
}
