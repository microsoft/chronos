// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.chronos.priority

import com.microsoft.chronos.api.EventStream
import com.microsoft.chronos.measure.MeasuredExecutor
import com.microsoft.chronos.api.CORE_POOL_SIZE
import com.microsoft.chronos.api.MAX_POOL_SIZE
import com.microsoft.chronos.api.DEFAULT_KEEP_ALIVE_IN_SECONDS
import com.microsoft.chronos.api.ExecutorPriority
import com.microsoft.chronos.priority.PriorityThreadPoolExecutor.PriorityComparator
import java.util.concurrent.PriorityBlockingQueue
import java.util.concurrent.ThreadFactory

/**
 * PriorityThreadPoolExecutor utilizes [PriorityBlockingQueue] to queue up tasks and order them using
 * the provided [PriorityComparator] object. Runnables must implement [HasTaskPriority]
 * to assign a task priority so that their task will get queued up based on the
 * TaskPriority they have relative to other tasks' TaskPriority.
 */
class PriorityThreadPoolExecutor private constructor(
    name: String,
    corePoolSize: Int,
    maximumPoolSize: Int,
    keepAliveTime: Long,
    priorityBlockingQueue: PriorityBlockingQueue<Runnable>,
    threadFactory: ThreadFactory,
    eventStream: EventStream?
) : MeasuredExecutor(name, corePoolSize, maximumPoolSize, keepAliveTime, priorityBlockingQueue, threadFactory, eventStream) {
    /**
     * The comparator which considers only priority as a deciding factor to rearrange in a priority queue.
     */
    class PriorityComparator : Comparator<Runnable> {
        override fun compare(lhs: Runnable, rhs: Runnable): Int {
            val lhsPriority = if (lhs is HasTaskPriority) (lhs as HasTaskPriority).priority else 0
            val rhsPriority = if (rhs is HasTaskPriority) (rhs as HasTaskPriority).priority else 0
            val diff = (rhsPriority - lhsPriority).toLong()
            return java.lang.Long.signum(diff)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(name: String, threadPriority: ExecutorPriority, eventStream: EventStream?): PriorityThreadPoolExecutor {
            val initialQueueCapacity = 10 /* This does not determine the size of the queue but just the initial size */
            return PriorityThreadPoolExecutor(
                name, CORE_POOL_SIZE, MAX_POOL_SIZE,
                DEFAULT_KEEP_ALIVE_IN_SECONDS, PriorityBlockingQueue(initialQueueCapacity, PriorityComparator()),
                PriorityThreadFactory(name, threadPriority), eventStream
            )
        }

        /**
         * This instance uses the [PriorityComparator] and allows us to define pool size for
         * the priority thread pool executor.
         */
        @JvmStatic
        fun newInstance(
            name: String,
            corePoolSize: Int,
            maxPoolSize: Int,
            keepAliveTime: Long,
            threadPriority: ExecutorPriority,
            eventStream: EventStream?
        ): PriorityThreadPoolExecutor {
            val initialQueueCapacity = 10 /* This does not determine the size of the queue but just the initial size */
            return PriorityThreadPoolExecutor(
                name, corePoolSize, maxPoolSize, keepAliveTime,
                PriorityBlockingQueue(initialQueueCapacity, PriorityComparator()),
                PriorityThreadFactory(name, threadPriority), eventStream
            )
        }
    }
}
