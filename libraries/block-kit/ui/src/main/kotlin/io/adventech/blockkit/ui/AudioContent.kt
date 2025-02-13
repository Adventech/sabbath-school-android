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

package io.adventech.blockkit.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.adventech.blockkit.model.AudioBlockCredits
import io.adventech.blockkit.model.BlockItem
import io.adventech.blockkit.model.BlockStyle
import io.adventech.blockkit.ui.media.MediaPlayer
import io.adventech.blockkit.ui.style.LocalReaderStyle
import io.adventech.blockkit.ui.style.Styler
import io.adventech.blockkit.ui.style.Styler.genericBackgroundColorForInteractiveBlock
import io.adventech.blockkit.ui.style.Styler.genericForegroundColorForInteractiveBlock
import io.adventech.blockkit.ui.style.background
import io.adventech.blockkit.ui.style.primaryForeground
import io.adventech.blockkit.ui.style.theme.BlocksPreviewTheme
import ss.libraries.media.model.PlaybackProgressState
import ss.libraries.media.model.extensions.millisToDuration
import ss.services.media.ui.PlaybackPlayPause
import ss.services.media.ui.common.PlaybackSlider
import ss.services.media.ui.spec.PlaybackStateSpec

@Composable
fun AudioContent(blockItem: BlockItem.Audio, modifier: Modifier = Modifier) {
    MediaPlayer(
        source = blockItem.src,
        modifier = modifier,
    ) { _, playbackState, progressState, onPlayPause, onSeekTo ->
        PlayerContent(
            playbackState = playbackState,
            progressState = progressState,
            modifier = Modifier,
            onPlayPause = onPlayPause,
            onSeekTo = onSeekTo
        )

        Spacer(
            Modifier
                .fillMaxWidth()
                .height(8.dp)
        )

        PlayerCaption(
            caption = blockItem.caption,
            modifier = Modifier,
            blockStyle = blockItem.style,
        )

        blockItem.credits?.let {
            PlayerCredits(
                credits = it,
                modifier = Modifier,
                blockStyle = blockItem.style,
            )
        }
    }
}

@Composable
private fun PlayerContent(
    playbackState: PlaybackStateSpec,
    progressState: PlaybackProgressState,
    modifier: Modifier = Modifier,
    onPlayPause: () -> Unit = {},
    onSeekTo: (Long) -> Unit = {},
) {
    val contentColor = genericForegroundColorForInteractiveBlock()

    val (draggingProgress, setDraggingProgress) = remember { mutableStateOf<Float?>(null) }

    val currentDuration = when (draggingProgress != null) {
        true -> (progressState.total.toFloat() * (draggingProgress)).toLong().millisToDuration()
        else -> progressState.currentDuration
    }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = genericBackgroundColorForInteractiveBlock(),
            contentColor = contentColor
        ),
        shape = Styler.roundedShape(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlaybackPlayPause(
                spec = playbackState,
                contentColor = contentColor,
                onPlayPause = onPlayPause,
            )

            Text(
                currentDuration,
                style = Styler.textStyle(null).copy(
                    fontFamily = Styler.defaultFontFamily(),
                    fontSize = 14.sp
                ),
                color = contentColor
            )

            PlaybackSlider(
                isBuffering = playbackState.isBuffering,
                color = contentColor,
                progressState = progressState,
                draggingProgress = draggingProgress,
                setDraggingProgress = setDraggingProgress,
                onSeekTo = onSeekTo,
                modifier = Modifier.weight(1f),
            )

            Spacer(Modifier.size(4.dp))
        }
    }
}

@Composable
private fun PlayerCaption(
    caption: String?,
    modifier: Modifier = Modifier,
    blockStyle: BlockStyle? = null
) {
    caption?.let {
        Text(
            text = it,
            modifier = modifier.fillMaxWidth(),
            style = Styler.textStyle(blockStyle?.text).copy(
                fontFamily = Styler.defaultFontFamily(),
                fontSize = 14.sp,
                fontStyle = FontStyle.Italic
            ),
            color = genericForegroundColorForInteractiveBlock(),
            textAlign = TextAlign.Center,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayerCredits(
    credits: AudioBlockCredits,
    modifier: Modifier = Modifier,
    blockStyle: BlockStyle? = null
) {
    val contentColor = genericForegroundColorForInteractiveBlock()
    val textStyle = Styler.textStyle(blockStyle?.text).copy(
        fontFamily = Styler.defaultFontFamily(),
        fontSize = 14.sp,
        fontStyle = FontStyle.Normal,
        color = contentColor
    )

    var openBottomSheet by rememberSaveable { mutableStateOf(false) }

    val annotatedText = remember {
        buildAnnotatedString {
            withLink(
                link = LinkAnnotation.Clickable(
                    tag = "TAG",
                    styles = TextLinkStyles(
                        style = textStyle
                            .copy(textDecoration = TextDecoration.Underline)
                            .toSpanStyle()
                    ),
                    linkInteractionListener = {
                        openBottomSheet = true
                    }
                )
            ) {
                append(credits.title?.takeUnless { it.isBlank() } ?: "Credits")
            }
        }
    }
    Text(
        text = annotatedText,
        modifier = modifier.fillMaxWidth(),
        style = textStyle,
        color = contentColor,
        textAlign = TextAlign.Center,
    )

    if (openBottomSheet) {
        CreditsContent(credits) { openBottomSheet = false }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreditsContent(credits: AudioBlockCredits, onDismiss: () -> Unit) {
    val readerStyle = LocalReaderStyle.current
    val backgroundColor = readerStyle.theme.background()
    val contentColor = readerStyle.theme.primaryForeground()
    val textStyle = Styler.textStyle(null).copy(
        fontFamily = Styler.defaultFontFamily(),
        fontSize = 14.sp,
        fontStyle = FontStyle.Normal,
        color = contentColor
    )

    CompositionLocalProvider(LocalReaderStyle provides readerStyle) {
        val sheetState = rememberModalBottomSheetState()

        ModalBottomSheet(
            onDismissRequest = { onDismiss() },
            modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top)),
            sheetState = sheetState,
            containerColor = backgroundColor,
            contentColor = contentColor,
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    Text(
                        text = credits.title?.takeUnless { it.isBlank() } ?: "Credits",
                        style = textStyle.copy(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = contentColor,
                    )
                }

                items(credits.credits) { item ->
                    Text(
                        text = item.key,
                        modifier = Modifier,
                        style = textStyle.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = contentColor,
                    )

                    Text(
                        text = item.value,
                        modifier = Modifier,
                        style = textStyle.copy(
                            fontSize = 18.sp,
                        ),
                        color = contentColor,
                    )
                }

                item {
                    credits.copyright?.let {
                        Text(
                            text = it,
                            modifier = Modifier.fillMaxWidth(),
                            style = textStyle.copy(
                                fontSize = 15.sp,
                                fontStyle = FontStyle.Italic,
                            ),
                            color = contentColor.copy(alpha = 0.7f),
                        )
                    }
                }

                item {
                    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.systemBars))
                }
            }
        }
    }
}

@Composable
@PreviewLightDark
private fun Preview() {
    BlocksPreviewTheme {
        Surface {
            PlayerContent(
                playbackState = PlaybackStateSpec.NONE.copy(
                    isPlayEnabled = true
                ),
                progressState = PlaybackProgressState(
                    total = 3 * 60 * 1000,
                    position = 60 * 1000
                ),
            )
        }
    }
}
