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

package ss.document.reader

import androidx.compose.runtime.Composable
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.components.SingletonComponent
import io.adventech.blockkit.ui.style.ReaderStyleConfig
import ss.document.producer.ReaderStyleStateProducer
import ss.document.reader.ReaderOptionsScreen.Event
import ss.document.reader.ReaderOptionsScreen.State
import ss.prefs.api.SSPrefs
import ss.prefs.model.SSReadingDisplayOptions

class ReaderOptionsPresenter @AssistedInject constructor(
    private val ssPrefs: SSPrefs,
    private val readerStyleStateProducer: ReaderStyleStateProducer,
) : Presenter<State> {

    @Composable
    override fun present(): State {
        val readerStyle = readerStyleStateProducer()

        return State(readerStyle) { event ->
            val displayOptions = readerStyle.toDisplayOptions()

            when (event) {
                is Event.OnThemeChanged -> {
                    ssPrefs.setDisplayOptions(displayOptions.copy(theme = event.theme.value))
                }

                is Event.OnTypefaceChanged -> {
                    ssPrefs.setDisplayOptions(displayOptions.copy(font = event.typeface.value))
                }

                is Event.OnFontSizeChanged -> {
                    ssPrefs.setDisplayOptions(displayOptions.copy(size = event.size.value))
                }
            }
        }
    }

    private fun ReaderStyleConfig.toDisplayOptions() = SSReadingDisplayOptions(
        theme = theme.value,
        font = typeface.value,
        size = size.value
    )

    @CircuitInject(ReaderOptionsScreen::class, SingletonComponent::class)
    @AssistedFactory
    interface Factory {
        fun create(): ReaderOptionsPresenter
    }
}
