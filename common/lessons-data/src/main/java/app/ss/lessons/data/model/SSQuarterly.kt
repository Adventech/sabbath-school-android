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
import com.google.firebase.database.IgnoreExtraProperties
import java.util.UUID

@Keep
@IgnoreExtraProperties
data class SSQuarterly(
    val id: String,
    val title: String = "",
    val description: String = "",
    val human_date: String = "",
    val start_date: String = "",
    val end_date: String = "",
    val cover: String = "",
    val index: String = "",
    val group: String = UUID.randomUUID().toString(), // Allows us to easily group quarterlies while maintaining order from api
    val path: String = "",
    val full_path: String = "",
    val lang: String = "",
    val color_primary: String,
    val color_primary_dark: String = "",
    val quarterly_name: String = "",
) {
    constructor() : this("", color_primary = "")
}
