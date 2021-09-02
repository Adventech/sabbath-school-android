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

package app.ss.media.model

import android.net.Uri
import androidx.annotation.Keep
import app.ss.media.playback.model.AudioFile
import app.ss.storage.db.entity.AudioFileEntity
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class SSAudio(
    val id: String,
    val artist: String,
    val image: String,
    val imageRatio: String,
    val src: String,
    val target: String,
    val targetIndex: String,
    val title: String
)

fun SSAudio.toEntity(): AudioFileEntity = AudioFileEntity(
    id,
    artist,
    image,
    imageRatio,
    src,
    target,
    targetIndex,
    title,
)

fun AudioFileEntity.toAudio(): AudioFile = AudioFile(
    id = id,
    artist = artist,
    image = image,
    imageRatio = imageRatio,
    source = Uri.parse(src),
    target = target,
    targetIndex = targetIndex,
    title = title,
)
