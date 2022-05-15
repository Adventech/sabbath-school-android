/*
 * Copyright (c) 2021. Adventech <info@adventech.io>
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

package com.cryart.sabbathschool.lessons.ui.quarterlies.components

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import coil.imageLoader
import coil.request.ImageRequest
import coil.size.Size
import coil.transform.CircleCropTransformation
import com.cryart.sabbathschool.core.extensions.coroutines.flow.collectIn
import com.cryart.sabbathschool.core.navigation.AppNavigator
import com.cryart.sabbathschool.core.navigation.Destination
import com.cryart.sabbathschool.core.ui.BaseDataComponent
import com.cryart.sabbathschool.lessons.R
import com.cryart.sabbathschool.lessons.databinding.SsQuarterliesAppBarBinding
import kotlinx.coroutines.flow.Flow

sealed interface QuarterliesAppbarData {
    data class Photo(val uri: Uri?) : QuarterliesAppbarData
    data class Title(val title: String?) : QuarterliesAppbarData
    object Empty : QuarterliesAppbarData
}

class QuarterliesAppbarComponent(
    private val activity: AppCompatActivity,
    private val binding: SsQuarterliesAppBarBinding,
    private val appNavigator: AppNavigator? = null
) : BaseDataComponent<QuarterliesAppbarData>(activity) {

    init {
        activity.setSupportActionBar(binding.ssToolbar)
        binding.ssToolbar.apply {
            appNavigator?.let {
                setNavigationIcon(R.drawable.ic_account_circle)
                setNavigationOnClickListener {
                    appNavigator.navigate(activity, Destination.ACCOUNT)
                }
            } ?: run {
                activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
            }
        }
    }

    override fun collect(dataFlow: Flow<QuarterliesAppbarData>) {
        dataFlow.collectIn(owner) { data ->
            when (data) {
                QuarterliesAppbarData.Empty -> {}
                is QuarterliesAppbarData.Photo -> {
                    val size = activity.resources.getDimensionPixelSize(R.dimen.spacing_large)
                    val request = ImageRequest.Builder(activity)
                        .size(Size(size, size))
                        .transformations(CircleCropTransformation())
                        .data(data.uri)
                        .error(R.drawable.ic_account_circle)
                        .placeholder(R.drawable.ic_account_circle)
                        .target {
                            binding.ssToolbar.navigationIcon = it
                        }
                        .build()
                    activity.imageLoader.enqueue(request)
                }
                is QuarterliesAppbarData.Title -> {
                    binding.ssToolbarLayout.title = data.title
                }
            }
        }
    }
}
