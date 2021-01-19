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
import android.text.InputType
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.databinding.ObservableInt
import com.afollestad.materialdialogs.MaterialDialog
import com.cryart.sabbathschool.core.extensions.prefs.SSPrefs
import com.cryart.sabbathschool.core.extensions.strings.StringUtils
import com.cryart.sabbathschool.core.misc.SSConstants
import com.cryart.sabbathschool.core.misc.SSEvent
import com.cryart.sabbathschool.core.misc.SSHelper
import com.cryart.sabbathschool.core.model.SSReadingDisplayOptions
import com.cryart.sabbathschool.lessons.R
import com.cryart.sabbathschool.lessons.data.model.SSContextMenu
import com.cryart.sabbathschool.lessons.data.model.SSLessonInfo
import com.cryart.sabbathschool.lessons.data.model.SSRead
import com.cryart.sabbathschool.lessons.data.model.SSReadComments
import com.cryart.sabbathschool.lessons.data.model.SSReadHighlights
import com.cryart.sabbathschool.lessons.data.model.SSSuggestion
import com.cryart.sabbathschool.lessons.databinding.SsReadingActivityBinding
import com.cryart.sabbathschool.lessons.ui.readings.options.SSReadingDisplayOptionsView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.ArrayList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import ru.beryukhov.reactivenetwork.ReactiveNetwork
import ru.beryukhov.reactivenetwork.internet.observing.InternetObservingSettings
import timber.log.Timber

class SSReadingViewModel(
    private val context: Context,
    private val dataListener: DataListener,
    private val ssLessonIndex: String,
    private val ssReadingActivityBinding: SsReadingActivityBinding
) : SSReadingView.ContextMenuCallback, SSReadingView.HighlightsCommentsCallback {
    private val ssFirebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val userUuid = ssFirebaseAuth.currentUser?.uid!!
    private val mDatabase = FirebaseDatabase.getInstance().reference.apply {
        keepSynced(true)
    }
    var ssLessonInfo: SSLessonInfo? = null
    private var ssReadIndexInt = 0
    private val ssReads: ArrayList<SSRead> = arrayListOf()
    private val ssReadHighlights: ArrayList<SSReadHighlights> = arrayListOf()
    private val ssReadComments: ArrayList<SSReadComments> = arrayListOf()
    private var ssTotalReadsCount = 0
    private var ssReadsLoadedCounter = 0
    private var ssReadsDownloaded = false
    private var highlightId = 0
    val ssLessonLoadingVisibility = ObservableInt(View.INVISIBLE)
    val ssLessonOfflineStateVisibility = ObservableInt(View.INVISIBLE)
    val ssLessonErrorStateVisibility = ObservableInt(View.INVISIBLE)
    val ssLessonCoordinatorVisibility = ObservableInt(View.INVISIBLE)

    private val prefs = SSPrefs(context)
    private var ssReadingDisplayOptions = prefs.getDisplayOptions()

    init {
        loadLessonInfo()
        val inetOptions = InternetObservingSettings.builder()
            .host(DEFAULT_PING_HOST)
            .port(DEFAULT_PING_PORT)
            .timeout(DEFAULT_PING_TIMEOUT_IN_MS)
            .initialInterval(DEFAULT_INITIAL_PING_INTERVAL_IN_MS)
            .interval(DEFAULT_PING_INTERVAL_IN_MS)
            .build()
        ReactiveNetwork().observeInternetConnectivity(inetOptions).onEach {
            if (it && ssLessonInfo == null) {
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

    private fun loadLessonInfo() {
        ssLessonLoadingVisibility.set(View.VISIBLE)
        ssLessonOfflineStateVisibility.set(View.INVISIBLE)
        ssLessonErrorStateVisibility.set(View.INVISIBLE)
        ssLessonCoordinatorVisibility.set(View.INVISIBLE)

        mDatabase.child(SSConstants.SS_FIREBASE_LESSON_INFO_DATABASE)
            .child(ssLessonIndex)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    ssLessonInfo = dataSnapshot.getValue(SSLessonInfo::class.java)
                    ssTotalReadsCount = ssLessonInfo!!.days.size

                    if (ssLessonInfo?.days?.isNotEmpty()!!) {
                        dataListener.onLessonInfoChanged(ssLessonInfo!!)
                        val today = DateTime.now().withTimeAtStartOfDay()
                        for ((idx, ssDay) in ssLessonInfo?.days?.withIndex()!!) {
                            val startDate = DateTimeFormat.forPattern(SSConstants.SS_DATE_FORMAT)
                                .parseLocalDate(ssDay.date).toDateTimeAtStartOfDay()
                            if (startDate.isEqual(today) && ssReadIndexInt < 6) {
                                ssReadIndexInt = idx
                            }
                            downloadHighlights(ssDay.index, idx)
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Timber.e(databaseError.toException())
                    ssLessonErrorStateVisibility.set(View.VISIBLE)
                    ssLessonLoadingVisibility.set(View.INVISIBLE)
                    ssLessonOfflineStateVisibility.set(View.INVISIBLE)
                    ssLessonCoordinatorVisibility.set(View.INVISIBLE)
                }
            })
    }

    fun promptForEditSuggestion() {
        if (ssReads.isNotEmpty()) {
            val name = prefs.getUserName(context.getString(R.string.ss_menu_anonymous_name))
            val email = prefs.getUserEmail(context.getString(R.string.ss_menu_anonymous_email))
            MaterialDialog.Builder(context)
                .title(context.getString(R.string.ss_reading_suggest_edit))
                .inputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
                .input(context.getString(R.string.ss_reading_suggest_edit_hint), "") { _, input ->
                    mDatabase.child(SSConstants.SS_FIREBASE_SUGGESTIONS_DATABASE)
                        .child(userUuid)
                        .child(ssReads[ssReadingActivityBinding.ssReadingViewPager.currentItem].index)
                        .setValue(SSSuggestion(name, email, input.toString()))
                    Toast.makeText(context, context.getString(R.string.ss_reading_suggest_edit_done), Toast.LENGTH_LONG).show()
                }.show()
        }
    }

    private fun downloadHighlights(dayIndex: String, index: Int) {
        mDatabase.child(SSConstants.SS_FIREBASE_HIGHLIGHTS_DATABASE)
            .child(userUuid)
            .child(dayIndex)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val ssReadHighlights = dataSnapshot.getValue(SSReadHighlights::class.java) ?: SSReadHighlights(dayIndex, "")
                    downloadComments(dayIndex, index, ssReadHighlights)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Timber.e(databaseError.toException())
                }
            })
    }

    private fun downloadComments(dayIndex: String, index: Int, ssReadHighlights: SSReadHighlights) {
        mDatabase.child(SSConstants.SS_FIREBASE_COMMENTS_DATABASE)
            .child(userUuid)
            .child(dayIndex)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val ssReadComments = dataSnapshot.getValue(SSReadComments::class.java) ?: SSReadComments(dayIndex, listOf())
                    downloadRead(dayIndex, index, ssReadHighlights, ssReadComments)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Timber.e(databaseError.toException())
                }
            })
    }

    private fun downloadRead(dayIndex: String, index: Int, _ssReadHighlights: SSReadHighlights, _ssReadComments: SSReadComments) {
        mDatabase.child(SSConstants.SS_FIREBASE_READS_DATABASE)
            .child(dayIndex)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.value != null) {
                        if (ssReadsLoadedCounter < ssTotalReadsCount && ssReads.size >= index) {
                            val ssRead = dataSnapshot.getValue(SSRead::class.java)
                            ssReads.add(index, ssRead!!)
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
            })
    }

    private val currentSSReadingView: SSReadingView?
        get() {
            val view = ssReadingActivityBinding.ssReadingViewPager
                .findViewWithTag<View>("ssReadingView_" + ssReadingActivityBinding.ssReadingViewPager.currentItem)
            return view?.findViewById(R.id.ss_reading_view)
        }

    val cover: String
        get() = ssLessonInfo?.lesson?.cover ?: ""

    val lessonInterval: String
        get() {
            if (ssLessonInfo != null) {
                val startDateOut = DateTimeFormat.forPattern(SSConstants.SS_DATE_FORMAT_OUTPUT)
                    .print(
                        DateTimeFormat.forPattern(SSConstants.SS_DATE_FORMAT)
                            .parseLocalDate(ssLessonInfo?.lesson?.start_date)
                    )
                val endDateOut = DateTimeFormat.forPattern(SSConstants.SS_DATE_FORMAT_OUTPUT)
                    .print(
                        DateTimeFormat.forPattern(SSConstants.SS_DATE_FORMAT)
                            .parseLocalDate(ssLessonInfo?.lesson?.end_date)
                    )
                return StringUtils.capitalize(startDateOut) + " - " + StringUtils.capitalize(endDateOut)
            }
            return ""
        }

    @JvmOverloads
    fun formatDate(date: String?, DateFormatOutput: String? = SSConstants.SS_DATE_FORMAT_OUTPUT): String {
        return StringUtils.capitalize(
            DateTimeFormat.forPattern(DateFormatOutput)
                .print(
                    DateTimeFormat.forPattern(SSConstants.SS_DATE_FORMAT)
                        .parseLocalDate(date)
                )
        )
    }

    fun getDayDate(ssDayIdx: Int): String {
        return if (ssLessonInfo != null && ssLessonInfo?.days?.size!! > ssDayIdx) {
            formatDate(ssLessonInfo?.days?.get(ssDayIdx)?.date)
        } else ""
    }

    override fun onSelectionStarted(x: Float, y: Float, highlightId: Int) {
        onSelectionStarted(x, y)
        this.highlightId = highlightId
    }

    override fun onSelectionStarted(posX: Float, posY: Float) {
        var y = posY
        val view: LinearLayout = ssReadingActivityBinding.ssReadingViewPager
            .findViewWithTag("ssReadingView_" + ssReadingActivityBinding.ssReadingViewPager.currentItem)
        val scrollView: NestedScrollView = view.findViewById(R.id.ss_reading_view_scroll)
        y = y - scrollView.scrollY + ssReadingActivityBinding.ssReadingViewPager.top
        val metrics = DisplayMetrics()
        (context as Activity?)!!.windowManager.defaultDisplay.getMetrics(metrics)
        val params = ssReadingActivityBinding.ssContextMenu.ssReadingContextMenu.layoutParams as ViewGroup.MarginLayoutParams
        val contextMenuWidth = ssReadingActivityBinding.ssContextMenu.ssReadingContextMenu.width
        val contextMenuHeight = ssReadingActivityBinding.ssContextMenu.ssReadingContextMenu.height
        val screenWidth: Int = metrics.widthPixels
        val margin: Int = SSHelper.convertDpToPixels(context, 20)
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
        mDatabase.child(SSConstants.SS_FIREBASE_HIGHLIGHTS_DATABASE)
            .child(userUuid)
            .child(ssReadHighlights.readIndex)
            .setValue(ssReadHighlights)
        SSEvent.track(
            context,
            SSConstants.SS_EVENT_TEXT_HIGHLIGHTED,
            hashMapOf(SSConstants.SS_EVENT_PARAM_READ_INDEX to ssReads[ssReadingActivityBinding.ssReadingViewPager.currentItem].index)
        )
    }

    override fun onCommentsReceived(ssReadComments: SSReadComments) {
        mDatabase.child(SSConstants.SS_FIREBASE_COMMENTS_DATABASE)
            .child(userUuid)
            .child(ssReadComments.readIndex)
            .setValue(ssReadComments)
        SSEvent.track(
            context,
            SSConstants.SS_EVENT_COMMENT_CREATED,
            hashMapOf(SSConstants.SS_EVENT_PARAM_READ_INDEX to ssReads[ssReadingActivityBinding.ssReadingViewPager.currentItem].index)
        )
    }

    override fun onVerseClicked(verse: String) {
        // TODO: Add SSBibleVersesActivity
//        Intent _SSBibleActivityIntent = new Intent(context, SSBibleVersesActivity.class);
//        _SSBibleActivityIntent.putExtra(SSConstants.SS_READ_INDEX_EXTRA, ssReads.get(ssReadingActivityBinding.ssReadingViewPager.getCurrentItem()).index);
//        _SSBibleActivityIntent.putExtra(SSConstants.SS_READ_VERSE_EXTRA, verse);
//        context.startActivity(_SSBibleActivityIntent);
    }

    interface DataListener {
        fun onLessonInfoChanged(ssLessonInfo: SSLessonInfo)
        fun onReadsDownloaded(ssReads: List<SSRead>, ssReadHighlights: List<SSReadHighlights>, ssReadComments: List<SSReadComments>, ssReadIndex: Int)
    }

    fun onDisplayOptionsClick() {
        val ssReadingDisplayOptionsView = SSReadingDisplayOptionsView()
        ssReadingDisplayOptionsView.setSSReadingViewModel(context, this, ssReadingDisplayOptions)
        ssReadingDisplayOptionsView.show((context as SSReadingActivity?)!!.supportFragmentManager, ssReadingDisplayOptionsView.tag)
        SSEvent.track(
            context,
            SSConstants.SS_EVENT_READ_OPTIONS_OPEN,
            hashMapOf(SSConstants.SS_EVENT_PARAM_READ_INDEX to ssReads[ssReadingActivityBinding.ssReadingViewPager.currentItem].index)
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
        val ssReadingView = currentSSReadingView
        if (ssReadingView != null) {
            ssReadingView.ssReadViewBridge.search()
            ssReadingView.selectionFinished()
        }
    }

    fun onSSReadingDisplayOptions(ssReadingDisplayOptions: SSReadingDisplayOptions) {
        this.ssReadingDisplayOptions = ssReadingDisplayOptions
        prefs.setDisplayOptions(ssReadingDisplayOptions)

        val ssReadingView = currentSSReadingView
        if (ssReadingView != null) {
            ssReadingView.setReadingDisplayOptions(ssReadingDisplayOptions)
            ssReadingView.updateReadingDisplayOptions()
        }
        for (i in 0 until ssTotalReadsCount) {
            if (i == ssReadingActivityBinding.ssReadingViewPager.currentItem) continue
            if (ssReadingActivityBinding.ssReadingViewPager.findViewWithTag<View?>("ssReadingView_$i") != null) {
                val _ssReadingView: SSReadingView =
                    ssReadingActivityBinding.ssReadingViewPager.findViewWithTag<View>("ssReadingView_$i").findViewById(R.id.ss_reading_view)
                _ssReadingView.setReadingDisplayOptions(ssReadingDisplayOptions)
                _ssReadingView.updateReadingDisplayOptions()
            }
        }
    }

    fun reloadActivity() {
        (context as SSReadingActivity?)?.finish()
        context.startActivity((context as SSReadingActivity?)!!.intent)
    }

    fun onMenuClick() {
    }

    companion object {
        private const val DEFAULT_PING_HOST = "www.google.com"
        private const val DEFAULT_PING_PORT = 80
        private const val DEFAULT_PING_INTERVAL_IN_MS = 2000
        private const val DEFAULT_INITIAL_PING_INTERVAL_IN_MS = 500
        private const val DEFAULT_PING_TIMEOUT_IN_MS = 2000
    }
}
