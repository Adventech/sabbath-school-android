/*
 * Copyright (c) 2022. Adventech <info@adventech.io>
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


package com.cryart.sabbathschool.lessons.ui.quarterlies.list

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.ss.design.compose.theme.SsTheme
import kotlin.math.roundToInt

private enum class BoxState {
    Collapsed,
    Expanded
}

@Composable
fun AnimPlaygroundScreen() {

    var contentState by remember { mutableStateOf(BoxState.Collapsed) }

    SsTheme {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        contentState = when (contentState) {
                            BoxState.Collapsed -> BoxState.Expanded
                            BoxState.Expanded -> BoxState.Collapsed
                        }
                    },
            ) {

                Spacer(
                    modifier = Modifier
                        .height(300.dp)
                        .background(Color.LightGray)
                )

                PlayerSample(
                    boxState = contentState,
                    modifier = Modifier.weight(1f)
                )

            }
        }
    }

}

@Composable
private fun PlayerSample(
    boxState: BoxState,
    modifier: Modifier = Modifier
) {
    val expanded = boxState == BoxState.Expanded
    val imageWidth by animateDpAsState(
        targetValue = if (expanded) 100.dp / 2 else 100.dp,
        animationSpec = tween(
            durationMillis = 600,
        )
    )
    val imageHeight by animateDpAsState(
        targetValue = if (expanded) 200.dp / 2 else 200.dp,
        animationSpec = tween(
            durationMillis = 600,
        )
    )

    Layout(
        content = {

            Spacer(
                modifier = Modifier
                    .size(imageWidth, imageHeight)
                    .background(Color.Blue, RoundedCornerShape(8.dp))
            )

            Text(
                text = "Hello World - title",
                style = MaterialTheme.typography.titleSmall,
                textAlign = if (expanded) TextAlign.Start else TextAlign.Center,
            )

            Text(
                text = "Adult Bible Study guides",
                style = MaterialTheme.typography.bodySmall,
                textAlign = if (expanded) TextAlign.Start else TextAlign.Center,
            )

        }, modifier = modifier
    ) { measurables, constraints ->
        /*val image = measurables[0].measure(constraints)
        val title = measurables[1].measure(constraints)
        val desc = measurables[2].measure(constraints)

        layout(constraints.maxWidth, constraints.maxHeight) {
            if(expanded) {
                image.place(0, 0)
            } else {
                image.place(
                    (constraints.maxWidth - imageWidth) / 2,
                    (constraints.maxHeight - imageHeight) / 2
                )
            }
            title.place(image.width, 0)
            desc.place(image.width, title.height)
        }*/

        val placeables = measurables.map { measurable ->
            // Measure each children
            measurable.measure(constraints)
        }

        // Set the size of the layout as big as it can
        layout(constraints.maxWidth, constraints.maxHeight) {
            // Track the y co-ord we have placed children up to
            var yPosition = 0

            // Place children in the parent layout
            placeables.forEach { placeable ->
                // Position item on the screen
                placeable.placeRelative(x = 0, y = yPosition)

                // Record the y co-ord placed up to
                yPosition += placeable.height
            }
        }
    }
}

private val width: Dp = 187.dp
private val height: Dp = 276.dp

enum class AlignmentState { Rowed, Columned }

@Composable
fun Sample(state: AlignmentState) {
    //Dimensions of the static/stable box
    val redWidth = 50
    val redHeight = 50

    //Animated Coordinates of the dynamic box
    val blueX by animateDpAsState(targetValue = (if (state == AlignmentState.Rowed) (redWidth * 1.25f).roundToInt() else 0).dp)
    val blueY by animateDpAsState(targetValue = (if (state == AlignmentState.Rowed) 0 else redHeight).dp)

    Layout(content = {
        Box(
            modifier = Modifier
                .height(redWidth.dp)
                .width(redHeight.dp)
                .background(Color.Red)
        )
        Box(
            modifier = Modifier
                .height(50.dp)
                .width(50.dp)
                .background(Color.Blue)
        )
    }) { measurables, constraints ->
        val red = measurables[0].measure(constraints)
        val blue = measurables[1].measure(constraints)

        layout(constraints.maxWidth, constraints.maxHeight) {
            red.place(0, 0)
            blue.place(blueX.value.roundToInt(), blueY.value.roundToInt())
        }

    }

}


