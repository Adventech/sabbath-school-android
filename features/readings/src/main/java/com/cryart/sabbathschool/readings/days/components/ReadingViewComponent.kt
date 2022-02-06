package com.cryart.sabbathschool.readings.days.components

import androidx.lifecycle.LifecycleOwner
import app.ss.models.SSReadComments
import app.ss.models.SSReadHighlights
import com.cryart.sabbathschool.core.extensions.coroutines.flow.collectIn
import com.cryart.sabbathschool.core.model.SSReadingDisplayOptions
import com.cryart.sabbathschool.core.ui.BaseComponent
import com.cryart.sabbathschool.readings.databinding.ComponentReadingViewBinding
import kotlinx.coroutines.flow.Flow
import timber.log.Timber

class ReadingViewComponent(
    lifecycleOwner: LifecycleOwner,
    private val options: SSReadingDisplayOptions,
    private val binding: ComponentReadingViewBinding
) : BaseComponent<ReadingData>(lifecycleOwner), SSReadingView.ContextMenuCallback, SSReadingView.HighlightsCommentsCallback {

    init {
        binding.readingView.apply {
            setContextMenuCallback(this@ReadingViewComponent)
            setHighlightsCommentsCallback(this@ReadingViewComponent)
        }
    }

    override fun collect(visibilityFlow: Flow<Boolean>, dataFlow: Flow<ReadingData>) {
        dataFlow.collectIn(owner) { data ->
            when (data) {
                is ReadingData.Content -> {
                    binding.readingView.apply {
                        loadContent(data.content, options)
                    }
                }
                ReadingData.Empty -> {
                }
            }
        }
    }

    override fun onSelectionStarted(x: Float, y: Float) {
    }

    override fun onSelectionStarted(x: Float, y: Float, highlightId: Int) {
        onSelectionStarted(x, y)
    }

    override fun onSelectionFinished() {
    }

    override fun onHighlightsReceived(highlights: SSReadHighlights) {
    }

    override fun onCommentsReceived(comments: SSReadComments) {
    }

    override fun onVerseClicked(verse: String) {
        Timber.d("VERSE: $verse")
    }
}
