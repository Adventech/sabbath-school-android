package com.cryart.sabbathschool.lessons.ui.readings

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.ss.models.SSRead
import app.ss.models.SSReadComments
import app.ss.models.SSReadHighlights
import com.cryart.sabbathschool.core.extensions.context.isDarkTheme
import com.cryart.sabbathschool.core.extensions.view.inflate
import com.cryart.sabbathschool.lessons.R
import com.cryart.sabbathschool.lessons.databinding.SsReadingViewBinding
import ss.prefs.model.SSReadingDisplayOptions
import ss.prefs.model.colorTheme

internal class ReadingViewPagerAdapter(
    private val readingViewModel: SSReadingViewModel
) : ListAdapter<ReadingContent, ReadingViewHolder>(ReadingContent.DIFF) {

    var readingOptions: SSReadingDisplayOptions? = null

    fun setContent(
        ssReads: List<SSRead>,
        ssReadHighlights: List<SSReadHighlights>,
        ssReadComments: List<SSReadComments>,
        commitCallback: Runnable? = null
    ) {
        val data = ssReads.mapIndexed { index, read ->
            ReadingContent(index, read, ssReadHighlights[index], ssReadComments[index])
        }
        submitList(data, commitCallback)
    }

    fun setContent(content: ReadUserContent) {
        val listIndex = currentList.indexOfFirst { it.read.index == content.index }
            .takeUnless { it == -1 } ?: return

        val data = currentList.mapIndexed { index, readingContent ->
            if (listIndex == index) {
                readingContent.copy(
                    comments = content.comments,
                    highlights = content.highlights
                )
            } else {
                readingContent
            }
        }
        submitList(data)
    }

    fun getContent(readIndex: String): ReadUserContent? {
        val listIndex = currentList.indexOfFirst { it.read.index == readIndex }
            .takeUnless { it == -1 } ?: return null
        val content = currentList[listIndex]
        return ReadUserContent(readIndex, content.comments, content.highlights)
    }

    fun getReadAt(position: Int): SSRead? = currentList.getOrNull(position)?.read

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReadingViewHolder =
        ReadingViewHolder.create(parent)

    override fun onBindViewHolder(holder: ReadingViewHolder, position: Int) {
        holder.bind(
            getItem(position),
            readingOptions ?: defaultDisplayOptions(holder.itemView.context),
            readingViewModel
        )
        holder.itemView.tag = "ssReadingView_$position"
    }

    private fun defaultDisplayOptions(context: Context): SSReadingDisplayOptions {
        val theme = if (context.isDarkTheme()) {
            SSReadingDisplayOptions.SS_THEME_DARK
        } else {
            SSReadingDisplayOptions.SS_THEME_LIGHT
        }
        return SSReadingDisplayOptions(
            theme,
            SSReadingDisplayOptions.SS_SIZE_MEDIUM,
            SSReadingDisplayOptions.SS_FONT_LATO
        )
    }
}

internal class ReadingViewHolder(
    private val binding: SsReadingViewBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(
        content: ReadingContent,
        readingOptions: SSReadingDisplayOptions,
        readingViewModel: SSReadingViewModel
    ) {
        val isDarkTheme = binding.root.context.isDarkTheme()
        binding.ssReadingViewScroll.setBackgroundColor(readingOptions.colorTheme(isDarkTheme))
        binding.ssReadingView.apply {
            setBackgroundColor(readingOptions.colorTheme(isDarkTheme))
            setContextMenuCallback(readingViewModel)
            setHighlightsCommentsCallback(readingViewModel)
            setReadHighlights(content.highlights)
            setReadComments(content.comments)
            loadContent(content.read.content, readingOptions)
        }
    }

    companion object {
        fun create(parent: ViewGroup): ReadingViewHolder = ReadingViewHolder(
            SsReadingViewBinding.bind(parent.inflate(R.layout.ss_reading_view))
        )
    }
}
