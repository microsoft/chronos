// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.chronos.measure

import com.microsoft.chronos.api.EventStream
import com.microsoft.chronos.api.ExecutorMeasureEvent
import com.microsoft.chronos.util.RunningMetric
import java.util.concurrent.*

/**
 * Measures an underlying ThreadPoolExecutor for certain metrics only if the eventStream is set
 * by emitting [ExecutorMeasureEvent] to [EventStream]
 *
 * @param threadPoolExecutor : [ThreadPoolExecutor] this Executor will wrap for measurements.
 * @param executorId : Name of the Executor to identify the events posted from
 * @param eventStream : Event Stream this Executor will post events to
 */
open class MeasuredExecutor @JvmOverloads constructor(
    private val executorId: String,
    corePoolSize: Int,
    maxPoolSize: Int,
    keepAliveInSeconds: Long,
    queue: BlockingQueue<Runnable> = LinkedBlockingQueue(),
    threadFactory: ThreadFactory = Executors.defaultThreadFactory(),
    private val eventStream: EventStream?
) : ThreadPoolExecutor(
    corePoolSize,
    maxPoolSize,
    keepAliveInSeconds,
    TimeUnit.SECONDS,
    queue,
    threadFactory
) {

    private val avgPoolSize: RunningMetric = RunningMetric.Average()
    private val avgWaitTime: RunningMetric = RunningMetric.Average()
    private val maxWaitTime: RunningMetric = RunningMetric.Maximum()
    private val maxExecutionTime: RunningMetric = RunningMetric.Maximum()
    private val avgExecutionTime: RunningMetric = RunningMetric.Average()
    private val avgQueueSize: RunningMetric = RunningMetric.Average()
    private val maxQueueSize: RunningMetric = RunningMetric.Maximum()

    private val taskStartTime: ThreadLocal<Double> = ThreadLocal<Double>()
    private val waitingTasks = mutableMapOf<Runnable, Double>()

    override fun execute(task: Runnable) {
        eventStream?.let {
            waitingTasks[task] = System.currentTimeMillis().toDouble()
            avgQueueSize.update(queue.size.toDouble())
            maxQueueSize.update(queue.size.toDouble())
        }
        super.execute(task)
    }

    override fun beforeExecute(thread: Thread?, task: Runnable) {
        eventStream?.let {
            taskStartTime.set(System.currentTimeMillis().toDouble())
            waitingTasks[task]?.let {
                val waitTime = System.currentTimeMillis() - it
                avgWaitTime.update(waitTime)
                maxWaitTime.update(waitTime)
                waitingTasks.remove(task)
            }
        }
        super.beforeExecute(thread, task)
    }

    override fun afterExecute(task: Runnable, throwable: Throwable?) {
        eventStream?.let {
            taskStartTime.get()?.let {
                val exTime = System.currentTimeMillis() - it
                avgExecutionTime.update(exTime)
                maxExecutionTime.update(exTime)
            }

            avgPoolSize.update(activeCount.toDouble())
            it.post(
                ExecutorMeasureEvent(
                    executorIdentifier = executorId,
                    averageTaskWaitTimeInMillis = avgWaitTime.currentValue.toLong(),
                    averageExecutionTimeInMillis = avgExecutionTime.currentValue.toLong(),
                    averageActiveThreads = avgPoolSize.currentValue.toInt(),
                    averageQueueSize = avgQueueSize.currentValue.toInt(),
                    maximumTaskExecutionTimeInMillis = maxExecutionTime.currentValue.toLong(),
                    maximumWaitTimeInMillis = maxWaitTime.currentValue.toLong()
                )
            )
        }
        super.afterExecute(task, throwable)
    }
}
