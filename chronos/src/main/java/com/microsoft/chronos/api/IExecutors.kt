// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.chronos.api

import java.util.concurrent.Executor

/**
 * Provides the relevant Executor for the operation based on
 * a String based Key
 */
interface IExecutors {

    /**
     * Provides Executor for the key passed for client to post Work to
     * @return Relevant [Executor] or a default chosen by implementation
     */
    fun getExecutor(executorKey: String): Executor

    /**
     * shuts down Executor for the [Executor] linked to key passed.
     * @return Relevant [Executor] or a default chosen by implementation
     */
    fun shutDown(executorKey: String): Boolean
}
