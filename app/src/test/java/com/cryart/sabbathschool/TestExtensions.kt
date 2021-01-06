
package com.cryart.sabbathschool

import androidx.lifecycle.LiveData

fun <T> LiveData<T>.observeFuture(): List<T> = mutableListOf<T>().apply {
    observeForever { add(it) }

    clear()
}

fun <T> LiveData<T>.observeAll(): List<T> = mutableListOf<T>().apply {
    observeForever { add(it) }
}
