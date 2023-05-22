// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.chronos.api

/**
 * Allows EventCollector implementations to collect certain [MeasureEvent]
 * for processing events
 */
interface EventCollector<T : MeasureEvent> {
    /**
     * @param : MeasureEvent to listen to, this gets called when an event of
     * class T is posted
     */
    fun onEvent(event: T)
}
