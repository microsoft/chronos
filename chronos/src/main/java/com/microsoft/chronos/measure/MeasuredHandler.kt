// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.chronos.measure

import android.os.Handler
import android.os.Looper
import android.os.Message
import com.microsoft.chronos.api.EventStream
import com.microsoft.chronos.api.ExecutorMeasureEvent
import com.microsoft.chronos.util.RunningMetric
import com.microsoft.chronos.util.measureExecution

/**
 * A [Handler] that measures the time it takes to execute a [Runnable] on a Looper.
 * To be used with [HandlerThread]
 */
open class MeasuredHandler(looper: Looper, val threadName: String, val eventStream: EventStream?, callback: Callback? = null) : Handler(looper, callback) {

    val runningAvg: RunningMetric = RunningMetric.Average()

    constructor(looper: Looper, threadName: String, eventStream: EventStream?) : this(looper, threadName, eventStream, null)

    override fun dispatchMessage(msg: Message) {
        measureExecution {
            super.dispatchMessage(msg)
        }.also { result ->
            runningAvg.update(result.wallTimeInMillis.toDouble())
            eventStream?.post( // Don't post if no stream
                ExecutorMeasureEvent(
                    executorIdentifier = threadName,
                    averageTaskWaitTimeInMillis = 0,
                    averageExecutionTimeInMillis = runningAvg.currentValue.toLong(),
                    averageActiveThreads = 1,
                    averageQueueSize = 0
                )
            )
        }
    }
}
