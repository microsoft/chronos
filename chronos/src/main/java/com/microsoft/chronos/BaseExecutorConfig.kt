// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.chronos

import androidx.annotation.StringDef
import com.microsoft.chronos.api.ExecutorConfig
import com.microsoft.chronos.api.ExecutorPriority
import com.microsoft.chronos.api.ExecutorSettings

/**
 * Base threading configuration shipped with the app.
 * This is used by [Executors] in absence of any experiments.
 */
object BaseExecutorConfig : ExecutorConfig {

    override val executors = setOf(
        /**
         * The Executor type class for user-interactive tasks,
         * such as animations, event handling, or updates to your app's user interface.
         */
        ExecutorSettings(
            executorId = ExecutorName.USER_INTERACTIVE,
            allowThreadTimeout = false,
            corePoolSize = Integer.MAX_VALUE,
            threadPriority = ExecutorPriority.PThreadPriority.MAX
        ),
        /**
         * The Executor for tasks that prevent the user from actively using your app.
         */
        ExecutorSettings(
            executorId = ExecutorName.USER_INITIATED,
            allowThreadTimeout = false,
            corePoolSize = Integer.MAX_VALUE,
            threadPriority = ExecutorPriority.PThreadPriority.HIGH
        ),
        /**
         * Default Executor for tasks with no provided priority
         */
        ExecutorSettings(
            executorId = ExecutorName.DEFAULT,
            threadPriority = ExecutorPriority.PThreadPriority.NORM
        ),
        /**
         * Executor for tasks that the user does not track actively.
         */
        ExecutorSettings(
            executorId = ExecutorName.UTILITY,
            threadPriority = ExecutorPriority.PThreadPriority.BACKGROUND
        ),
        /**
         * Executor for tasks for maintenance or cleanup tasks that you create.
         */
        ExecutorSettings(
            executorId = ExecutorName.BACKGROUND,
            threadPriority = ExecutorPriority.PThreadPriority.BACKGROUND
        ),
        /**
         * Explicitly sets the Background Operation thread pool executor.
         * This method is used for testing the code where code has to run on immediate thread or serially.
         */
        ExecutorSettings(
            executorId = ExecutorName.UNSPECIFIED,
            threadPriority = ExecutorPriority.PThreadPriority.MIN
        )
    )
}

@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION, AnnotationTarget.TYPE)
@Retention(AnnotationRetention.SOURCE)
@StringDef(
    ExecutorName.BACKGROUND,
    ExecutorName.USER_INTERACTIVE,
    ExecutorName.USER_INITIATED,
    ExecutorName.DEFAULT,
    ExecutorName.UTILITY,
    ExecutorName.UNSPECIFIED
)
annotation class ExecutorName {
    companion object {
        const val BACKGROUND = "BACKGROUND"
        const val USER_INTERACTIVE = "USER_INTERACTIVE"
        const val USER_INITIATED = "USER_INITIATED"
        const val DEFAULT = "DEFAULT"
        const val UTILITY = "UTILITY"
        const val UNSPECIFIED = "UNSPECIFIED"
    }
}
