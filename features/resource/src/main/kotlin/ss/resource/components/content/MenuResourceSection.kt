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

package ss.resource.components.content

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.widget.icon.IconBox
import app.ss.design.compose.widget.icon.Icons
import io.adventech.blockkit.model.resource.ResourceSection
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

data class MenuResourceSection(
    override val id: String,
    private val sections: ImmutableList<ResourceSection>,
    private val defaultSection: ResourceSection? = null,
    private val onSelection: (ResourceSection) -> Unit
) : ResourceSectionSpec {

    @Composable
    override fun Content(modifier: Modifier) {
        var expanded by remember { mutableStateOf(false) }
        var selectedSection by remember(defaultSection) { mutableStateOf<ResourceSection?>(defaultSection) }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = RoundedCornerShape(16.dp),
        ) {
            sections.forEach { section ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = section.title,
                            modifier = Modifier,
                            style = SsTheme.typography.titleMedium
                        )
                    },
                    onClick = {
                        expanded = false
                        onSelection(section)
                    },
                    modifier = Modifier,
                    trailingIcon = {
                        if (section == selectedSection) {
                            IconBox(Icons.Check)
                        }
                    }
                )
            }
        }

        Surface(modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .sizeIn(minHeight = 48.dp)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { expanded = true }
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = selectedSection?.title ?: "",
                    modifier = Modifier,
                    style = SsTheme.typography.titleMedium.copy(
                        fontSize = 18.sp
                    ),
                    maxLines = 1
                )

                IconBox(Icons.ArrowDropDown)
            }
        }

    }
}

@PreviewLightDark
@Composable
private fun Preview() {
    SsTheme {
        Surface {
            MenuResourceSection(
                id = "123",
                sections = Placeholder.resourcesWithMenu.toImmutableList(),
                defaultSection = Placeholder.resourcesWithMenu.firstOrNull(),
                onSelection = {}
            ).Content()
        }
    }
}
