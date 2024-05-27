/*
 * Copyright (c) 2024. Adventech <info@adventech.io>
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
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package ss.workers.api.test

import androidx.annotation.VisibleForTesting
import ss.workers.api.WorkScheduler

@VisibleForTesting
class FakeWorkScheduler : WorkScheduler {

    var preFetchImagesLanguage: String? = null
        private set
    var preFetchImagesImages: Set<String>? = null
        private set
    var quarterlySyncIndex: String? = null
        private set

    override fun preFetchImages(language: String) {
        this.preFetchImagesLanguage = language
    }

    override fun preFetchImages(images: Set<String>) {
        this.preFetchImagesImages = images
    }

    override fun syncQuarterly(index: String) {
        quarterlySyncIndex = index
    }

    override fun syncQuarterlies() {
        // no-op
    }
}
