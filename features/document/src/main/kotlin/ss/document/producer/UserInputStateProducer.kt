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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import com.slack.circuit.retained.produceRetainedState
import io.adventech.blockkit.model.input.UserInput
import io.adventech.blockkit.model.input.UserInputRequest
import io.adventech.blockkit.ui.input.UserInputState
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import ss.resources.api.ResourcesRepository
import javax.inject.Inject

@Stable
interface UserInputStateProducer {

    @Composable
    operator fun invoke(documentId: String?): UserInputState
}

internal class UserInputStateProducerImpl @Inject constructor(
    private val resourcesRepository: ResourcesRepository,
) : UserInputStateProducer {

    private val _userInputRequest = MutableStateFlow<UserInputRequest?>(null)

    @OptIn(FlowPreview::class)
    @Composable
    override fun invoke(documentId: String?): UserInputState {
        val input by produceRetainedState(emptyList<UserInput>(), documentId) {
            documentId?.let { resourcesRepository.documentInput(it).collect { value = it } }
        }

        val updatedUserInput by produceRetainedState<UserInputRequest?>(null) {
            _userInputRequest
                .debounce(2000)
                .distinctUntilChanged()
                .collect { value = it }
        }

        LaunchedEffect(updatedUserInput) {
            updatedUserInput?.let {
                documentId?.let { id ->
                    resourcesRepository.saveDocumentInput(id, it)
                }
            }
        }

        return UserInputState(
            input = input.toImmutableList(),
            eventSink = { event ->
                when (event) {
                    is UserInputState.Event.InputChanged -> {
                        val input = event.input
                        _userInput.value = input
                    }
                }
            }
        )
    }
}
