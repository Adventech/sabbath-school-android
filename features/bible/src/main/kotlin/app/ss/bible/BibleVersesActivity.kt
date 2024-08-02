/*
 * Copyright (c) 2022. Adventech <info@adventech.io>
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
package app.ss.bible

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import app.ss.bible.databinding.SsBibleVersesActivityBinding
import com.cryart.sabbathschool.core.extensions.context.isDarkTheme
import com.cryart.sabbathschool.core.extensions.view.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.map
import ss.foundation.coroutines.flow.collectIn
import ss.misc.SSConstants
import ss.misc.SSEvent.track
import ss.prefs.model.SSReadingDisplayOptions
import ss.prefs.model.colorTheme

@AndroidEntryPoint
class BibleVersesActivity : AppCompatActivity(), ToolbarComponent.Callbacks {

    private val viewModel by viewModels<BibleVersesViewModel>()
    private val binding by viewBinding(SsBibleVersesActivityBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        track(this, SSConstants.SS_EVENT_BIBLE_OPEN)

        binding.root.setOnClickListener { onClose() }

        ToolbarComponent(
            composeView = binding.ssBibleVersesHeader,
            stateFlow = viewModel.uiState.map { it.toolbarState },
            this
        )

        binding.ssBibleVersesView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                val savedScrollY = savedInstanceState?.getInt(KEY_WEB_VIEW_SCROLL_POSITION) ?: return
                binding.ssBibleVersesView.run { post { scrollTo(0, savedScrollY) } }
            }
        }

        viewModel.uiState.collectIn(this, Lifecycle.State.CREATED) { uiState ->
            val options = uiState.displayOptions ?: SSReadingDisplayOptions(isDarkTheme())
            runOnUiThread {
                with(binding.ssBibleVersesView) {
                    setBackgroundColor(options.colorTheme(this@BibleVersesActivity.isDarkTheme()))
                    loadContent(uiState.content, options)
                }
            }
        }
    }

    override fun onClose() = finish()

    override fun versionSelected(version: String) {
        viewModel.versionSelected(version)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_WEB_VIEW_SCROLL_POSITION, binding.ssBibleVersesView.scrollY)
    }

    companion object {
        private const val KEY_WEB_VIEW_SCROLL_POSITION = "scrollY"

        fun launchIntent(
            context: Context,
            verse: String,
            readIndex: String
        ): Intent = Intent(
            context,
            BibleVersesActivity::class.java
        ).apply {
            putExtra(SSConstants.SS_READ_INDEX_EXTRA, readIndex)
            putExtra(SSConstants.SS_READ_VERSE_EXTRA, verse)
        }
    }
}
