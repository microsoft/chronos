// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.chronos.stream

import com.microsoft.chronos.api.EventCollector
import com.microsoft.chronos.api.EventTransformer
import com.microsoft.chronos.api.MeasureEvent
import com.microsoft.chronos.api.StreamConfig
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch

data class FlowStreamConfig(
    override val isStreamEnabled: Boolean = true,
    override val streamBufferCapacity: Int = 500,
    override val debounceDelay: Long = 0,
    /**
     * Scope to run the [Flow]. See [CoroutineScope] for more details
     * GlobalScope by default to let it send events even if the app is minimised
     */
    val coroutineScope: CoroutineScope = GlobalScope,
    /**
     * [CoroutineContext]s i.e. Mandatory Dispatchers that collectors and poster coroutines run on
     */
    val collectorCoroutineContext: CoroutineContext = Dispatchers.Default,
    val emitterCoroutineContext: CoroutineContext = Dispatchers.Default
) : StreamConfig

/**
 * Implementation per event to allow us to configure each stream for the event settings
 * such as suppression, enabling and different collection/transformation mechanism
 */
class Stream(private val streamConfig: FlowStreamConfig) {
    private val transformerList = mutableListOf<EventTransformer<MeasureEvent>>()

    private val stream = MutableSharedFlow<MeasureEvent>(
        extraBufferCapacity = streamConfig.streamBufferCapacity
    )

    fun registerCollector(collector: EventCollector<MeasureEvent>) {
        if (!streamConfig.isStreamEnabled) {
            return
        }
        streamConfig.coroutineScope.launch(
            context = streamConfig.collectorCoroutineContext,
            block = {
                stream.mapNotNull { event ->
                    var res: MeasureEvent? = event
                    for (t in transformerList) {
                        res?.let {
                            res = t.transform(it)
                        } ?: break
                    }
                    res
                }
                    .debounce(streamConfig.debounceDelay)
                    .collect {
                        collector.onEvent(it)
                    }
            }
        )
    }

    fun registerTransformer(transformer: EventTransformer<MeasureEvent>) {
        if (!streamConfig.isStreamEnabled) {
            return
        }
        transformerList.add(transformer)
    }

    fun post(event: MeasureEvent) {
        if (!streamConfig.isStreamEnabled) {
            return
        }
        streamConfig.coroutineScope.launch(
            context = streamConfig.emitterCoroutineContext,
            block = {
                stream.emit(event)
            }
        )
    }
}
