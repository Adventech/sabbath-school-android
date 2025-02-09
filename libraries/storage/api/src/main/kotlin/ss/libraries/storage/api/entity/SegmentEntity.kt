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

package ss.libraries.storage.api.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.adventech.blockkit.model.BlockItem
import io.adventech.blockkit.model.Style
import io.adventech.blockkit.model.resource.PdfAux
import io.adventech.blockkit.model.resource.SegmentType
import io.adventech.blockkit.model.resource.VideoClipSegment

@Entity(tableName = "segments")
data class SegmentEntity(
    @PrimaryKey val id: String,
    val index: String,
    val name: String,
    val title: String,
    val type: SegmentType,
    val resourceId: String,
    val markdownTitle: String?,
    val subtitle: String?,
    val markdownSubtitle: String?,
    val titleBelowCover: Boolean?,
    val cover: String?,
    val blocks: List<BlockItem>?,
    val date: String?,
    val background: String?,
    val pdf: List<PdfAux>?,
    val video: List<VideoClipSegment>?,
    val style: Style?,
)
