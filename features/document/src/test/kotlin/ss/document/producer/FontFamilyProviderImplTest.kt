/*
 * Copyright (c) 2025. Adventech <info@adventech.io>
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

package ss.document.producer

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import app.cash.turbine.test
import io.adventech.blockkit.model.resource.ResourceFontAttributes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import ss.resources.api.test.FakeResourcesRepository
import ss.resources.model.FontModel
import java.io.File

@ExperimentalCoroutinesApi
class FontFamilyProviderImplTest {
    private val testDispatcher = StandardTestDispatcher()
    private val fakeRepository = FakeResourcesRepository()

    private val underTest = FontFamilyProviderImpl(
        repository = fakeRepository,
    )

    private val defaultFontFamily = FontFamily.SansSerif

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `returns custom FontFamily when file is valid`() = runTest {
        val tempFile = createTempFontFile("valid-font")
        fakeRepository.setFont("CustomFont", FontModel(tempFile, ResourceFontAttributes.BOLD))

        underTest("CustomFont", defaultFontFamily).test {
            assertNotEquals(defaultFontFamily, awaitItem())
            awaitComplete()
        }

        tempFile.delete()
    }

    @Test
    fun `returns default FontFamily when file is missing`() = runTest {
        underTest("MissingFont", defaultFontFamily).test {
            assertEquals(defaultFontFamily, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `returns default FontFamily when file is invalid`() = runTest {
        val emptyFile = File.createTempFile("invalid", ".ttf")
        // File is empty by default
        fakeRepository.setFont("InvalidFont", FontModel(emptyFile, ResourceFontAttributes.REGULAR))

        underTest("InvalidFont", defaultFontFamily).test {
            assertEquals(defaultFontFamily, awaitItem())
            awaitComplete()
        }

        emptyFile.delete()
    }

    @Test
    fun `style function maps ResourceFontAttributes to expected weight and style`() {
        val (weight, style) = ResourceFontAttributes.BOLD_ITALIC.style()

        assertEquals(FontWeight.Bold, weight)
        assertEquals(FontStyle.Italic, style)
    }

    private fun createTempFontFile(name: String): File {
        val file = File.createTempFile(name, ".ttf")
        file.writeBytes(byteArrayOf(0x00, 0x01, 0x00, 0x00)) // minimal header-like bytes
        return file
    }

}
