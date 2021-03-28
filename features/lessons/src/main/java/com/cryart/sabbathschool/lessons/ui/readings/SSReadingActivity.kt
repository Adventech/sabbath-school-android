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

import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.viewpager.widget.ViewPager
import coil.load
import com.cryart.sabbathschool.core.extensions.context.colorPrimary
import com.cryart.sabbathschool.core.extensions.prefs.SSPrefs
import com.cryart.sabbathschool.core.misc.SSConstants
import com.cryart.sabbathschool.core.misc.SSUnzip
import com.cryart.sabbathschool.core.navigation.AppNavigator
import com.cryart.sabbathschool.core.navigation.Destination
import com.cryart.sabbathschool.lessons.R
import app.ss.lessons.data.model.SSLessonInfo
import app.ss.lessons.data.model.SSReadComments
import app.ss.lessons.data.model.SSReadHighlights
import com.cryart.sabbathschool.lessons.databinding.SsReadingActivityBinding
import com.cryart.sabbathschool.lessons.ui.base.SSBaseActivity
import app.ss.lessons.data.model.SSRead
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.colorInt
import com.mikepenz.iconics.utils.sizeDp
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class SSReadingActivity : SSBaseActivity(), SSReadingViewModel.DataListener, ViewPager.OnPageChangeListener {

    @Inject
    lateinit var appNavigator: AppNavigator

    @Inject
    lateinit var ssPrefs: SSPrefs

    private val binding: SsReadingActivityBinding by lazy { SsReadingActivityBinding.inflate(layoutInflater) }
    private val latestReaderArtifactRef: StorageReference = FirebaseStorage.getInstance()
        .reference.child(SSConstants.SS_READER_ARTIFACT_NAME)
    private lateinit var ssReadingViewModel: SSReadingViewModel

    private var currentLessonIndex: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        checkIfReaderNeeded()

        initUI()
        val lessonIndex = intent.extras?.getString(SSConstants.SS_LESSON_INDEX_EXTRA)
        if (lessonIndex.isNullOrEmpty()) {
            finish()
            return
        }

        if (!this::ssReadingViewModel.isInitialized) {
            ssReadingViewModel = SSReadingViewModel(
                this,
                this,
                lessonIndex,
                binding,
                ssPrefs
            )
        }
        currentLessonIndex = savedInstanceState?.getInt(ARG_POSITION)
        binding.ssReadingViewPager.adapter = SSReadingViewAdapter(this, ssReadingViewModel)
        binding.ssReadingViewPager.addOnPageChangeListener(this)
        binding.executePendingBindings()
        binding.viewModel = ssReadingViewModel
        updateColorScheme()
    }

    private fun initUI() {
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE

        setSupportActionBar(binding.ssReadingAppBar.ssReadingToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        with(binding.ssReadingAppBar.ssReadingCollapsingToolbar) {
            setCollapsedTitleTextAppearance(R.style.AppThemeAppBarTextStyle)
            setExpandedTitleTextAppearance(R.style.AppThemeAppBarTextStyleExpanded)
            setCollapsedTitleTypeface(ResourcesCompat.getFont(this@SSReadingActivity, R.font.lato_bold))
            setExpandedTitleTypeface(ResourcesCompat.getFont(this@SSReadingActivity, R.font.lato_bold))
        }

        currentLessonIndex?.let {
            binding.ssReadingViewPager.currentItem = it
        }
    }

    private fun checkIfReaderNeeded() {
        latestReaderArtifactRef.metadata.addOnSuccessListener { storageMetadata: StorageMetadata ->
            val lastReaderArtifactCreationTime = ssPrefs.getLastReaderArtifactCreationTime()
            if (lastReaderArtifactCreationTime != storageMetadata.creationTimeMillis) {
                downloadLatestReader(storageMetadata.updatedTimeMillis)
            }
        }.addOnFailureListener {
            Timber.e(it.fillInStackTrace())
        }
    }

    private fun downloadLatestReader(readerArtifactCreationTime: Long) {
        val localFile = File(filesDir, SSConstants.SS_READER_ARTIFACT_NAME)
        latestReaderArtifactRef.getFile(localFile)
            .addOnSuccessListener {
                ssPrefs.setLastReaderArtifactCreationTime(readerArtifactCreationTime)
                SSUnzip(localFile.path, filesDir.path + "/")
            }.addOnFailureListener {
                Timber.e(it)
            }
    }

    private fun updateColorScheme() {
        val primaryColor = this.colorPrimary
        binding.ssReadingAppBar.ssReadingCollapsingToolbar.setContentScrimColor(primaryColor)
        binding.ssReadingAppBar.ssReadingCollapsingToolbar.setBackgroundColor(primaryColor)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.ss_reading_menu, menu)
        val menuItem = menu.findItem(R.id.ss_reading_menu_display_options)
        menuItem.icon = IconicsDrawable(this, GoogleMaterial.Icon.gmd_text_format).apply {
            colorInt = Color.WHITE
            sizeDp = 16
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.ss_reading_menu_share -> {
                shareApp(title as String?)
                true
            }
            R.id.ss_reading_menu_suggest_edit -> {
                ssReadingViewModel.promptForEditSuggestion()
                true
            }
            R.id.ss_reading_menu_display_options -> {
                ssReadingViewModel.onDisplayOptionsClick()
                true
            }
            R.id.ss_reading_menu_settings -> {
                appNavigator.navigate(this, Destination.SETTINGS)
                true
            }
            R.id.ss_reading_debug -> {
                appNavigator.navigate(this, Destination.READ, intent.extras)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        try {
            (binding.ssReadingAppBar.ssCollapsingToolbarBackdrop.drawable as? BitmapDrawable)?.bitmap?.recycle()
        } catch (e: Exception) {
            Timber.e(e)
        }
        super.onDestroy()
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
        val ssReadingViewAdapter = binding.ssReadingViewPager.adapter as? SSReadingViewAdapter
        ssReadingViewAdapter?.let {
            it.setSSReads(ssReads)
            it.setSSReadComments(ssReadComments)
            it.setSSReadHighlights(ssReadHighlights)
            it.notifyDataSetChanged()
        }
        val index = currentLessonIndex ?: ssReadIndex
        binding.ssReadingViewPager.currentItem = index
        val ssRead = ssReads.getOrNull(index) ?: return
        setPageTitleAndSubtitle(ssRead.title, ssReadingViewModel.formatDate(ssRead.date, SSConstants.SS_DATE_FORMAT_OUTPUT_DAY))

        currentLessonIndex = null
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
    override fun onPageSelected(position: Int) {
        val ssReadingViewAdapter = binding.ssReadingViewPager.adapter as? SSReadingViewAdapter
        val ssRead = ssReadingViewAdapter?.ssReads?.get(position) ?: return
        setPageTitleAndSubtitle(ssRead.title, ssReadingViewModel.formatDate(ssRead.date, SSConstants.SS_DATE_FORMAT_OUTPUT_DAY))
    }

    override fun onPageScrollStateChanged(state: Int) {}

    override fun onSaveInstanceState(outState: Bundle) {
        val position = binding.ssReadingViewPager.currentItem
        outState.putInt(ARG_POSITION, position)
        super.onSaveInstanceState(outState)
    }

    companion object {
        private const val ARG_POSITION = "arg:lesson_index"
    }
}
