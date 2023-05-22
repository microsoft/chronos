// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.chronos.util;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Int def to represent different task priorities in increasing priority relative to the actual
 * int value
 */
@IntDef({
        TaskPriority.TASK_PRIORITY_BACKGROUND,
        TaskPriority.TASK_PRIORITY_MEDIUM,
        TaskPriority.TASK_PRIORITY_HIGH,
        TaskPriority.TASK_PRIORITY_BLOCKING,
        TaskPriority.TASK_PRIORITY_MAIN
})
@Retention(RetentionPolicy.SOURCE)
public @interface TaskPriority {
    /*
     *    Background Priority : Default Priority which is lowest when no other priority is given
     */
    int TASK_PRIORITY_BACKGROUND = 0;
    /*
     *   Medium Priority : The tasks executed as last on the [TeamsAppLifecycleEvent] and can be starved if multiple events happen in a short span of time.
     *  Add Tasks that are okay to be skipped in this [TeamsAppLifecycleEvent] and might be executed by a later event.
     */
    int TASK_PRIORITY_MEDIUM = 1;
    /*
     *    High Priority Tasks : The highest priority critical background task that need to be finished on this lifecycle event.
     *   Add only the Tasks which are highest in priority and should not be starved if another [TeamsAppLifecycleEvent] occurs
     */
    int TASK_PRIORITY_HIGH = 2;
    /*
     *   Blocking Tasks : Tasks will be executed first in parallel and will block the main thread, till all of them are complete.
     *   Add only those Tasks which if not executed block the user journey or lead to a crash.
     */
    int TASK_PRIORITY_BLOCKING = 3;
    /*
     *  Main thread tasks : Tasks will be executed on the main thread before any of the below background tasks are scheduled.
     *  Executed synchronously and in the order of definition.
     */
    int TASK_PRIORITY_MAIN = 4;
}
