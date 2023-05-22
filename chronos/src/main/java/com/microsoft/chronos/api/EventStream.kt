// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.chronos.api

import kotlin.reflect.KClass

/**
 * Running event stream to post, transform and collect [MeasureEvent] via
 * [EventCollector] & [EventTransformer]
 */
interface EventStream {

    /**
     * @param collector : [EventCollector] implementations to process the event T passed
     * @param event : Class of [MeasureEvent] to attach this collector to
     */
    fun registerCollector(collector: EventCollector<MeasureEvent>, event: KClass<out MeasureEvent>)

    /**
     * @param transformer : [EventTransformer] implementation to process
     * @param event : Class of [MeasureEvent] to attach this transformer to
     */
    fun registerTransformer(transformer: EventTransformer<MeasureEvent>, event: KClass<out MeasureEvent>)

    /**
     * @param event : Posts the event to the stream for all collectors to process
     */
    fun post(event: MeasureEvent)
}

/**
 * Configuration for running the above [EventStream] which contains a list
 * of configs per stream identified by each event type. This enforces the implementers
 * to support all [StreamConfig] configurations on a per-event level.
 */
interface EventStreamConfig {
    /**
     * Map of event type name to it's configuration
     */
    val configs: Map<String, StreamConfig>
}
