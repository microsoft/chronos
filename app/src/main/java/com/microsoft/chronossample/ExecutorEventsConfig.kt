// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.chronossample

import com.google.gson.annotations.SerializedName
import com.microsoft.chronos.stream.FlowEventStreamConfig
import com.microsoft.chronos.stream.FlowStreamConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * Configuration values powered via ECS mapped to [FlowEventStreamConfig]
 */
class ExecutorEventsConfig {
    @SerializedName("streamConfigs")
    val configs: Map<String, StreamConfig>? = null

    /**
     * Check [StackContextProvider] for usage details
     */
    @SerializedName("stackContextIgnoreList")
    val stackContextIgnoreList: List<String> = listOf("chronos", "logger", "threading", "TaskUtilities")

    /**
     * Check [StackContextProvider] for usage details
     */
    @SerializedName("stackContextSkipList")
    val stackContextSkipList: List<String> = listOf(
        "ApplicationServiceStateManager", // Powered by decoupled events, no idea where the event is from
        "EventHandler", // Powered by decoupled events, no idea where the event is from
        "HttpCallExecutor" // Network call, we have separate telemetry for them
    )

    /**
     * Skipped Executors
     */
    @SerializedName("skippedExecutors")
    val skippedExecutors: List<String> = listOf("Telemetry")

    /**
     * Maximum Time for scenario expiry till it's not considered for tagging
     */
    @SerializedName("maxScenarioTimeThreshold")
    val maxScenarioTimeThreshold: Long = 120000

    /**
     * Minimum Time for scenario below which events are dropped
     */
    @SerializedName("minScenarioTimeThreshold")
    val minScenarioTimeThreshold: Long = 10

    companion object {
        fun toFlowEventStreamConfig(config: ExecutorEventsConfig?, scope: CoroutineScope): FlowEventStreamConfig =
            config?.configs?.entries?.associateBy(
                { it.key },
                { StreamConfig.toFlowStreamConfig(it.value, scope) }
            )?.let {
                FlowEventStreamConfig(it)
            } ?: run {
                FlowEventStreamConfig.default
            }
    }
}

/**
 * Validates if any of the list items are in the passed String
 */
fun ExecutorEventsConfig.Companion.validateInList(className: String, list: List<String>): Boolean {
    return list.parallelStream().anyMatch { className.contains(it, ignoreCase = true) }
}

/**
 * Configuration values per stream powered via ECS
 * mapped to [FlowEventStreamConfig]
 */
class StreamConfig {
    @SerializedName("isStreamEnabled")
    val isStreamEnabled: Boolean = true

    @SerializedName("streamBufferCapacity")
    val streamBufferCapacity: Int = 500

    @SerializedName("debounceDelay")
    val debounceDelay: Long = 100

    companion object {
        fun toFlowStreamConfig(config: StreamConfig, scope: CoroutineScope) = FlowStreamConfig(
            isStreamEnabled = config.isStreamEnabled,
            streamBufferCapacity = config.streamBufferCapacity,
            debounceDelay = config.debounceDelay,
            coroutineScope = scope,
            collectorCoroutineContext = Executors.newSingleThreadExecutor().asCoroutineDispatcher(), // Keeping these here, can be powered from where it's constructed.
            emitterCoroutineContext = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
        )
    }
}
