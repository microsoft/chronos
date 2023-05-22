// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.chronos.api

/**
 * Configurations for EventStream implementations to
 */
interface StreamConfig {
    /**
     * Enable/Disable toggle for this stream, disabling this disables all posters,
     * collector callbacks.
     */
    val isStreamEnabled: Boolean

    /**
     * If collectors or transformers are slow, events are buffered till this capacity
     */
    val streamBufferCapacity: Int

    /**
     * The time the events would be skipped or clubbed together, this is there to control the backpressure
     * of streams which generate a lot of events
     */
    val debounceDelay: Long
}
