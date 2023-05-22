// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.chronos.util

import kotlin.math.max
import kotlin.math.min

/**
 * Keeps running metric for optimised handling. Forces client to provide Double
 * since it is the highest precision floating constant to handle all values.
 * @param initialValue : Starting value else taken as 0.0d
 */
sealed class RunningMetric(val initialValue: Double = 0.0) {
    var currentValue: Double = initialValue

    /**
     * Updates the currentValue to the latest metric after calculation for consumption.
     */
    abstract fun update(newValue: Double)

    /**
     * Computes and keeps average of the RunningMetric
     */
    class Average(val initialVal: Double = 0.0) : RunningMetric(initialVal) {
        var countUpdated: Int = 0
        override fun update(newValue: Double) {
            currentValue += (newValue - currentValue) / ++countUpdated
        }
    }

    /**
     * Keeps a median of the metric i.e. middle of the sequence
     */
    class Median(val initialVal: Double = 0.0) : RunningMetric(initialVal) {
        override fun update(newValue: Double) {
        }
    }

    /**
     * Keeps the running minimum of the sequence
     */
    class Minimum(val initialVal: Double = 0.0) : RunningMetric(initialVal) {
        override fun update(newValue: Double) {
            currentValue = min(newValue, currentValue)
        }
    }

    /**
     * Keeps the running maximum of the sequence
     */
    class Maximum(val initialVal: Double = 0.0) : RunningMetric(initialVal) {
        override fun update(newValue: Double) {
            currentValue = max(newValue, currentValue)
        }
    }
}
