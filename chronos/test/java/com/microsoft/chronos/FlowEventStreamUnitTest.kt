// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.chronos

import com.microsoft.chronos.api.*
import com.microsoft.chronos.stream.FlowEventStream
import com.microsoft.chronos.stream.FlowEventStreamConfig
import com.microsoft.chronos.stream.FlowStreamConfig
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import org.mockito.Mockito.`when`

/**
 * Unit Tests that run tests on [FlowEventStream], for working of all public apis
 * and [FlowEventStreamConfig] for testing config values individually.
 */
class FlowEventStreamUnitTest {

    private val coroutineDispatcher = TestCoroutineDispatcher()

    /*
        Base config values to work for all tests, applied to the map of all configs
     */
    private val baseTransform: (Map.Entry<String, FlowStreamConfig>) -> FlowStreamConfig = {
        it.value.copy(
            emitterCoroutineContext = coroutineDispatcher,
            collectorCoroutineContext = coroutineDispatcher
        )
    }

    @Test
    fun `Registered Collector gets event`() {
        coroutineDispatcher.runBlockingTest {
            val baseFlow: EventStream = FlowEventStream(
                FlowEventStreamConfig.default.copy(
                    configs = FlowEventStreamConfig.default.configs.mapValues(baseTransform)
                )
            )
            val mockEvent = ExecutorMeasureEvent()
            val collector = mock<EventCollector<MeasureEvent>>()

            baseFlow.registerCollector(collector, ExecutorMeasureEvent::class)
            baseFlow.post(mockEvent)
            verify(collector, atLeastOnce()).onEvent(mockEvent)
        }
    }

    @Test
    fun `Registered Transformer gets event`() {
        coroutineDispatcher.runBlockingTest {
            val baseFlow: EventStream = FlowEventStream(
                FlowEventStreamConfig.default.copy(
                    configs = FlowEventStreamConfig.default.configs.mapValues(baseTransform)
                )
            )
            val mockEvent = ExecutorMeasureEvent()

            val transformer = mock<EventTransformer<MeasureEvent>>()
            baseFlow.registerTransformer(transformer, ExecutorMeasureEvent::class)

            val collector = mock<EventCollector<MeasureEvent>>()
            baseFlow.registerCollector(collector, ExecutorMeasureEvent::class)

            baseFlow.post(mockEvent)
            verify(transformer, atLeastOnce()).transform(mockEvent)
        }
    }

    @Test
    fun `Filter Transformer filters event for collectors`() {
        coroutineDispatcher.runBlockingTest {
            val baseFlow: EventStream = FlowEventStream(
                FlowEventStreamConfig.default.copy(
                    configs = FlowEventStreamConfig.default.configs.mapValues(baseTransform)
                )
            )
            val mockEvent = ExecutorMeasureEvent()

            val transformer = mock<EventTransformer<MeasureEvent>>()
            `when`(transformer.transform(any())).doReturn(null) // Filter out all events
            baseFlow.registerTransformer(transformer, ExecutorMeasureEvent::class)

            val collector = mock<EventCollector<MeasureEvent>>()
            baseFlow.registerCollector(collector, ExecutorMeasureEvent::class)

            baseFlow.post(mockEvent)
            verify(collector, never()).onEvent(mockEvent)
        }
    }

    @Test
    fun `Disabled stream does not emit event`() {
        coroutineDispatcher.runBlockingTest {
            /*
            Transform for config values on top of base for the config test
            */
            val enabledTransform: (Map.Entry<String, FlowStreamConfig>) -> FlowStreamConfig = {
                baseTransform(it).copy(isStreamEnabled = false)
            }
            val baseFlow: EventStream = FlowEventStream(
                FlowEventStreamConfig.default.copy(
                    configs = FlowEventStreamConfig.default.configs.mapValues(enabledTransform)
                )
            )

            val mockEvent = ExecutorMeasureEvent()
            val collector = mock<EventCollector<MeasureEvent>>()

            baseFlow.registerCollector(collector, ExecutorMeasureEvent::class)
            baseFlow.post(mockEvent)
            verify(collector, never()).onEvent(mockEvent)
        }
    }
}
