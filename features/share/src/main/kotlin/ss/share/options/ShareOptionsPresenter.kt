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

package ss.share.options

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import app.ss.design.compose.extensions.color.parse
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.produceRetainedState
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.presenter.Presenter
import dagger.Lazy
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.components.SingletonComponent
import io.adventech.blockkit.model.resource.ShareFileURL
import io.adventech.blockkit.model.resource.ShareLinkURL
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import ss.foundation.android.intent.ShareIntentHelper
import ss.libraries.circuit.navigation.ShareOptionsScreen
import timber.log.Timber

class ShareOptionsPresenter @AssistedInject constructor(
    @Assisted private val screen: ShareOptionsScreen,
    private val shareIntentHelper: Lazy<ShareIntentHelper>,
) : Presenter<ShareState> {

    private val shareGroups = screen.options.shareGroups

    @Composable
    override fun present(): ShareState {
        var selectedGroup by rememberRetained {
            mutableStateOf(shareGroups.firstOrNull { it.selected == true } ?: shareGroups.first())
        }
        var selectedShareLinkUrl by rememberRetained(selectedGroup) { mutableStateOf<ShareLinkURL?>(null) }
        var selectedShareFileUrl by rememberRetained(selectedGroup) { mutableStateOf<ShareFileURL?>(null) }
        val shareButtonState by produceRetainedState(
            initialValue = ShareButtonState.LOADING,
            key1 = selectedShareFileUrl,
            key2 = selectedShareLinkUrl,
        ) {
            buttonState(selectedShareLinkUrl, selectedShareFileUrl)
                .catch { Timber.e(it) }
                .collect { value = it }
        }

        return ShareState(
            segments = shareGroups.map { it.title },
            selectedGroup = selectedGroup,
            shareButtonState = shareButtonState,
            themeColor = screen.resourceColor?.takeUnless { it.isBlank() }?.let { Color.parse(it) },
            eventSink = { event ->
                when (event) {
                    is Event.OnSegmentSelected -> {
                        selectedGroup = shareGroups.firstOrNull { it.title == event.segment }
                            ?: shareGroups.first()
                    }

                    is Event.OnShareFileClicked -> {
                        selectedShareFileUrl = event.file
                    }

                    is Event.OnShareUrlSelected -> {
                        selectedShareLinkUrl = event.url
                    }

                    is Event.OnShareButtonClicked -> {
                        handleShareClick(event.context, selectedShareLinkUrl, selectedShareFileUrl)
                    }
                }
            }
        )
    }

    private fun buttonState(
        selectedLink: ShareLinkURL?,
        selectedFile: ShareFileURL?,
    ): Flow<ShareButtonState> {
        return when {
            selectedLink != null -> flowOf(ShareButtonState.ENABLED)
            selectedFile != null -> {
                shareIntentHelper.get()
                    .fileExists(selectedFile.src, selectedFile.fileName ?: screen.title)
                    .map { downloaded -> if (downloaded) ShareButtonState.ENABLED else ShareButtonState.DISABLED }
                    .onStart { emit(ShareButtonState.LOADING) }
            }

            else -> flowOf(ShareButtonState.DISABLED)
        }
    }

    private fun handleShareClick(
        context: Context,
        selectedLink: ShareLinkURL?,
        selectedFile: ShareFileURL?,
    ) {
        when {
            selectedLink != null -> {
                shareIntentHelper.get().shareText(
                    context = context,
                    text = selectedLink.src,
                    chooserTitle = screen.title
                )
            }

            selectedFile != null -> {
                shareIntentHelper.get().shareFile(
                    context = context,
                    fileUrl = selectedFile.src,
                    fileName = selectedFile.fileName ?: screen.title,
                    chooserTitle = screen.title
                )
            }

            else -> Timber.w("No shareable content selected")
        }
    }

    @CircuitInject(ShareOptionsScreen::class, SingletonComponent::class)
    @AssistedFactory
    interface Factory {
        fun create(screen: ShareOptionsScreen): ShareOptionsPresenter
    }
}
