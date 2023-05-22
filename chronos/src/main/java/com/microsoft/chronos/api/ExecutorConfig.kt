// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.chronos.api

/**
 * Configuration containing the list of supported pools with [ExecutorSettings]
 */
interface ExecutorConfig {
    val executors: Set<ExecutorSettings>

    /**
     * Experiment on the [ExecutorConfig] to apply conditionally
     * which modifies and returns a new [ExecutorConfig]
     */
    interface Experiment {
        val name: String

        /**
         * Uses the argument to apply
         * this experiment to passed config
         * @return modified ExecutorConfig
         */
        fun apply(config: ExecutorConfig, argument: String?): ExecutorConfig
    }
}
