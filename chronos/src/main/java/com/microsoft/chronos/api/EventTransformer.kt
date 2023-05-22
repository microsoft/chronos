// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.chronos.api

/**
 * Allows clients to attach Transformers to the [MeasureEvent] event stream
 */
interface EventTransformer<T : MeasureEvent> {
    /**
     * @param : MeasureEvent to transform on
     * @return transformed event or null the event needs to be filtered
     */
    fun transform(event: T): T?
}
