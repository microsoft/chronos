// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.chronos.util

import com.microsoft.chronos.api.ExecutorPriority
import com.microsoft.chronos.api.ExecutorPriority.JvmPriority.Companion.fromInt
import java.util.concurrent.atomic.AtomicInteger

/**
 * Class to maintain variables and methods related to thread information.
 */
class ThreadIdentifier {
    private val mThreadNumber = AtomicInteger(1)
    val threadPriority: ExecutorPriority
    private val mNamePrefix: String
    val poolName: String

    @JvmOverloads
    constructor(poolName: String, threadPriority: ExecutorPriority = ExecutorPriority.JvmPriority.NORM) {
        mNamePrefix = getThreadNamePrefixForPool(poolName)
        this.threadPriority = threadPriority
        this.poolName = poolName
    }

    constructor(thread: Thread) {
        poolName = getPoolNameFromThreadName(thread.name)
        threadPriority = fromInt(thread.priority)
        mNamePrefix = getThreadNamePrefixForPool(poolName)
    }

    private fun getThreadNamePrefixForPool(poolName: String): String {
        return "Pool-$poolName-Thread-"
    }

    val threadName: String
        get() = mNamePrefix + mThreadNumber.getAndIncrement()

    companion object {
        /**
         * In the ThreadFactory, we have defined the thread name to have the below syntax.
         * "Pool-" + poolName + "-Thread-". Hence we split by "-" to obtain the pool name.
         *
         * @return Returns the pool name that the thread belongs to.
         */
        private fun getPoolNameFromThreadName(threadName: String): String {
            if (threadName.isNotEmpty() && threadName.startsWith("Pool")) {
                val stringArray = threadName.split("-").toTypedArray()
                return if (stringArray.size > 2) {

                    // return the pool name.
                    stringArray[1]
                } else "UNKNOWN"

                // return the thread name as UNKNOWN if it does not match the "Pool-" + poolName + "-Thread-" syntax.
            }
            return "UNKNOWN"
        }
    }
}
