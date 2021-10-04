/*
 * Copyright (c) 2021. Adventech <info@adventech.io>
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

package app.ss.pdf.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import com.cryart.sabbathschool.core.R
import com.cryart.sabbathschool.core.extensions.coroutines.flow.collectIn
import com.pspdfkit.document.DocumentSource
import com.pspdfkit.ui.DocumentDescriptor
import com.pspdfkit.ui.PdfActivity
import com.pspdfkit.ui.tabs.PdfTabBarCloseMode
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SSReadPdfActivity : PdfActivity() {

    private val viewModel by viewModels<ReadPdfViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.enter_from_right, android.R.anim.fade_out)

        initUi()

        collectData()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> true.also { finish() }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initUi() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        pspdfKitViews.tabBar?.setCloseMode(PdfTabBarCloseMode.CLOSE_DISABLED)
    }

    private fun collectData() {
        viewModel.pdfsFilesFlow.collectIn(this) { resource ->
            if (documentCoordinator.documents.isNotEmpty()) return@collectIn

            val documents = resource.data?.map { file ->
                DocumentDescriptor.fromDocumentSource(DocumentSource(file.uri)).apply {
                    setTitle(file.title)
                }
            } ?: return@collectIn

            if (documents.isEmpty()) return@collectIn

            documents.forEach { documentCoordinator.addDocument(it) }
            documentCoordinator.setVisibleDocument(documents.first())
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, R.anim.exit_to_right)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(android.R.anim.fade_in, R.anim.exit_to_right)
    }
}

internal const val ARG_PDF_FILES = "ss_arg_pdf_files"
