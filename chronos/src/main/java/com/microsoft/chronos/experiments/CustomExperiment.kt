// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.chronos.experiments

import com.google.gson.annotations.SerializedName
import com.microsoft.chronos.api.ExecutorConfig
import com.microsoft.chronos.api.ExecutorPriority
import com.microsoft.chronos.api.ExecutorSettings

/**
 * Experiment that parses @param argument fetched via ECS for
 * dynamic list of [ExecutorSettings] to override/replace.
 * Please follow the wiki to run dynamic experiments,
 * <>
 * argument format Eg:
 * [
 * {
 *      "executorId" : "Network",
 *      "corePoolSize" : 24,
 *      "threadPriority" : "JvmPriority.HIGH"
 * },
 * {
 *      "executorId" : "AppLifecycle",
 *      "allowThreadTimeout" : false,
 *      "keepAliveInSeconds" : "30"
 * }
 * ]
 */
object CustomExperiment : ExecutorConfig.Experiment {
    override val name = "CustomExperiment"

    override fun apply(base: ExecutorConfig, argument: String?): ExecutorConfig {
        /*val overiddenExecutorMap: Map<String, ExecutorSettingsModel> = JsonUtils.getJsonArrayFromString(argument).associate {
            val obj = JsonUtils.parseObject(it, ExecutorSettingsModel::class.java, null)
            obj.executorId to obj
        }
        return object : ExecutorConfig {

            override val executors = base.executors.map { data ->
                overiddenExecutorMap[data.executorId]?.let {
                    ExecutorSettingsModel.update(
                        data,
                        it
                    )
                } ?: data
            }.toSet()
        }*/
        return base
    }
}

/**
 * Model class to map ECS ExecutorSetting json object to [ExecutorSettings]
 */
data class ExecutorSettingsModel(
    @SerializedName("executorId") val executorId: String,
    @SerializedName("corePoolSize") val corePoolSize: Int?,
    @SerializedName("maxPoolSize") val maxPoolSize: Int?,
    @SerializedName("keepAliveInSeconds") val keepAliveInSeconds: Long?,
    @SerializedName("allowThreadTimeout") val allowThreadTimeout: Boolean?,
    @SerializedName("prestartCoreThread") val prestartCoreThread: Boolean?,
    @SerializedName("threadPriority") val threadPriority: String?
) {
    companion object {
        /**
         * Updates base [ExecutorSettings] values with passed [ExecutorSettingsModel], prefers base values till overrides are present
         */
        fun update(base: ExecutorSettings, newSettings: ExecutorSettingsModel): ExecutorSettings {
            return ExecutorSettings(
                executorId = newSettings.executorId,
                corePoolSize = newSettings.corePoolSize ?: base.corePoolSize,
                maxPoolSize = newSettings.maxPoolSize ?: base.maxPoolSize,
                keepAliveInSeconds = newSettings.keepAliveInSeconds ?: base.keepAliveInSeconds,
                allowThreadTimeout = newSettings.allowThreadTimeout ?: base.allowThreadTimeout,
                prestartCoreThread = newSettings.prestartCoreThread ?: base.prestartCoreThread,
                threadPriority = newSettings.threadPriority?.let { toExecutorPriority(it) ?: base.threadPriority } ?: base.threadPriority,
                type = base.type // Note: type shouldn't be changed dynamically
            )
        }

        /**
         * Maps value of key "threadPriority" to [ExecutorPriority]
         * Only understands value in following format, JvmPriority.NORM, PThreadPriority.HIGH
         * else returns null
         */
        fun toExecutorPriority(pr: String): ExecutorPriority? {
            return try {
                if (pr.startsWith("PThreadPriority")) {
                    ExecutorPriority.PThreadPriority.fromString(pr)
                } else if (pr.startsWith("JvmPriority")) {
                    ExecutorPriority.JvmPriority.fromString(pr)
                } else {
                    null
                }
            } catch (ex: NoSuchElementException) {
                null
            }
        }
    }
}
