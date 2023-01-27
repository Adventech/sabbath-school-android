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

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import app.ss.media.playback.PlaybackViewModel
import app.ss.media.playback.ui.nowPlaying.showNowPlaying
import app.ss.media.playback.ui.video.showVideoList
import app.ss.models.SSLessonInfo
import app.ss.models.SSRead
import app.ss.models.SSReadComments
import app.ss.models.SSReadHighlights
import app.ss.models.colorTheme
import app.ss.models.media.MediaAvailability
import app.ss.pdf.PdfReader
import coil.load
import com.cryart.design.theme
import com.cryart.sabbathschool.core.extensions.context.colorPrimary
import com.cryart.sabbathschool.core.extensions.context.isDarkTheme
import com.cryart.sabbathschool.core.extensions.context.launchWebUrl
import com.cryart.sabbathschool.core.extensions.context.shareContent
import com.cryart.sabbathschool.core.extensions.context.toWebUri
import com.cryart.sabbathschool.core.extensions.coroutines.flow.collectIn
import com.cryart.sabbathschool.core.extensions.view.viewBinding
import com.cryart.sabbathschool.core.ui.ShareableScreen
import com.cryart.sabbathschool.core.ui.SlidingActivity
import com.cryart.sabbathschool.lessons.R
import com.cryart.sabbathschool.lessons.databinding.SsReadingActivityBinding
import com.cryart.sabbathschool.lessons.ui.readings.components.MiniPlayerComponent
import dagger.hilt.android.AndroidEntryPoint
import ss.misc.DateHelper
import ss.misc.SSConstants
import ss.prefs.api.SSPrefs
import javax.inject.Inject

@AndroidEntryPoint
class SSReadingActivity : SlidingActivity(), SSReadingViewModel.DataListener, ShareableScreen {

    @Inject
    lateinit var ssPrefs: SSPrefs

    @Inject
    lateinit var pdfReader: PdfReader

    @Inject
    lateinit var viewModelFactory: ReadingViewModelFactory

    private val binding by viewBinding(SsReadingActivityBinding::inflate)

    private lateinit var ssReadingViewModel: SSReadingViewModel

    private val readingViewAdapter: ReadingViewPagerAdapter by lazy {
        ReadingViewPagerAdapter(ssReadingViewModel)
    }
    private val viewModel by viewModels<ReadingsViewModel>()
    private val playbackViewModel by viewModels<PlaybackViewModel>()

    private var appbarChangeListener: AppbarOffsetChangeListener? = null

    private var currentReadPosition: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initUI()

        binding.root.setViewTreeLifecycleOwner(this)

        ssReadingViewModel = viewModelFactory.create(
            intent.extras?.getString(SSConstants.SS_LESSON_INDEX_EXTRA)!!,
            this,
            binding,
            this
        )

        // Read position passed in intent extras
        val extraPosition = intent.extras?.getString(SSConstants.SS_READ_POSITION_EXTRA)
        currentReadPosition = savedInstanceState?.getInt(SSConstants.SS_READ_POSITION_EXTRA) ?: extraPosition?.toIntOrNull()

        binding.ssReadingViewPager.apply {
            offscreenPageLimit = 4
            adapter = readingViewAdapter
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)

                    val ssRead = readingViewAdapter.getReadAt(position) ?: return
                    setPageTitleAndSubtitle(ssRead.title, DateHelper.formatDate(ssRead.date, SSConstants.SS_DATE_FORMAT_OUTPUT_DAY))
                }
            })
        }
        binding.executePendingBindings()
        binding.viewModel = ssReadingViewModel
        updateColorScheme()

        observeData()
    }

    private fun initUI() {
        setSupportActionBar(binding.ssReadingAppBar.ssReadingToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        with(binding.ssReadingViewPager) {
            val insetAnimator = KeyboardInsetsChangeAnimator(this)
            WindowCompat.setDecorFitsSystemWindows(window, false)
            ViewCompat.setWindowInsetsAnimationCallback(this, insetAnimator)
            ViewCompat.setOnApplyWindowInsetsListener(this, insetAnimator)
        }

        currentReadPosition?.let {
            binding.ssReadingViewPager.currentItem = it
        }

        MiniPlayerComponent(
            binding.ssPlayerView,
            playbackViewModel.playbackConnection,
            onExpand = {
                supportFragmentManager.showNowPlaying(
                    viewModel.lessonIndex,
                    getReadIndex()
                )
            }
        )
    }

    private fun updateColorScheme() {
        val primaryColor = this.colorPrimary
        binding.ssReadingAppBar.ssReadingCollapsingToolbar.setContentScrimColor(primaryColor)
        binding.ssReadingAppBar.ssReadingCollapsingToolbar.setBackgroundColor(primaryColor)
        binding.ssProgressBar.ssQuarterliesLoading.theme(primaryColor)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.ss_reading_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finishAfterTransition()
                true
            }
            R.id.ss_reading_menu_audio -> {
                supportFragmentManager.showNowPlaying(
                    viewModel.lessonIndex,
                    getReadIndex()
                )
                true
            }
            R.id.ss_reading_menu_video -> {
                viewModel.lessonIndex?.let {
                    supportFragmentManager.showVideoList(it)
                }
                true
            }
            R.id.ss_reading_menu_share -> {
                val position = binding.ssReadingViewPager.currentItem
                val readTitle = readingViewAdapter.getReadAt(position)?.title ?: ""
                val message = "${ssReadingViewModel.lessonTitle} - $readTitle"
                shareContent(
                    "$message\n${getShareWebUri()}",
                    getString(R.string.ss_menu_share_app)
                )
                true
            }
            R.id.ss_reading_menu_pdf -> {
                val (index, pdfs) = viewModel.lessonPdfsFlow.value
                if (pdfs.isNotEmpty()) {
                    val media = MediaAvailability(
                        audio = viewModel.audioAvailableFlow.value,
                        video = viewModel.videoAvailableFlow.value
                    )
                    startActivity(pdfReader.launchIntent(pdfs, index, mediaAvailability = media))
                }
                true
            }
            R.id.ss_reading_menu_display_options -> {
                ssReadingViewModel.onDisplayOptionsClick()
                true
            }
            R.id.ss_reading_menu_printed_resources -> {
                viewModel.publishingInfo.value?.let { launchWebUrl(it.url) }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.ss_reading_menu_audio)?.isVisible = viewModel.audioAvailableFlow.value
        menu.findItem(R.id.ss_reading_menu_video)?.isVisible = viewModel.videoAvailableFlow.value
        menu.findItem(R.id.ss_reading_menu_pdf)?.isVisible = viewModel.pdfAvailableFlow.value
        menu.findItem(R.id.ss_reading_menu_printed_resources)?.isVisible = viewModel.publishingInfo.value != null
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onLessonInfoChanged(ssLessonInfo: SSLessonInfo) {
        binding.ssReadingAppBar.ssCollapsingToolbarBackdrop.load(ssLessonInfo.lesson.cover)
    }

    private fun setPageTitleAndSubtitle(title: String, subTitle: String) {
        binding.ssReadingAppBar.apply {
            ssReadingCollapsingToolbar.title = title
            ssCollapsingToolbarSubtitle.text = subTitle
        }
    }

    override fun onReadsDownloaded(
        ssReads: List<SSRead>,
        ssReadHighlights: List<SSReadHighlights>,
        ssReadComments: List<SSReadComments>,
        ssReadIndex: Int
    ) {
        val index = currentReadPosition ?: ssReadIndex
        val observer = object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                binding.ssReadingViewPager.currentItem = index
                ssReads.getOrNull(index)?.let { read ->
                    setPageTitleAndSubtitle(
                        read.title,
                        DateHelper.formatDate(read.date, SSConstants.SS_DATE_FORMAT_OUTPUT_DAY)
                    )
                }
            }
        }
        with(readingViewAdapter) {
            registerAdapterDataObserver(observer)
            setContent(ssReads, ssReadHighlights, ssReadComments)
            unregisterAdapterDataObserver(observer)
        }

        currentReadPosition = null
    }

    @SuppressLint("MissingSuperCall")
    override fun onSaveInstanceState(outState: Bundle) {
        val position = binding.ssReadingViewPager.currentItem
        outState.putInt(SSConstants.SS_READ_POSITION_EXTRA, position)
        super.onSaveInstanceState(outState)
    }

    private fun observeData() {
        ssPrefs.displayOptionsFlow().collectIn(this) { displayOptions ->
            readingViewAdapter.readingOptions = displayOptions
            appbarChangeListener?.readingOptions = displayOptions
            ssReadingViewModel.onSSReadingDisplayOptions(displayOptions)
            window.navigationBarColor = displayOptions.colorTheme(this@SSReadingActivity.isDarkTheme())
        }

        viewModel.audioAvailableFlow.collectIn(this) { available ->
            val menu = binding.ssReadingAppBar.ssReadingToolbar.menu
            menu.findItem(R.id.ss_reading_menu_audio)?.isVisible = available
        }
        viewModel.videoAvailableFlow.collectIn(this) { available ->
            val menu = binding.ssReadingAppBar.ssReadingToolbar.menu
            menu.findItem(R.id.ss_reading_menu_video)?.isVisible = available
        }
        viewModel.pdfAvailableFlow.collectIn(this) { available ->
            val menu = binding.ssReadingAppBar.ssReadingToolbar.menu
            menu.findItem(R.id.ss_reading_menu_pdf)?.isVisible = available
        }
        viewModel.publishingInfo.collectIn(this) { info ->
            val menu = binding.ssReadingAppBar.ssReadingToolbar.menu
            menu.findItem(R.id.ss_reading_menu_printed_resources)?.isVisible = info != null
        }
    }

    override fun getShareWebUri(): Uri {
        val position = binding.ssReadingViewPager.currentItem
        val read = readingViewAdapter.getReadAt(position)
        val readIndex = read?.shareIndex(ssReadingViewModel.lessonShareIndex, position + 1)

        return "${getString(R.string.ss_app_host)}/${readIndex ?: ""}".toWebUri()
    }

    private fun getReadIndex(): String? {
        val position = binding.ssReadingViewPager.currentItem
        val read = readingViewAdapter.getReadAt(position)
        return read?.index
    }

    companion object {
        fun launchIntent(
            context: Context,
            lessonIndex: String,
            readPosition: String? = null
        ): Intent = Intent(context, SSReadingActivity::class.java).apply {
            putExtra(SSConstants.SS_LESSON_INDEX_EXTRA, lessonIndex)
            readPosition?.let {
                putExtra(SSConstants.SS_READ_POSITION_EXTRA, readPosition)
            }
        }
    }
}
