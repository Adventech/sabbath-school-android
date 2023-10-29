/*
 * Copyright (c) 2023. Adventech <info@adventech.io>
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

package ss.workers.api

/** API to schedule background work. **/
interface WorkScheduler {

    /**
     * Prefetch cover images for [language] quarterlies.
     *
     * @param language The current selected language.
     */
    fun preFetchImages(language: String)

    /**
     * Schedules background work to download quarterly content matching the given [index].
     * Called specifically when the user clicks on download quarterly.
     * Scheduled on a CONNECTED network.
     *
     * @param index The quarterly index.
     */
    fun syncQuarterly(index: String)

    /**
     * Schedules background work to sync all quarterlies OfflineState.
     * Can be called periodically as user goes through the app and more content becomes available offline.
     * Does not make any network requests - sync is performed offline.
     */
    fun syncQuarterlies()
}
