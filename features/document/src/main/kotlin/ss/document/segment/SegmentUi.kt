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

package ss.document.segment

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import app.ss.models.PDFAux
import com.slack.circuit.foundation.CircuitContent
import com.slack.circuit.foundation.NavEvent
import io.adventech.blockkit.model.BlockData
import io.adventech.blockkit.model.BlockItem
import io.adventech.blockkit.model.resource.ReferenceModel
import io.adventech.blockkit.model.resource.Segment
import io.adventech.blockkit.model.resource.SegmentType
import io.adventech.blockkit.ui.input.UserInputState
import ss.document.segment.components.blocks.SegmentBlocksContent
import ss.document.segment.components.story.StorySegmentUi
import ss.document.segment.components.video.VideoSegmentScreen
import ss.libraries.circuit.navigation.PdfScreen

@Composable
fun SegmentUi(
    segment: Segment,
    documentId: String,
    titleBelowCover: Boolean,
    userInputState: UserInputState,
    modifier: Modifier = Modifier,
    onNavBack: () -> Unit = {},
    onCollapseChange: (Boolean) -> Unit = {},
    onHandleUri: (String, BlockData?) -> Unit = { _, _ -> },
    onHandleReference: (ReferenceModel) -> Unit = { _ -> },
    onNavEvent: (NavEvent) -> Unit = {},
) {
    when (segment.type) {
        SegmentType.UNKNOWN -> Unit
        SegmentType.BLOCK -> {
            SegmentBlocksContent(
                segment,
                titleBelowCover,
                modifier,
                userInputState,
                onCollapseChange,
                onHandleUri,
                onHandleReference,
            )
        }

        SegmentType.STORY -> {
            (segment.blocks?.firstOrNull() as? BlockItem.Story)?.let {
                StorySegmentUi(
                    story = it,
                    modifier = modifier,
                    onNavBack = onNavBack,
                    onCollapseChange = onCollapseChange,
                    onHandleUri = onHandleUri,
                )
            }
        }

        SegmentType.PDF -> {
            val pdfs = remember(segment) {
                segment.pdf.orEmpty().map {
                    PDFAux(
                        id = it.id,
                        src = it.src,
                        title = it.title,
                        target = it.target,
                        targetIndex = it.targetIndex,
                    )
                }
            }

            CircuitContent(
                screen = PdfScreen(pdfs),
                modifier = modifier,
                onNavEvent = onNavEvent,
            )
        }

        SegmentType.VIDEO -> {
            segment.video?.let {
                CircuitContent(
                    screen = VideoSegmentScreen(segment.id, documentId),
                    modifier = modifier,
                    onNavEvent = onNavEvent,
                )
            }
        }
    }
}
