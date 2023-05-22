// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.chronos.stream

import com.microsoft.chronos.api.*
import kotlin.reflect.KClass

data class FlowEventStreamConfig(
    override val configs: Map<String, FlowStreamConfig>
) : EventStreamConfig {
    companion object {
        val default = FlowEventStreamConfig(
            mapOfEventNameToSubclass.entries.associateBy({ it.key }, { FlowStreamConfig() })
        )
    }
}

/***
 * Class to encapsulate a unidirectional SharedFlow event stream for [MeasureEvent]s.
 * Events can be posted, transformed or collected.
 * @param streamConfig - Config to configure the stream settings
 */
class FlowEventStream(streamConfig: FlowEventStreamConfig) : EventStream {

    private val streams: Map<KClass<out MeasureEvent>, Stream> =
        mapOfEventNameToSubclass.entries.associateBy(
            { it.value },
            {
                Stream(
                    streamConfig.configs.get(it.key) ?: FlowStreamConfig()
                )
            }
        )

    override fun registerCollector(
        collector: EventCollector<MeasureEvent>,
        event: KClass<out MeasureEvent>
    ) {
        streams[event]?.registerCollector(collector)
    }

    override fun registerTransformer(
        transformer: EventTransformer<MeasureEvent>,
        event: KClass<out MeasureEvent>
    ) {
        streams[event]?.registerTransformer(transformer)
    }

    override fun post(event: MeasureEvent) {
        streams[event::class]?.post(event)
    }
}