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
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import app.ss.bible.databinding.SsBibleVersesActivityBinding
import com.cryart.sabbathschool.core.extensions.context.isDarkTheme
import com.cryart.sabbathschool.core.extensions.coroutines.flow.collectIn
import com.cryart.sabbathschool.core.extensions.view.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.map
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

        viewModel.uiState.collectIn(this) { uiState ->
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

    companion object {
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
