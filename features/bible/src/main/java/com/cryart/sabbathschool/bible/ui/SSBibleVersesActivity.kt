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
package com.cryart.sabbathschool.bible.ui

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.cryart.sabbathschool.bible.databinding.SsBibleVersesActivityBinding
import com.cryart.sabbathschool.core.extensions.context.colorPrimary
import com.cryart.sabbathschool.core.misc.SSConstants
import com.cryart.sabbathschool.core.misc.SSEvent.track
import com.cryart.sabbathschool.reader.data.model.SSBibleVerses
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SSBibleVersesActivity : AppCompatActivity() {
    private val binding: SsBibleVersesActivityBinding by lazy { SsBibleVersesActivityBinding.inflate(layoutInflater) }
    private val viewModel: SSBibleVersesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        track(this, SSConstants.SS_EVENT_BIBLE_OPEN)

        val verse = intent.extras!!.getString(SSConstants.SS_READ_VERSE_EXTRA)
        val readIndex = intent.extras!!.getString(SSConstants.SS_READ_INDEX_EXTRA)!!

        binding.bibleCloseIV.setOnClickListener { finish() }
        binding.root.setOnClickListener { finish() }

        binding.ssBibleVersesHeader.setBackgroundColor(this.colorPrimary)
        binding.ssReadingBibleVersionList.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                val ssBibleVerses = adapterView.getItemAtPosition(i) as SSBibleVerses
                viewModel.setLastBibleUsed(ssBibleVerses.name)
                binding.ssBibleVersesView.loadContent(ssBibleVerses.verses[verse]!!, viewModel.getDisplayOptions())
            }

            override fun onNothingSelected(arg0: AdapterView<*>?) {}
        }

        viewModel.verses.observe(this) {
            binding.ssReadingBibleVersionList.adapter = SSBibleVersionsAdapter(it)
            val lastBibleVersionUsed = viewModel.getLastBibleUsed()
            var pos = 0
            if (lastBibleVersionUsed != null) {
                pos = it.indexOfFirst { verse -> verse.name.equals(lastBibleVersionUsed, ignoreCase = true) }
                if (pos == -1) {
                    pos = 0
                }
            }
            binding.ssReadingBibleVersionList.setSelection(pos)
        }

        viewModel.requestVerses(readIndex)
    }
}
