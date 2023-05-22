// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.chronos.util

import android.os.Debug
import android.os.SystemClock
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

data class ExecutionResult<T>(val startTimestamp: Date, val wallTimeInMillis: Long, val cpuTimeInMillis: Long, val result: T)

/**
 * Executes the given [block] and returns [ExecutionResult]
 */
public inline fun <T> measureExecution(block: () -> T): ExecutionResult<T> {
    val startTimeStamp = Date()
    val startWall = System.currentTimeMillis()
    val startCPU = Debug.threadCpuTimeNanos()
    val res: T = block()
    return ExecutionResult<T>(
        wallTimeInMillis = System.currentTimeMillis() - startWall,
        cpuTimeInMillis = TimeUnit.NANOSECONDS.toMillis(Debug.threadCpuTimeNanos() - startCPU),
        startTimestamp = startTimeStamp,
        result = res
    )
}

fun <T> Flow<T>.throttle(waitMillis: Long): Flow<T> {
    if (waitMillis <= 0)
        return this
    return flow {
        coroutineScope {
            val context = coroutineContext
            var nextMillis = 0L
            var delayPost: Deferred<Unit>? = null
            collect {
                val current = SystemClock.uptimeMillis()
                if (nextMillis < current) {
                    nextMillis = current + waitMillis
                    emit(it)
                    delayPost?.cancel()
                } else {
                    val delayNext = nextMillis
                    delayPost?.cancel()
                    delayPost = async(coroutineContext) {
                        delay(nextMillis - current)
                        if (delayNext == nextMillis) {
                            nextMillis = SystemClock.uptimeMillis() + waitMillis
                            withContext(context) {
                                emit(it)
                            }
                        }
                    }
                }
            }
        }
    }
}
