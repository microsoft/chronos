// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.chronos.util

import java.util.concurrent.ThreadFactory

/**
 * The default thread factory.
 */
class DefaultThreadFactory internal constructor(name: String) : ThreadFactory {
    private val mThreadIdentifier: ThreadIdentifier

    init {
        mThreadIdentifier = ThreadIdentifier(name)
    }

    override fun newThread(r: Runnable): Thread {
        val t = Thread(r, mThreadIdentifier.threadName)
        if (t.isDaemon) {
            t.isDaemon = false
        }
        if (t.priority != Thread.NORM_PRIORITY) {
            t.priority = Thread.NORM_PRIORITY
        }
        return t
    }
}
