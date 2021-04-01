package com.cryart.sabbathschool.readings.days.components

import androidx.lifecycle.LifecycleOwner
import app.ss.lessons.data.model.SSReadComments
import app.ss.lessons.data.model.SSReadHighlights
import com.cryart.sabbathschool.core.extensions.coroutines.flow.collectIn
import com.cryart.sabbathschool.core.extensions.logger.timber
import com.cryart.sabbathschool.core.model.SSReadingDisplayOptions
import com.cryart.sabbathschool.readings.components.BaseComponent
import com.cryart.sabbathschool.readings.databinding.ComponentReadingViewBinding
import kotlinx.coroutines.flow.Flow

class ReadingViewComponent(
    private val options: SSReadingDisplayOptions,
    private val binding: ComponentReadingViewBinding
) : BaseComponent<ReadingData>, SSReadingView.ContextMenuCallback, SSReadingView.HighlightsCommentsCallback {

    private val logger by timber()

    init {
        binding.readingView.apply {
            setContextMenuCallback(this@ReadingViewComponent)
            setHighlightsCommentsCallback(this@ReadingViewComponent)
        }
    }

    override fun collect(visibilityFlow: Flow<Boolean>, dataFlow: Flow<ReadingData>, owner: LifecycleOwner) {
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
        logger.d("VERSE: $verse")
    }
}
