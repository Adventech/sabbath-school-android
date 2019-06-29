/*
 * Copyright (c) 2019. Adventech <info@adventech.io>.
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

package com.cryart.sabbathschool.misc

import org.junit.Assert.assertEquals
import org.junit.Test

class SSHelperTest {

    @Test
    fun parse_valid_hour() {
        val time = "2019-06-29 20:00:30"

        val hour = SSHelper.parseHourFromString(time, DATE_FORMAT)

        assertEquals(20, hour)
    }

    @Test
    fun parse_valid_min() {
        val time = "2019-06-29 20:30:30"

        val min = SSHelper.parseMinuteFromString(time, DATE_FORMAT)

        assertEquals(30, min)
    }

    @Test
    fun parse_hour_invalid_format() {
        val time = "2019-06-29 20:00:30"

        val hour = SSHelper.parseHourFromString(time, "zzzz-dd-MM HH:mm")

        assertEquals(0, hour)
    }

    @Test
    fun parse_min_invalid_format() {
        val time = "2019-06-29 20:00:30"

        val min = SSHelper.parseMinuteFromString(time, "zzzz-dd-MM HH:mm")

        assertEquals(0, min)
    }

    companion object {
        private const val DATE_FORMAT = "yyyy-MM-dd HH:mm:ss"
    }
}