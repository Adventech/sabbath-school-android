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

@file:OptIn(ExperimentalMaterial3Api::class)

package app.ss.design.compose.widget.scaffold

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.widget.appbar.SsTopAppBar
import app.ss.design.compose.widget.appbar.TopAppBarSpec
import app.ss.design.compose.widget.appbar.TopAppBarType
import app.ss.design.compose.widget.icon.IconBox
import app.ss.design.compose.widget.icon.IconButton
import org.junit.Rule
import org.junit.Test

class ScaffoldKtTest {

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5
    )

    @Test
    fun scaffold() {
        paparazzi.snapshot {
            TestScaffold()
        }
    }

    @Test
    fun scaffold_dark() {
        paparazzi.snapshot {
            TestScaffold(true)
        }
    }

    @Composable
    private fun TestScaffold(
        darkTheme: Boolean = false
    ) {
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarScrollState())

        SsTheme(darkTheme = darkTheme) {
            SsScaffold(
                topBar = {
                    SsTopAppBar(
                        spec = TopAppBarSpec(
                            TopAppBarType.CenterAligned,
                            actions = listOf(
                                IconButton(
                                    imageVector = Icons.Rounded.AccountCircle,
                                    contentDescription = "Profile",
                                    onClick = {}
                                )
                            )
                        ),
                        title = { Text(text = "Title") },
                        navigationIcon = {
                            IconBox(
                                icon = IconButton(
                                    imageVector = Icons.Rounded.ArrowBack,
                                    contentDescription = "Back",
                                    onClick = {}
                                )
                            )
                        },
                        scrollBehavior = scrollBehavior
                    )
                },
                scrollBehavior = scrollBehavior
            ) { innerPadding ->

                LazyColumn(
                    contentPadding = innerPadding,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val list = (0..50).map { it.toString() }
                    items(count = list.size) {
                        Text(
                            text = "Hello world ${list[it]}",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { }
                                .padding(
                                    horizontal = 16.dp,
                                    vertical = 12.dp
                                )
                        )
                    }
                }
            }
        }
    }
}
