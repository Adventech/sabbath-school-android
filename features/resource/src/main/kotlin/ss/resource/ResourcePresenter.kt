/*
 * Copyright (c) 2024. Adventech <info@adventech.io>
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

package ss.resource

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.produceRetainedState
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.Lazy
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.components.SingletonComponent
import io.adventech.blockkit.model.resource.ProgressTracking
import io.adventech.blockkit.model.resource.Resource
import io.adventech.blockkit.model.resource.ShareGroup
import io.adventech.blockkit.ui.style.font.FontFamilyProvider
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import ss.foundation.android.intent.ShareIntentHelper
import ss.libraries.circuit.navigation.ResourceScreen
import ss.resource.components.content.ResourceSectionsStateProducer
import ss.resource.components.spec.SharePosition
import ss.resource.components.spec.toSpec
import ss.resource.producer.CtaScreenState
import ss.resource.producer.ResourceCtaScreenProducer
import ss.resources.api.ResourcesRepository

class ResourcePresenter @AssistedInject constructor(
    @Assisted private val navigator: Navigator,
    @Assisted private val screen: ResourceScreen,
    private val resourcesRepository: ResourcesRepository,
    private val resourceSectionStateProducer: ResourceSectionsStateProducer,
    private val resourceCtaScreenProducer: ResourceCtaScreenProducer,
    private val fontFamilyProvider: FontFamilyProvider,
    private val shareIntentHelper: Lazy<ShareIntentHelper>,
) : Presenter<State> {

    @Composable
    override fun present(): State {
        val resourceResponse by rememberResource()
        var title by rememberRetained(resourceResponse) { mutableStateOf(resourceResponse?.title ?: "") }

        val resource = resourceResponse
        val credits = rememberRetained(resource) { resource?.credits?.map { it.toSpec() }?.toImmutableList() ?: persistentListOf() }
        val features = rememberRetained(resource) { resource?.features?.map { it.toSpec() }?.toImmutableList() ?: persistentListOf() }
        val sections = resource?.let { resourceSectionStateProducer(navigator, it) }?.specs ?: persistentListOf()
        val ctaScreen = resourceCtaScreenProducer(resource)
        val readDocumentTitle = rememberRetained(resource, ctaScreen) {
            (ctaScreen as? CtaScreenState.Default)?.title?.takeIf {
                resource?.progressTracking in setOf(ProgressTracking.AUTOMATIC, ProgressTracking.AUTOMATIC)
            } ?: ""
        }
        val sharePosition by rememberRetained(resource?.share) {
            mutableStateOf(SharePosition.fromResource(resource))
        }

        var overlayState by rememberRetained { mutableStateOf<ResourceOverlayState?>(null) }

        val eventSink: (Event) -> Unit = { event ->
            when (event) {
                Event.OnNavBack -> navigator.pop()
                Event.OnCtaClick -> {
                    when (ctaScreen) {
                        is CtaScreenState.Default -> navigator.goTo(ctaScreen.screen)
                        CtaScreenState.None -> Unit
                    }
                }
                Event.OnReadMoreClick -> {
                    (resource?.introduction ?: resource?.markdownDescription ?: resource?.description)?.let {
                        overlayState = ResourceOverlayState.IntroductionBottomSheet(it) { result ->
                            overlayState = null
                        }
                    }
                }

                is Event.OnShareClick -> {
                    resource?.share?.let { options ->
                        val shareGroups = options.shareGroups
                        val linkGroup = shareGroups.firstOrNull()
                        if (shareGroups.size == 1 && linkGroup is ShareGroup.Link && linkGroup.links.size == 1) {
                            val shareLink = linkGroup.links.first().src
                            shareIntentHelper.get().shareText(event.context, shareLink)
                        } else {
                            overlayState = ResourceOverlayState.ShareBottomSheet(
                                options = options,
                                primaryColorDark = resource.primaryColorDark,
                                title = resource.title,
                                onResult = { overlayState = null }
                            )
                        }
                    }
                }
            }
        }

        return when {
            resource != null -> State.Success(
                title = title,
                readDocumentTitle = readDocumentTitle.takeUnless { it.isBlank() },
                resource = resource,
                sections = sections,
                credits = credits,
                features = features,
                fontFamilyProvider = fontFamilyProvider,
                overlayState = overlayState,
                eventSink = eventSink,
                sharePosition = sharePosition,
                primaryColorDark = resource.primaryColorDark
            )

            else -> State.Loading(
                title = title,
                eventSink = eventSink
            )
        }
    }

    @Composable
    private fun rememberResource() = produceRetainedState<Resource?>(null) {
        resourcesRepository.resource(screen.index).collect { value = it }
    }

    @CircuitInject(ResourceScreen::class, SingletonComponent::class)
    @AssistedFactory
    interface Factory {
        fun create(navigator: Navigator, screen: ResourceScreen): ResourcePresenter
    }
}
