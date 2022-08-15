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

package app.ss.lessons.data.model

import app.ss.models.SSLesson
import app.ss.models.SSLessonInfo
import app.ss.models.SSQuarterly
import app.ss.models.SSQuarterlyInfo
import app.ss.models.SSRead
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test

class ModelTests {

    @Test
    fun `should format quarterly info share index`() {
        val quarterly = SSQuarterly(
            id = "en-2021-03",
            index = "en-2021-03"
        )
        val quarterlyInfo = SSQuarterlyInfo(quarterly, emptyList())

        quarterlyInfo.shareIndex() shouldBeEqualTo "en/2021-03"
    }

    @Test
    fun `should format lesson info share index`() {
        val lesson = SSLesson(
            title = "",
            index = "en-2021-03-04"
        )

        val lessonInfo = SSLessonInfo(lesson, emptyList())

        lessonInfo.shareIndex() shouldBeEqualTo "en/2021-03/04"
    }

    @Test
    fun `should format Read share index`() {
        val read = SSRead(
            id = "",
            index = "en-2021-03-04-02"
        )

        read.shareIndex("en/2021-03/04", 2) shouldBeEqualTo "en/2021-03/04/02"
    }

    @Test
    fun `should format Read share index additional day`() {
        val read = SSRead(
            id = "",
            index = "en-2021-03-04-teacher-comments",
            title = "Teacher Comments"
        )

        read.shareIndex("en/2021-03/04", 9) shouldBeEqualTo "en/2021-03/04/09-teacher-comments"
    }
}
