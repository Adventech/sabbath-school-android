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

package com.cryart.sabbathschool.data.model.response

import com.cryart.sabbathschool.data.model.Status
import com.cryart.sabbathschool.data.model.Status.ERROR
import com.cryart.sabbathschool.data.model.Status.LOADING
import com.cryart.sabbathschool.data.model.Status.SUCCESS

class Resource<out T> private constructor(val status: Status = LOADING,
                                          val data: T?,
                                          val error: Throwable?) {

    val isSuccessFul: Boolean get() = data != null

    companion object {

        fun <T> success(data: T): Resource<T> {
            return Resource(SUCCESS, data, null)
        }

        fun <T> error(error: Throwable): Resource<T> {
            return Resource(ERROR, null, error)
        }

        fun <T> loading(): Resource<T> {
            return Resource(LOADING, null, null)
        }
    }
}
