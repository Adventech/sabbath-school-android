/*
 * Copyright (c) 2020 Adventech <info@adventech.io>
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
package app.ss.lessons.data.model

import androidx.annotation.Keep
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.IgnoreExtraProperties

@Keep
@IgnoreExtraProperties
data class SSQuarterly(
    val id: String,
    val title: String = "",
    val description: String = "",
    val introduction: String? = null,
    val human_date: String = "",
    val start_date: String = "",
    val end_date: String = "",
    val cover: String = "",
    val splash: String? = null,
    val index: String = "",
    val path: String = "",
    val full_path: String = "",
    val lang: String = "",
    val color_primary: String = "",
    val color_primary_dark: String = "",
    val quarterly_name: String = "",
    val quarterly_group: QuarterlyGroup? = null,
    val features: List<Feature> = emptyList(),
    val credits: List<Credit> = emptyList()
) {

    constructor(snapshot: DataSnapshot) : this(
        snapshot.child("id").getValue(String::class.java) ?: "",
        snapshot.child("title").getValue(String::class.java) ?: "",
        snapshot.child("description").getValue(String::class.java) ?: "",
        snapshot.child("introduction").getValue(String::class.java),
        snapshot.child("human_date").getValue(String::class.java) ?: "",
        snapshot.child("start_date").getValue(String::class.java) ?: "",
        snapshot.child("end_date").getValue(String::class.java) ?: "",
        snapshot.child("cover").getValue(String::class.java) ?: "",
        snapshot.child("splash").getValue(String::class.java),
        snapshot.child("index").getValue(String::class.java) ?: "",
        snapshot.child("path").getValue(String::class.java) ?: "",
        snapshot.child("full_path").getValue(String::class.java) ?: "",
        snapshot.child("lang").getValue(String::class.java) ?: "",
        snapshot.child("color_primary").getValue(String::class.java) ?: "",
        snapshot.child("color_primary_dark").getValue(String::class.java) ?: "",
        snapshot.child("quarterly_name").getValue(String::class.java) ?: "",
        snapshot.child("quarterly_group").getValue(QuarterlyGroup::class.java),
        snapshot.child("features").children.mapNotNull { it.getValue(Feature::class.java) },
        snapshot.child("credits").children.mapNotNull { it.getValue(Credit::class.java) }
    )
}
