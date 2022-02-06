package com.cryart.sabbathschool.lessons.ui.readings

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.ss.models.SSRead
import app.ss.lessons.data.model.SSReadComments
import app.ss.lessons.data.model.SSReadHighlights
import com.cryart.sabbathschool.core.extensions.context.isDarkTheme
import com.cryart.sabbathschool.core.extensions.view.inflate
import com.cryart.sabbathschool.core.model.SSReadingDisplayOptions
import com.cryart.sabbathschool.core.model.colorTheme
import com.cryart.sabbathschool.lessons.R
import com.cryart.sabbathschool.lessons.databinding.SsReadingViewBinding

class ReadingViewPagerAdapter(
    private val readingViewModel: SSReadingViewModel
) : RecyclerView.Adapter<ReadingViewHolder>() {

    var readingOptions: SSReadingDisplayOptions? = null

    private var ssReads: List<SSRead> = emptyList()
    private var ssReadHighlights: List<SSReadHighlights> = emptyList()
    private var ssReadComments: List<SSReadComments> = emptyList()

    fun setContent(
        ssReads: List<SSRead>,
        ssReadHighlights: List<SSReadHighlights>,
        ssReadComments: List<SSReadComments>
    ) {
        this.ssReads = ssReads
        this.ssReadHighlights = ssReadHighlights
        this.ssReadComments = ssReadComments
        this.notifyDataSetChanged()
    }

    fun getReadAt(position: Int): SSRead? = ssReads.getOrNull(position)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReadingViewHolder =
        ReadingViewHolder.create(parent)

    override fun onBindViewHolder(holder: ReadingViewHolder, position: Int) {
        holder.bind(
            ssReads[position],
            ssReadHighlights[position],
            ssReadComments[position],
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

    override fun getItemCount(): Int = ssReads.size
}

class ReadingViewHolder(
    private val binding: SsReadingViewBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(
        read: SSRead,
        highlights: SSReadHighlights,
        comments: SSReadComments,
        readingOptions: SSReadingDisplayOptions,
        readingViewModel: SSReadingViewModel
    ) {

        val context = binding.root.context
        binding.ssReadingViewScroll.setBackgroundColor(readingOptions.colorTheme(context))
        binding.ssReadingView.apply {
            setBackgroundColor(readingOptions.colorTheme(context))
            setContextMenuCallback(readingViewModel)
            setHighlightsCommentsCallback(readingViewModel)
            setReadHighlights(highlights)
            setReadComments(comments)
            loadContent(read.content, readingOptions)
        }
    }

    companion object {
        fun create(parent: ViewGroup): ReadingViewHolder = ReadingViewHolder(
            SsReadingViewBinding.bind(parent.inflate(R.layout.ss_reading_view))
        )
    }
}
