// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.chronos.util;

import com.microsoft.chronos.priority.PriorityThreadPoolExecutor;
import com.microsoft.chronos.priority.HasTaskPriority;

/***
 * Utilities related to runnables and Bolts.Tasks
 */
public class RunnableUtils {

    /**
     * A runnable that can define its priority relative to other tasks queued in the same thread
     * pool {@link PriorityThreadPoolExecutor}. This runnable is intended to be used by
     * Priority data tasks that are user driven i.e. when a user opens a chat, the task to fetch
     * local data should be done using this Runnable with a higher TaskPriority than, say, a
     * contending data call or other busy work not required for UI updating queued in the same
     * thread pool. BaseViewData is ubiquitous and some long running tasks might be clogging the
     * queue
     *
     * <p>NOTE: This should not be used to prioritize non-user driven data tasks!!!
     *
     * @param <T> The task return type
     */
    public abstract static class PriorityRunnable<T>
            implements HasTaskPriority {
        private int mRunnablePriority;

        protected PriorityRunnable(@TaskPriority int threadPriority) {
            mRunnablePriority = threadPriority;
        }

        protected PriorityRunnable() {
            mRunnablePriority = TaskPriority.TASK_PRIORITY_MEDIUM;
        }

        public int getPriority() {
            return mRunnablePriority;
        }
    }
}
