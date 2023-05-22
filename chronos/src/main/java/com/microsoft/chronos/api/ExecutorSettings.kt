// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.chronos.api

import com.microsoft.chronos.priority.PriorityThreadPoolExecutor
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.PriorityBlockingQueue
import java.util.concurrent.RejectedExecutionHandler

val CPU_COUNT = Runtime.getRuntime().availableProcessors()
val CORE_POOL_SIZE: Int = CPU_COUNT + 1
val MAX_POOL_SIZE: Int = CPU_COUNT * 2 + 1
const val DEFAULT_KEEP_ALIVE_IN_SECONDS: Long = 1

/**
 *  Setting per Executor exposed by the [ThreadPoolExecutor] api
 */
data class ExecutorSettings(
    val executorId: String,
    val corePoolSize: Int = CORE_POOL_SIZE,
    val maxPoolSize: Int = MAX_POOL_SIZE,
    val keepAliveInSeconds: Long = DEFAULT_KEEP_ALIVE_IN_SECONDS,
    val allowThreadTimeout: Boolean = true,
    val prestartCoreThread: Boolean = false,
    val type: ExecutorType = ExecutorType.UnboundedQueueExecutor(),
    val threadPriority: ExecutorPriority = ExecutorPriority.JvmPriority.NORM
) {
    override fun toString(): String {
        return "[$executorId] : $corePoolSize, $maxPoolSize, " +
            "$keepAliveInSeconds, $allowThreadTimeout $keepAliveInSeconds, " +
            "$allowThreadTimeout, $prestartCoreThread, $type, $threadPriority"
    }
}

/**
 * Works on setting priority to each thread in the [Executor] linked to [ExecutorSettings.threadPriority]
 * The priorities represented in Int are then used by [PriorityThreadPoolExecutor]
 */
sealed interface ExecutorPriority {

    fun getValue(): Int

    /**
     * Based on [java.lang.Thread] priorities and currently run on JVM.
     * These are mapped to underlying pthread priorities by system.
     * https://developer.android.com/reference/java/lang/Thread#setPriority(int)
     */
    enum class JvmPriority(val priority: Int) : ExecutorPriority {
        MIN(1), BACKGROUND(3), NORM(5), MEDIUM(6), HIGH(7), MAX(10);

        override fun getValue() = priority

        override fun toString() = "JvmPriority.$name"

        companion object {
            @JvmStatic
            fun fromInt(value: Int) = values().first { it.priority == value }

            @JvmStatic
            fun fromString(value: String) = values().first { it.toString() == value }
        }
    }

    /**
     * Based on [android.os.Process] thread priorities mapping to p_thread priorities in Linux.
     * These must be preferred as these are actual priorities on the unix-based Android system
     * https://developer.android.com/reference/android/os/Process#setThreadPriority(int)
     */
    enum class PThreadPriority(val priority: Int) : ExecutorPriority {
        MIN(13), BACKGROUND(10), NORM(4), HIGH(2), MAX(0);

        override fun getValue() = priority

        override fun toString() = "PThreadPriority.$name"

        companion object {
            @JvmStatic
            fun fromInt(value: Int) = values().first { it.priority == value }

            @JvmStatic
            fun fromString(value: String) = values().first { it.toString() == value }
        }
    }
}

/**
 * Types of Queues needed by the [ExecutorSettings.type]
 * This represents the underlying Queue the Executor will implement
 */
sealed class ExecutorType {

    abstract fun getQueue(): BlockingQueue<Runnable>

    /**
     * Executor type backed by a a priority queue depending on [PriorityThreadPoolExecutor] & [TaskPriority]
     * Threads are created till Executors.maxPoolSize of queue post which queue is filled
     * till maxQueueSize post which work is rejected.
     * If your Executor is this type, implement [HasTaskPriority] in your posted Runnables.
     */
    class PriorityQueueExecutor(private val initialCapacity: Int = 10 /* Picked from ExecutorV1 */) : ExecutorType() {
        override fun getQueue() = PriorityBlockingQueue(
            initialCapacity,
            PriorityThreadPoolExecutor.PriorityComparator()
        )
    }

    /**
     * Executor type backed by a unbounded queue.
     * Threads are created till Executors.corePoolSize of queue post which queued to the underlying queue infinitely
     */
    class UnboundedQueueExecutor : ExecutorType() {
        override fun getQueue() = LinkedBlockingQueue<Runnable>()
    }

    /**
     * Executor type backed by a bounded queue.
     * Threads are created till Executors.maxPoolSize of queue post which queue is filled
     * till maxQueueSize post which work is rejected
     * @param maxQueueSize: Final size of queue after which Tasks on Executor are rejected
     */
    class BoundedQueueExecutor(
        private val maxQueueSize: Int,
        val rejectionHandler: RejectedExecutionHandler
    ) : ExecutorType() {
        override fun getQueue() = ArrayBlockingQueue<Runnable>(maxQueueSize)
    }
}
