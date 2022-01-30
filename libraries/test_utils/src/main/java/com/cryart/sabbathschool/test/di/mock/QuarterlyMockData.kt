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

package com.cryart.sabbathschool.test.di.mock

import android.content.Context
import app.ss.models.QuarterlyGroup
import app.ss.models.SSQuarterly
import app.ss.lessons.data.model.SSQuarterlyInfo
import com.cryart.sabbathschool.test.di.repository.fromJson
import com.squareup.moshi.Moshi

interface QuarterlyMockData {
    fun getQuarterlies(group: QuarterlyGroup? = null): List<SSQuarterly>
    fun getQuarterlyInfo(index: String): SSQuarterlyInfo?
}

class QuarterlyMockDataImpl(
    private val moshi: Moshi,
    private val context: Context
) : QuarterlyMockData {

    private var _quarterlies: List<SSQuarterly> = emptyList()

    override fun getQuarterlies(group: QuarterlyGroup?): List<SSQuarterly> {
        if (_quarterlies.isEmpty()) {
            _quarterlies = moshi.fromJson(context, FILE_QUARTERLIES)
        }
        return group?.let { _quarterlies.filter { it.quarterly_group == group } } ?: _quarterlies
    }

    override fun getQuarterlyInfo(index: String): SSQuarterlyInfo? {
        return getQuarterlies().find { it.index == index }?.let { quarterly ->
            SSQuarterlyInfo(quarterly, moshi.fromJson(context, FILE_LESSONS))
        }
    }

    companion object {
        private const val FILE_QUARTERLIES = "quarterlies.json"
        private const val FILE_LESSONS = "lessons.json"
    }
}
