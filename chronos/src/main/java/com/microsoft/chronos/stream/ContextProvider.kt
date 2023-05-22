// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.chronos.stream

interface CallerContext

/**
 * CallerContext can be provided by any external implementer for [MeasureEvent]
 * The below implementation provides
 */
interface ContextProvider<I> {
    /**
     * Generate [CallerContext] from any input type. Default implementation in v1 is a String
     */
    fun provideCallerContext(input: I): CallerContext?
}
