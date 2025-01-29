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

package ss.document.segment.components.story

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.produceRetainedState
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.components.SingletonComponent
import io.adventech.blockkit.model.resource.Segment
import ss.document.segment.components.story.StorySegmentScreen.State
import ss.resources.api.ResourcesRepository

class StorySegmentPresenter @AssistedInject constructor(
    @Assisted private val screen: StorySegmentScreen,
    private val resourcesRepository: ResourcesRepository,
): Presenter<State> {

    @Composable
    override fun present(): State {
        val result by rememberSegment()
        val segment = result

        val story = segment?.blocks?.firstOrNull()

        return when {
            story == null -> State.Loading
            else -> State.Content(story)
        }
    }

    @Composable
    private fun rememberSegment() = produceRetainedState<Segment?>(null) {
        resourcesRepository.segment(screen.segmentId).collect { value = it }
    }

    @CircuitInject(StorySegmentScreen::class, SingletonComponent::class)
    @AssistedFactory
    interface Factory {
        fun create(screen: StorySegmentScreen): StorySegmentPresenter
    }
}
