/*
 * Copyright (c) 2023. Adventech <info@adventech.io>
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
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package app.ss.readings

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import app.ss.bible.BibleVersesActivity
import app.ss.lessons.data.model.SSContextMenu
import app.ss.lessons.data.repository.user.UserDataRepository
import app.ss.models.SSLessonInfo
import app.ss.models.SSReadComments
import app.ss.models.SSReadHighlights
import app.ss.readings.databinding.SsReadingActivityBinding
import app.ss.readings.model.ReadingsState
import app.ss.readings.options.SSReadingDisplayOptionsView
import com.cryart.sabbathschool.core.extensions.context.isDarkTheme
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import ss.foundation.android.connectivity.ConnectivityHelper
import ss.foundation.coroutines.DispatcherProvider
import ss.foundation.coroutines.Scopable
import ss.foundation.coroutines.debounceUntilLast
import ss.foundation.coroutines.flow.stateIn
import ss.foundation.coroutines.mainScopable
import ss.lessons.api.repository.LessonsRepositoryV2
import ss.misc.SSConstants
import ss.misc.SSEvent
import ss.prefs.model.SSReadingDisplayOptions
import ss.prefs.model.colorTheme
import kotlin.math.roundToInt

class SSReadingViewModel @AssistedInject constructor(
    lessonsRepository: LessonsRepositoryV2,
    private val userDataRepository: UserDataRepository,
    private val connectivityHelper: ConnectivityHelper,
    private val dispatcherProvider: DispatcherProvider,
    @Assisted private val ssLessonIndex: String,
    @Assisted private val ssReadingActivityBinding: SsReadingActivityBinding,
    @Assisted private val activity: FragmentActivity
) : SSReadingView.ContextMenuCallback,
    SSReadingView.HighlightsCommentsCallback,
    Scopable by mainScopable(dispatcherProvider) {

    private val context: Context = activity
    private var ssTotalReadsCount = 0
    private var highlightId = 0
    private val ssLessonInfo: SSLessonInfo?
        get() = (viewState.value as? ReadingsState.Success)?.lessonReads?.lessonInfo
    val lessonTitle: String get() = ssLessonInfo?.lesson?.title ?: ""
    val lessonShareIndex: String get() = ssLessonInfo?.shareIndex() ?: ""

    val viewState: StateFlow<ReadingsState> = lessonsRepository.getLessonReads(ssLessonIndex)
        .map { result ->
            val data = result.getOrNull()
            if (data != null) {
                ssTotalReadsCount = data.lessonInfo.days.size
                ReadingsState.Success(data)
            } else {
                ReadingsState.Error(isOffline = !connectivityHelper.isConnected())
            }
        }
        .stateIn(scope, ReadingsState.Loading)


    private val currentSSReadingView: SSReadingView?
        get() {
            val view = ssReadingActivityBinding.ssReadingViewPager
                .findViewWithTag<View>("ssReadingView_" + ssReadingActivityBinding.ssReadingViewPager.currentItem)
            return view?.findViewById(R.id.ss_reading_view)
        }

    private val verseClickWithDebounce: (verse: String) -> Unit =
        debounceUntilLast(
            scope = ssReadingActivityBinding.root.findViewTreeLifecycleOwner()?.lifecycleScope ?: MainScope()
        ) { verse ->
            val ssReads = (viewState.value as? ReadingsState.Success)?.lessonReads?.reads ?: return@debounceUntilLast
            val intent = BibleVersesActivity.launchIntent(
                context,
                verse,
                ssReads[ssReadingActivityBinding.ssReadingViewPager.currentItem].index
            )
            context.startActivity(intent)
        }

    override fun onSelectionStarted(x: Float, y: Float, highlightId: Int) {
        this.highlightId = highlightId
    }

    @Suppress("DEPRECATION")
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
        val margin: Int = convertDpToPixels(context, 50)
        val jumpMargin: Int = convertDpToPixels(context, 60)
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

    private fun convertDpToPixels(context: Context, dp: Int): Int {
        val displayMetrics = context.resources.displayMetrics
        return (dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
    }

    override fun onSelectionFinished() {
        ssReadingActivityBinding.ssContextMenu.ssReadingContextMenu.visibility = View.INVISIBLE
    }

    override fun onHighlightsReceived(ssReadHighlights: SSReadHighlights) {
        userDataRepository.saveHighlights(ssReadHighlights)
    }

    override fun onCommentsReceived(ssReadComments: SSReadComments) {
        userDataRepository.saveComments(ssReadComments)
    }

    override fun onVerseClicked(verse: String) = verseClickWithDebounce(verse)

    fun onDisplayOptionsClick() {
        val ssReadingDisplayOptionsView = SSReadingDisplayOptionsView()
        val fragmentManager = (context as? FragmentActivity)?.supportFragmentManager ?: return
        ssReadingDisplayOptionsView.show(fragmentManager, ssReadingDisplayOptionsView.tag)

        val ssReads = (viewState.value as? ReadingsState.Success)?.lessonReads?.reads ?: return
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
        parent?.setBackgroundColor(ssReadingDisplayOptions.colorTheme(parent.context.isDarkTheme()))

        for (i in 0 until ssTotalReadsCount) {
            if (i == ssReadingActivityBinding.ssReadingViewPager.currentItem) continue
            val view = ssReadingActivityBinding.ssReadingViewPager.findViewWithTag<View?>("ssReadingView_$i")
            if (view != null) {
                val readingView = view.findViewById<SSReadingView>(R.id.ss_reading_view)
                readingView.updateReadingDisplayOptions(ssReadingDisplayOptions)
            }
        }
    }

    fun reloadContent() = Unit // Handle retry logic
}

@AssistedFactory
interface ReadingViewModelFactory {
    fun create(
        lessonIndex: String,
        ssReadingActivityBinding: SsReadingActivityBinding,
        activity: FragmentActivity
    ): SSReadingViewModel
}
