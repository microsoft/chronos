// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.chronos

import com.microsoft.chronos.api.*
import com.microsoft.chronos.measure.MeasuredExecutor
import com.microsoft.chronos.priority.PriorityThreadFactory
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService

/**
 * Implementation to return the correct [Executor] for a particular key.
 * Not bound to any Key and as V1 is currently working for [ThreadPoolName]
 * but this is generic implementation to take any mapping.
 */
class Executors(
    config: ExecutorConfig,
    private val eventStream: EventStream
) : IExecutors {

    private val executorMap = config.executors.associateBy(
        { it.executorId }, { ExecutorData(it, createThreadPool(it)) }
    ).toMutableMap()

    override fun getExecutor(executorKey: String): Executor {
        val executorData = executorMap[executorKey]
            ?: executorMap["BACKGROUND"]
            ?: throw IllegalArgumentException("$executorKey and BACKGROUND not in ExecutorConfig")
        return if (executorData.executor.isShutdown) {
            //  Tenant switch
            with(
                ExecutorData(
                    executorData.setting, createThreadPool(executorData.setting)
                )
            ) {
                executorMap[executorData.setting.executorId] = this
                this.executor
            }
        } else {
            executorData.executor
        }
    }

    override fun shutDown(executorKey: String): Boolean {
        val ex = executorMap[executorKey]?.executor
        return if (ex is ExecutorService) {
            ex.shutdownNow()
            true
        } else {
            false
        }
    }

    /**
     * @return a unique [MeasuredExecutor] for the passed [ExecutorSettings]
     */
    private fun createThreadPool(data: ExecutorSettings) = MeasuredExecutor(
        data.executorId, data.corePoolSize, data.maxPoolSize,
        data.keepAliveInSeconds, data.type.getQueue(),
        PriorityThreadFactory(data.executorId, data.threadPriority), eventStream
    ).apply {
        allowCoreThreadTimeOut(data.allowThreadTimeout)
        if (data.prestartCoreThread) {
            prestartCoreThread()
        }
        if (data.type is ExecutorType.BoundedQueueExecutor) {
            rejectedExecutionHandler = data.type.rejectionHandler
        }
    }

    data class ExecutorData(
        val setting: ExecutorSettings,
        val executor: MeasuredExecutor
    )
}
