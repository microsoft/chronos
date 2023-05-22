// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.chronos.priority

import android.os.Process
import com.microsoft.chronos.api.ExecutorPriority
import com.microsoft.chronos.api.ExecutorPriority.JvmPriority
import com.microsoft.chronos.api.ExecutorPriority.PThreadPriority
import com.microsoft.chronos.util.ThreadIdentifier
import java.util.concurrent.ThreadFactory

/**
 * Priority thread factory
 */
class PriorityThreadFactory(name: String, threadPriority: ExecutorPriority) : ThreadFactory {
    private val threadIdentifier: ThreadIdentifier

    init {
        threadIdentifier = ThreadIdentifier(name, threadPriority)
    }

    override fun newThread(r: Runnable): Thread {
        val t = Thread(r, threadIdentifier.threadName)
        t.isDaemon = false
        when (val priority: ExecutorPriority = threadIdentifier.threadPriority) {
            is JvmPriority -> t.priority = priority.getValue()
            is PThreadPriority -> Process.setThreadPriority(priority.getValue())
            else -> throw IllegalArgumentException("The ExecutorPriority type is not supported")
        }
        return t
    }
}
