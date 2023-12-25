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

package app.ss.storage.db

import app.ss.models.SSComment
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test

class ConvertersTest {

    @Test
    fun jsonToComments() {
        val json = """
            [
                {
                    "comment": "...",
                    "elementId": "input-0"
                }
            ]
        """.trimIndent()
        val comments = Converters.toComments(json)

        comments shouldBeEqualTo listOf(SSComment("input-0", "..."))
    }

    @Test
    fun nullJsonToComments() {
        val json = null
        val comments = Converters.toComments(json)

        comments shouldBeEqualTo null
    }

    @Test
    fun legacyJsonToComments() {
        val json = """
            [{"a":"input-0","b":"Legacy comment"}]
        """.trimIndent()

        val comments = Converters.toComments(json)

        comments shouldBeEqualTo listOf(SSComment("input-0", "Legacy comment"))
    }
}
