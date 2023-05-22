// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.chronos.api

import java.util.Date
import kotlin.reflect.KClass

/**
 * MeasurementEvent emitted by each Measured producer
 * @param meta : Properties that can be filled by any Provider such as memory info, app visibility etc that happened at the time
 * @param eventContext: Event Identifier, can be packagem calling class details for [ExecutionMeasureEvent] or threadpool name for [ExecutorMeasureEvent]
 */
sealed class MeasureEvent(
    private val meta: Map<String, Any>,
    private val eventContext: String
)

/**
 * Add more events to this class & to the map below.
 * We are not picking simplename to make it R8 & Reflection proof
 */
val mapOfEventNameToSubclass = mapOf<String, KClass<out MeasureEvent>>(
    "ExecutionMeasureEvent" to ExecutionMeasureEvent::class,
    "ExecutorMeasureEvent" to ExecutorMeasureEvent::class,
)

/**
 * Emitted by [Measured*] executions such as Bolt Tasks, Async Tasks, Handler threads
 */
data class ExecutionMeasureEvent(
    /**
     * Timestamp of start of this event
     */
    val eventStartTimestamp: Date,
    /**
     * Wall clock time spent between posting of work & thread picking it up
     */
    val queuedWallTimeInMillis: Long = 0,
    /**
     * Wall clock time spent between thread picking up and marking as complete
     */
    val executionWallTimeInMillis: Long = 0,
    /**
     * CPU clock time spent between thread picking up and marking as complete
     */
    val executionCPUTimeInMillis: Long = 0,
    /**
     * Ratio of ExecutionCPUTime/ ExecutionWallTime, indicating CPU it got while being executed on the thread.
     */
    val executionRatio: Double = if (executionWallTimeInMillis > 0.0)
        executionCPUTimeInMillis.toDouble() / executionWallTimeInMillis else 0.0,
    /**
     * The thread that executed the work, can be used to derive Executor.
     */
    val threadName: String = "",
    /**
     * Model containing Caller package, class, method, line number
     */
    val callerContext: String = "",
    /**
     * The metric can be tagged with custom Strings such as “Sync”, “DBCall”,”Inflation” etc.
     * This can help in query and classification as needed apart from the CallerContext classification.
     */
    var tags: MutableList<String> = mutableListOf(),
    /**
     * MetaData Map associated for the event
     */
    var meta: MutableMap<String, Any> = mutableMapOf()
) : MeasureEvent(meta = meta, eventContext = callerContext) {

    override fun toString(): String = "callerContext:$callerContext, eventStartTimestamp:$eventStartTimestamp, queuedWallTimeInMillis:$queuedWallTimeInMillis," +
        " executionWallTimeInMillis:$executionWallTimeInMillis, executionCPUTimeInMillis:$executionCPUTimeInMillis," +
        " executionRatio:$executionRatio, threadName:$threadName, tags:$tags, meta: $meta"
}

/**
 * Emitted by [MeasuredExecutor] such as Threadpools to measure executor metrics
 */
data class ExecutorMeasureEvent(
    /**
     * Average time tasks wait in this Executor for the lifecycle of Executor.
     */
    val averageTaskWaitTimeInMillis: Long = 0,
    /**
     * Cumulative average of all wall clock time of completed tasks.
     */
    val averageExecutionTimeInMillis: Long = 0,
    /**
     * Number of active threads on average for the lifecycle of Executor.
     */
    val averageActiveThreads: Int = 0,
    /**
     * Average size of the wait queue of the Executor.
     */
    val averageQueueSize: Int = 0,
    /**
     * Average size of the wait queue of the Executor.
     */
    val maximumQueueSize: Int = 0,
    /**
     * Maximum of all wall clock time of completed tasks for the lifecycle of the Executor
     */
    val maximumTaskExecutionTimeInMillis: Long = 0,
    /**
     * Maximum time of waiting times of all the tasks for the lifecycle of the Executor.
     */
    val maximumWaitTimeInMillis: Long = 0,
    /**
     *  Name of the Executor this event belongs to.
     */
    val executorIdentifier: String = "",
    /**
     * MetaData Map associated for the event
     */
    val meta: MutableMap<String, Any> = mutableMapOf()
) : MeasureEvent(meta = meta, eventContext = executorIdentifier) {

    override fun toString(): String = "executorIdentifier:$executorIdentifier, averageTaskWaitTimeInMillis:$averageTaskWaitTimeInMillis" +
        ", averageExecutionTimeInMillis:$averageExecutionTimeInMillis, averageActiveThreads:$averageActiveThreads, averageQueueSize:$averageQueueSize" +
        ", maximumQueueSize:$maximumQueueSize, maximumTaskExecutionTimeInMillis:$maximumTaskExecutionTimeInMillis, maximumWaitTimeInMillis:$maximumWaitTimeInMillis, meta: $meta"
}
