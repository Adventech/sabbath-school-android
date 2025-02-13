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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import com.slack.circuit.retained.produceRetainedState
import io.adventech.blockkit.ui.style.ReaderStyle
import io.adventech.blockkit.ui.style.ReaderStyleConfig
import kotlinx.coroutines.flow.flowOn
import ss.foundation.coroutines.DispatcherProvider
import ss.prefs.api.SSPrefs
import ss.prefs.model.SSReadingDisplayOptions
import javax.inject.Inject

@Stable
interface ReaderStyleStateProducer {
    @Composable
    operator fun invoke(): ReaderStyleConfig
}

internal class ReaderStyleStateProducerImpl @Inject constructor(
    private val ssPrefs: SSPrefs,
    private val dispatcherProvider: DispatcherProvider,
) : ReaderStyleStateProducer {

    @Composable
    override fun invoke(): ReaderStyleConfig {
        val displayOptions by produceRetainedState(
            SSReadingDisplayOptions(
                theme = ReaderStyle.Theme.Auto.value,
                font = ReaderStyle.Typeface.Lato.value,
                size = ReaderStyle.Size.Medium.value
            )
        ) {
            ssPrefs.displayOptionsFlow()
                .flowOn(dispatcherProvider.io)
                .collect { value = it }
        }

        return displayOptions.toReaderStyle()
    }

    private fun SSReadingDisplayOptions.toReaderStyle(): ReaderStyleConfig = ReaderStyleConfig(
        theme = ReaderStyle.Theme.from(theme),
        typeface = ReaderStyle.Typeface.from(font),
        size = ReaderStyle.Size.from(size)
    )
}
