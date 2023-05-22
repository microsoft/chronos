// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.chronos.util

import com.microsoft.chronos.stream.CallerContext
import com.microsoft.chronos.stream.ContextProvider

/**
 * Implements [CallerContext] to convert relevant [StackTraceElement] to a String
 */
data class StackCallerContext(
    val className: String,
    val methodName: String,
    val lineNumer: Int
) : CallerContext {
    override fun toString(): String = "$className.$methodName"
}

/**
 * This method takes a predicate and matches to all stackTraceElements to return the first matching element's class and method name
 * @param predicateToMatch : Predicate to Match each StackTraceElement against, will consider that Class.method as the source
 * @param predicateToExit : Predicate to Exit if true, to exit out of traces we don't need to measure
 * @return the class and method from the StackTrace
 */
class StackContextProvider(
    /*
    * The predicate if true returns the current StackTraceElement details
    * for the event.
    */
    private val predicateToMatch: (StackTraceElement) -> Boolean,
    /*
    *  The predicate if true, exits and skip the stacktrace/event
    */
    private val predicateToExit: (StackTraceElement) -> Boolean,
) : ContextProvider<Array<StackTraceElement>> {
    override fun provideCallerContext(frames: Array<StackTraceElement>): StackCallerContext? {
        // find the first frame which is related to the application.
        for (i in 1 until frames.size) {
            if (predicateToExit(frames[i])) {
                return null
            }
            if (predicateToMatch(frames[i])) {
                return StackCallerContext(
                    className = frames[i].className,
                    methodName = frames[i].methodName,
                    lineNumer = frames[i].lineNumber
                )
            }
        }
        return null
    }
}
