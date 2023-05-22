// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.chronos.priority

/***
 * Runnable should implement this Interface to take advantage of the PriorityBlockingQueue utilized by the [PriorityThreadPoolExecutor]
 * This is used to compare tasks relative to each other, based on their priority, in order to schedule them in the queue properly.
 */
interface HasTaskPriority {
    val priority: Int
}
