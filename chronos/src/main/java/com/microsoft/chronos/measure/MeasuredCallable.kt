// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.chronos.measure

import com.microsoft.chronos.api.EventStream
import com.microsoft.chronos.api.ExecutionMeasureEvent
import com.microsoft.chronos.stream.*
import com.microsoft.chronos.util.measureExecution
import java.util.Date
import java.util.concurrent.Callable

/**
 * @param wrappedCallable : Callable to measure
 * @param callerContext : Package, Class, method that initiated the Callable
 * @param queueTimestamp : Creation Date of the Callable for the event
 */
class MeasuredCallable<T> @JvmOverloads constructor(
    private val wrappedCallable: Callable<T>,
    private val eventStream: EventStream?,
    private val callerContext: CallerContext? = null,
    private val queueTimestamp: Date = Date()
) : Callable<T> {
    override fun call(): T = measureExecution {
        wrappedCallable.call()
    }.also { result ->
        callerContext?.let { // Don't post if no context
            eventStream?.post( // Don't post if no stream
                ExecutionMeasureEvent(
                    eventStartTimestamp = result.startTimestamp,
                    queuedWallTimeInMillis = result.startTimestamp.time - queueTimestamp.time,
                    executionWallTimeInMillis = result.wallTimeInMillis,
                    executionCPUTimeInMillis = result.cpuTimeInMillis,
                    threadName = Thread.currentThread().name,
                    callerContext = it.toString()
                )
            )
        }
    }.result
}
