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

package com.cryart.sabbathschool.core.extensions.logger

import timber.log.Timber
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

// Source: https://gist.github.com/gpeal/2666c668c9e02e3064e87bcd0557e070

inline fun <reified T : Any> T.timber(tag: String? = null) = TimberLoggerProperty<T>(tag)

class TimberLoggerProperty<T : Any>(private val tag: String? = null) : ReadOnlyProperty<T, TimberLogger> {

    @Volatile
    var logger: TimberLogger? = null

    override fun getValue(thisRef: T, property: KProperty<*>): TimberLogger {
        logger?.let { return it }
        return TimberLogger(thisRef, tag).also { logger = it }
    }
}

class TimberLogger(thisRef: Any, tag: String? = null) : Timber.Tree() {

    private val tag = tag ?: thisRef.toTag()

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        Timber.tag(tag ?: this.tag).log(priority, t, message)
    }

    private fun Any.toTag(): String {
        val str = this::class.java.simpleName
            .run { if (endsWith("Impl")) substring(0, length - 4) else this }
        if (str.length <= 23) {
            return str
        }
        return str
            .replace("Fragment", "Frag")
            .replace("ViewModel", "VM")
            .replace("Controller", "Ctrl")
            .replace("Manager", "Mgr")
            .take(23)
    }
}
