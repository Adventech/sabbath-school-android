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

package com.cryart.sabbathschool.benchmark.quarterlies

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until

fun MacrobenchmarkScope.startApplicationJourney() {
    pressHome()
    startActivityAndWait()

    // Wait until content is loaded
    device.wait(Until.hasObject(By.text("STANDARD ADULT")), 30_000)

    // device.wait(Until.hasObject(By.res("quarterlies:list")), 15_000)

    // Wait until the quarterlies group item within the list is rendered
    // val quarterliesList = device.findObject(By.res("quarterlies:list"))
    // quarterliesList.wait(Until.hasObject(By.res("quarterlies:group")), 15_000)
}

fun MacrobenchmarkScope.quarterliesScrollListDownUp() {
    val quarterliesList = device.findObject(By.res("quarterlies:list"))
    quarterliesList.fling(Direction.DOWN)
    device.waitForIdle()
    quarterliesList.fling(Direction.UP)
}
