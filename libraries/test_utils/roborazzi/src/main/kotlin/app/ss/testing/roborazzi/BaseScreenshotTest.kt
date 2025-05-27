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

package app.ss.testing.roborazzi

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.RoborazziActivity
import com.github.takahirom.roborazzi.RoborazziRule
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

private const val OUTPUT_DIR = "src/test/snapshots/images"
private const val DEVICE_QUALIFIERS = RobolectricDeviceQualifiers.Pixel7Pro
private const val ROBORAZZI_TARGET_SDK = 34

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = DEVICE_QUALIFIERS, sdk = [ROBORAZZI_TARGET_SDK])
abstract class BaseScreenshotTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<RoborazziActivity>()

    @get:Rule
    val roborazziRule = RoborazziRule(
        options = RoborazziRule.Options(
            outputDirectoryPath = OUTPUT_DIR,
        )
    )

    protected fun snapshot(testLightDark: TestLightDark = TestLightDark.SYSTEM, content: @Composable () -> Unit) {
        composeTestRule.apply {
            setContent { SnapshotContent(testLightDark, content) }
            onRoot().captureRoboImage()
        }
    }

    @Composable
    private fun SnapshotContent(testLightDark: TestLightDark, content: @Composable () -> Unit) {
        when (testLightDark) {
            TestLightDark.SYSTEM -> {
                content()
            }

            TestLightDark.LIGHT -> {
                CompositionLocalProvider(
                    LocalConfiguration provides createConfiguration(Configuration.UI_MODE_NIGHT_NO)
                ) {
                    content()
                }
            }

            TestLightDark.DARK -> {
                CompositionLocalProvider(
                    LocalConfiguration provides createConfiguration(Configuration.UI_MODE_NIGHT_YES)
                ) {
                    content()
                }
            }

            TestLightDark.BOTH -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box(Modifier.weight(1f)) {
                        CompositionLocalProvider(
                            LocalConfiguration provides createConfiguration(Configuration.UI_MODE_NIGHT_NO)
                        ) {
                            content()
                        }
                    }

                    Box(Modifier.weight(1f)) {
                        CompositionLocalProvider(
                            LocalConfiguration provides createConfiguration(Configuration.UI_MODE_NIGHT_YES)
                        ) {
                            content()
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun createConfiguration(uiModeConfig: Int): Configuration {
        val original = LocalConfiguration.current
        return Configuration(original).apply {
            uiMode = uiModeConfig
        }
    }
}
