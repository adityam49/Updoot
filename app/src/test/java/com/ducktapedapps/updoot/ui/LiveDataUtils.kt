package com.ducktapedapps.updoot.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

fun <T> LiveData<T>.getOrAwaitValue(
        awaitTime: Long = 2,
        timeUnit: TimeUnit = TimeUnit.SECONDS,
        afterObserve: () -> Unit = {}
): T {
    var data: T? = null
    val latch = CountDownLatch(1)

    val singleUserObserver = object : Observer<T> {
        override fun onChanged(t: T) {
            data = t
            latch.countDown()
            this@getOrAwaitValue.removeObserver(this)
        }
    }

    observeForever(singleUserObserver)

    try {
        afterObserve.invoke()
        if (!latch.await(awaitTime, timeUnit)) throw TimeoutException("LiveData value was never set")
    } finally {
        removeObserver(singleUserObserver)
    }

    @Suppress("Unchecked_cast")
    return data as T
}