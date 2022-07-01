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

package com.cryart.sabbathschool.lessons.ui.lessons.components

import android.annotation.SuppressLint
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import app.ss.design.compose.extensions.color.parse
import app.ss.design.compose.extensions.modifier.thenIf
import app.ss.design.compose.theme.SsColor
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.widget.button.ButtonSpec
import app.ss.design.compose.widget.button.SsButton
import app.ss.design.compose.widget.button.SsButtonColors
import app.ss.design.compose.widget.content.ContentBox
import app.ss.design.compose.widget.image.RemoteImage
import app.ss.models.SSQuarterlyInfo
import com.cryart.design.color.withAlpha
import com.cryart.sabbathschool.core.extensions.coroutines.flow.collectIn
import com.cryart.sabbathschool.core.extensions.view.addMoreEllipses
import com.cryart.sabbathschool.core.misc.DateHelper
import com.cryart.sabbathschool.core.ui.BaseComponent
import com.cryart.sabbathschool.lessons.R
import com.cryart.sabbathschool.lessons.databinding.SsLessonsQuarterlyInfoBinding
import com.cryart.sabbathschool.lessons.ui.base.loadCover
import com.cryart.sabbathschool.lessons.ui.lessons.components.spec.QuarterlyInfoSpec
import com.cryart.sabbathschool.lessons.ui.lessons.intro.LessonIntroModel
import com.cryart.sabbathschool.lessons.ui.lessons.intro.showLessonIntro
import com.cryart.sabbathschool.lessons.ui.readings.SSReadingActivity
import kotlinx.coroutines.flow.Flow
import org.joda.time.DateTime
import org.joda.time.Interval
import android.graphics.Color as AndroidColor

internal class QuarterlyInfoComponent(
    lifecycleOwner: LifecycleOwner,
    private val fragmentManager: FragmentManager,
    private val binding: SsLessonsQuarterlyInfoBinding,
    private val lessonsCallback: LessonsCallback,
) : BaseComponent<SSQuarterlyInfo?>(lifecycleOwner) {

    override fun collect(visibilityFlow: Flow<Boolean>, dataFlow: Flow<SSQuarterlyInfo?>) {
        visibilityFlow.collectIn(owner) { visible ->
            binding.root.isVisible = visible
        }

        dataFlow.collectIn(owner) { data ->
            data?.let { quarterlyInfo ->
                setQuarterlyInfo(quarterlyInfo)

                val today = DateTime.now().withTimeAtStartOfDay()
                val todayLessonIndex = quarterlyInfo.lessons.find { lesson ->
                    val startDate = DateHelper.parseDate(lesson.start_date)
                    val endDate = DateHelper.parseDate(lesson.end_date)
                    Interval(startDate, endDate?.plusDays(1)).contains(today)
                }?.index ?: quarterlyInfo.lessons.firstOrNull()?.index

                binding.root.findViewById<View?>(R.id.ss_lessons_app_bar_read)
                    ?.setOnClickListener { view ->
                        todayLessonIndex?.let index@{ index ->
                            val lesson = quarterlyInfo.lessons.firstOrNull { it.index == index } ?: return@index
                            if (lesson.pdfOnly) {
                                lessonsCallback.openPdf(lesson)
                            } else {
                                val context = view.context
                                val ssReadingIntent = SSReadingActivity.launchIntent(context, index)
                                context.startActivity(ssReadingIntent)
                            }
                        }
                    }
            }
        }
    }

    @SuppressLint("Range")
    private fun setQuarterlyInfo(quarterlyInfo: SSQuarterlyInfo) {
        val quarterly = quarterlyInfo.quarterly
        val introModel = LessonIntroModel(
            quarterly.title,
            quarterly.introduction ?: quarterly.description
        )
        val primaryColor = AndroidColor.parseColor(quarterly.color_primary)
        val primaryDarkColor = AndroidColor.parseColor(quarterly.color_primary_dark)

        binding.root.apply {
            quarterly.splash?.let { url ->
                findViewById<ImageView?>(R.id.ss_quarterly_splash)
                    ?.loadCover(url, primaryColor)

                val array = arrayListOf(
                    primaryDarkColor,
                    primaryColor,
                    AndroidColor.BLACK.withAlpha(40),
                    AndroidColor.TRANSPARENT
                )
                val background = GradientDrawable(
                    GradientDrawable.Orientation.BOTTOM_TOP,
                    array.toIntArray()
                )
                findViewById<View?>(R.id.ss_quarterly_splash_gradient)?.background = background
            } ?: run {
                setBackgroundColor(primaryColor)
                findViewById<ImageView?>(R.id.ss_lessons_app_bar_cover)
                    ?.loadCover(quarterly.cover, primaryDarkColor)
            }

            findViewById<TextView>(R.id.ss_lessons_app_bar_description)?.apply {
                text = quarterly.description
                addMoreEllipses(
                    3,
                    R.string.ss_more,
                    R.color.text_link
                )
                setOnClickListener {
                    fragmentManager.showLessonIntro(introModel)
                }
            }
        }
    }
}

// view.setContent {
//    QuarterlyFeaturesRow(spec = QuarterlyFeaturesSpec(features))
// }
// view.isVisible = features.isNotEmpty()

@Composable
fun QuarterlyInfo(
    spec: QuarterlyInfoSpec,
    modifier: Modifier = Modifier,
) {

    val content: @Composable ColumnScope.() -> Unit = {
        Content(
            title = spec.title,
            date = spec.date,
            description = spec.description,
            primaryDarkColor = Color.parse(spec.colorDark)
        )
    }

    CoverBox(
        color = spec.color,
        splashImage = spec.splashImage,
        contentDescription = spec.title,
        modifier = modifier
    ) {
        if (spec.splashImage.isNullOrEmpty()) {
            ContentSecondary(
                spec = spec,
                content = content
            )
        } else {
            ContentPrimary(
                content = content,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun CoverBox(
    color: String,
    splashImage: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .background(Color.parse(color))
            .thenIf(splashImage != null) {
                Modifier.height(480.dp)
            },
    ) {
        ContentBox(
            content = RemoteImage(
                data = splashImage,
                contentDescription = contentDescription,
            ),
            modifier = Modifier
        )

        content()
    }
}

@Composable
private fun ContentPrimary(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {

    ConstraintLayout(modifier = modifier) {
        val container = createRef()

        val guideline = createGuidelineFromTop(0.4f)

        Column(
            modifier = Modifier
                .constrainAs(container) {
                    top.linkTo(guideline, 16.dp)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        ) {
            content()
        }
    }
}

@Composable
private fun ContentSecondary(
    spec: QuarterlyInfoSpec,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                top = with(LocalDensity.current) {
                    WindowInsets.safeDrawing
                        .only(WindowInsetsSides.Top)
                        .getTop(this)
                        .toDp()
                }
            )
    ) {

        Spacer(modifier = Modifier.height(64.dp))

        ContentBox(
            content = RemoteImage(
                data = spec.cover
            ),
            modifier = Modifier
                .size(width = 145.dp, height = 210.dp)
                .background(Color.parse(spec.color))
                .shadow(12.dp, CoverImageShape)
                .clip(CoverImageShape)
                .align(Alignment.CenterHorizontally)
        )

        content()
    }
}

private val CoverImageShape = RoundedCornerShape(6.dp)

@Composable
fun ColumnScope.Content(
    title: String,
    date: String,
    description: String,
    primaryDarkColor: Color,
    readClick: () -> Unit = {},
    readMoreClick: () -> Unit = {},
) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineMedium,
        color = Color.White,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 8.dp)
    )

    Text(
        text = date.uppercase(),
        style = MaterialTheme.typography.titleSmall,
        color = SsColor.White70,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp, bottom = 16.dp)
    )

    SsButton(
        spec = ButtonSpec(
            text = stringResource(id = R.string.ss_lessons_read),
            colors = SsButtonColors(
                containerColor = primaryDarkColor,
            ),
            onClick = readClick
        ),
        modifier = Modifier.align(Alignment.CenterHorizontally)
    )

    Text(
        text = description,
        style = MaterialTheme.typography.bodyMedium,
        color = Color.White,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { readMoreClick() }
    )
}

@Preview(
    name = "Quarterly Info",
    showBackground = true
)
@Composable
private fun QuarterlyInfoPreview() {
    SsTheme {
        QuarterlyInfo(
            spec = sampleSpec.copy(
                splashImage = null
            ),
            modifier = Modifier.height(480.dp)
        )
    }
}

@Preview(
    name = "Quarterly Info Splash",
    showBackground = true
)
@Composable
private fun QuarterlyInfoSplashPreview() {
    SsTheme {
        QuarterlyInfo(
            spec = sampleSpec,
            modifier = Modifier
        )
    }
}

private val sampleSpec = QuarterlyInfoSpec(
    title = "Rest In Christ",
    description = "The Lord is my shepherd, I shall not want.",
    date = "October - November - December",
    color = "#ffaa00",
    colorDark = "#4B3521",
    splashImage = "splash",
    cover = "cover",
    features = emptyList()
)
