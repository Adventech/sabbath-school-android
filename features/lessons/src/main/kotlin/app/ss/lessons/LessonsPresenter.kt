/*
 * Copyright (c) 2024. Adventech <info@adventech.io>
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

package app.ss.lessons

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import app.ss.lessons.data.repository.lessons.LessonsRepository
import app.ss.models.OfflineState
import app.ss.models.PublishingInfo
import app.ss.models.SSQuarterly
import app.ss.models.SSQuarterlyInfo
import com.cryart.sabbathschool.core.extensions.context.shareContent
import com.cryart.sabbathschool.core.navigation.Destination
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.produceRetainedState
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuitx.android.IntentScreen
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.joda.time.Interval
import ss.lessons.api.PdfReader
import ss.lessons.api.repository.QuarterliesRepository
import ss.libraries.appwidget.api.AppWidgetHelper
import ss.libraries.circuit.navigation.CustomTabsIntentScreen
import ss.libraries.circuit.navigation.LegacyDestination
import ss.libraries.circuit.navigation.LessonsScreen
import ss.misc.DateHelper
import ss.misc.SSConstants
import ss.prefs.api.SSPrefs
import ss.workers.api.WorkScheduler
import timber.log.Timber
import app.ss.translations.R as L10n

class LessonsPresenter @AssistedInject constructor(
    @Assisted private val navigator: Navigator,
    @Assisted private val screen: LessonsScreen,
    private val repository: QuarterliesRepository,
    private val lessonsRepository: LessonsRepository,
    private val ssPrefs: SSPrefs,
    private val appWidgetHelper: AppWidgetHelper,
    private val workScheduler: WorkScheduler,
    private val pdfReader: PdfReader,
) : Presenter<State> {

    @CircuitInject(LessonsScreen::class, SingletonComponent::class)
    @AssistedFactory
    interface Factory {
        fun create(navigator: Navigator, screen: LessonsScreen): LessonsPresenter
    }

    private val quarterlyIndex: String
        get() = requireNotNull(screen.quarterlyIndex ?: ssPrefs.getLastQuarterlyIndex()) { "Quarterly index should now be null" }

    @Composable
    override fun present(): State {
        val coroutineScope = rememberCoroutineScope()
        val quarterlyInfo by produceRetainedState<SSQuarterlyInfo?>(
            initialValue = null
        ) {
            repository.getQuarterly(quarterlyIndex)
                .map { it.getOrNull() }
                .onEach { info ->
                    info?.run {
                        appWidgetHelper.syncQuarterly(quarterly.index)
                        updatePrefs(quarterly)
                    }
                }
                .catch { Timber.e(it) }
                .collect { value = it }
        }
        val publishingInfo by produceRetainedState<PublishingInfo?>(initialValue = null) {
            repository.getPublishingInfo()
                .map { it.getOrNull() }
                .catch { Timber.e(it) }
                .collect { value = it }
        }

        var overlayState by rememberRetained { mutableStateOf<ReadMoreOverlayState?>(null) }

        return when (val info = quarterlyInfo) {
            null -> State.Loading
            else -> State.Success(info, publishingInfo, overlayState) { event ->
                handleEvent(event, coroutineScope, info, publishingInfo) {
                    overlayState = it
                }
            }
        }
    }

    private fun handleEvent(
        event: Event,
        coroutineScope: CoroutineScope,
        info: SSQuarterlyInfo?,
        publishingInfo: PublishingInfo?,
        overlayStateUpdate: (ReadMoreOverlayState?) -> Unit
    ) {
        when (event) {
            is Event.OnLessonClick -> {
                if (event.lesson.pdfOnly) {
                    goToPdfScreen(event.lesson.index, coroutineScope)
                } else {
                    navigator.goTo(
                        LegacyDestination(
                            destination = Destination.READ,
                            extras = bundleOf(SSConstants.SS_LESSON_INDEX_EXTRA to event.lesson.index)
                        )
                    )
                }
            }

            Event.OnNavigateBackClick -> navigator.pop()
            Event.OnOfflineStateClick -> info?.let { handleOfflineStateClick(it) }
            Event.OnReadMoreClick -> info?.let {
                overlayStateUpdate(
                    ReadMoreOverlayState(
                        content = it.quarterly.introduction ?: it.quarterly.description,
                    ) { overlayStateUpdate(null) }
                )
            }
            is Event.OnShareClick -> info?.let { shareQuarterly(event.context, it) }
            Event.OnPublishingInfoClick -> publishingInfo?.let { navigator.goTo(CustomTabsIntentScreen(it.url)) }
        }
    }

    private fun updatePrefs(quarterly: SSQuarterly) {
        ssPrefs.setLastQuarterlyIndex(quarterly.index)

        ssPrefs.setThemeColor(
            primary = quarterly.color_primary,
            primaryDark = quarterly.color_primary_dark
        )
        ssPrefs.setReadingLatestQuarterly(quarterly.isLatest())
    }

    private fun goToPdfScreen(lessonIndex: String, scope: CoroutineScope) {
        scope.launch {
            val resource = lessonsRepository.getLessonInfo(lessonIndex)
            if (resource.isSuccessFul) {
                val data = resource.data
                val pdfs = data?.pdfs ?: return@launch
                navigator.goTo(IntentScreen(pdfReader.launchIntent(pdfs, lessonIndex)))
            }
        }
    }

    private fun handleOfflineStateClick(info: SSQuarterlyInfo) {
        when (info.quarterly.offlineState) {
            OfflineState.IN_PROGRESS -> Unit
            OfflineState.NONE,
            OfflineState.PARTIAL,
            OfflineState.COMPLETE -> workScheduler.syncQuarterly(info.quarterly.index)
        }
    }

    private fun shareQuarterly(context: Context, info: SSQuarterlyInfo) {
        val title = info.quarterly.title
        val uri = "${context.getString(L10n.string.ss_app_share_host)}/${info.shareIndex()}".toUri()
        context.shareContent("$title\n$uri", context.getString(L10n.string.ss_menu_share_app))
    }

    private fun SSQuarterly.isLatest(): Boolean {
        val today = DateTime.now().withTimeAtStartOfDay()

        val startDate = DateHelper.parseDate(start_date)
        val endDate = DateHelper.parseDate(end_date)

        return Interval(startDate, endDate?.plusDays(1)).contains(today)
    }
}
