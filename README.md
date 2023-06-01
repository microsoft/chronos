# Chronos

> Measure, experiment with & govern your threading layer with an iron-fist

Chronos (named after Greek god of time) is a thread orchestration, experimentation, monitoring and governance library for Android applications that work for large scale mobile apps. It enables a teams of developers on multiple features to utilise threading in a safe and controllable way.
- Addresses the thread explosion problem for large teams with a centralised threadpool management system.
- Improves the throughput of work by monitoring each threadpool via a central event stream firing multiple metrics to identify bottlenecks and high wait times.
- Allows teams to experiment with different threadpool configurations with respect to thread priority, pool size, keep alive time etc. and monitor the impact of these changes on the performance of the feature.
- Improves governance on large codebases by ensuring that all background work is done only on threadpools that are defined at initialisation via centralised configurations and linting.
- Provides a Base threading layer configuration for your app inspired from GCD out of the box that can be used by apps to prioritise work across Executors based on importance to the user.


## Usage
**1.** Create a BaseExecutorConfig for threadpools that would contain all the threadpools. Centralising this will ensure governance of background work is possible. Each Executor is represented by an ExecutorSetting and the name should be passed to the get() APIs to ensure this is used.
Please check ExecutorSettings for possible values and their defaults.

For eg, you can follow a [GCD](https://developer.apple.com/documentation/dispatch/dispatchqos) based threadpool approach for creating your threadling layer to run work on based on it's priority to the user like in iOS.

```kotlin
object BaseExecutorConfig : ExecutorConfig {
    @StringDef(
        ExecutorName.USER_INTERACTIVE,
        ExecutorName.USER_INITIATED
        // ...
    )
    annotation class ExecutorName {
        companion object {
            const val USER_INTERACTIVE = "USER_INTERACTIVE"
            const val USER_INITIATED = "USER_INITIATED"
            // ...
        }
    }
    override val executors = setOf(
        ExecutorSettings(
            executorId = ExecutorName.USER_INTERACTIVE,
            allowThreadTimeout = false,
            corePoolSize = Integer.MAX_VALUE,
            threadPriority = ExecutorPriority.PThreadPriority.MAX
        ),
        ExecutorSettings(
            executorId = ExecutorName.USER_INITIATED,
            allowThreadTimeout = false,
            corePoolSize = Integer.MAX_VALUE,
            threadPriority = ExecutorPriority.PThreadPriority.HIGH
        ),
        // ...
    )
}
```

**2.** Initialise an EventStream and register collectors to collect ExecutorEvent and ExecutionEvents from these threadpools. The EventStreamConfig will decide the monitoring related settings we will be using for these events. For eg, we can decide to collect events only when the app is in debug mode or with apply backpressure to the collectors. Transformers can be applied to add more metadata to certain events or filter some out. 

All events collected pass through all the transformers to the collectors.

```kotlin
    private fun getEventStream(): EventStream {
    return FlowEventStream(FlowEventStreamConfig.default).apply {
        registerTransformer(object : EventTransformer<MeasureEvent> {
            override fun transform(event: MeasureEvent): MeasureEvent? {
                event.meta.isForeground = true
            }
        }, MeasureEvent::class)
        registerCollector(
            object : EventCollector<MeasureEvent> {
                override fun onEvent(event: MeasureEvent) {
                    telemetry.send(event)
                }
            }, MeasureEvent::class
        )
    }
}
```

**3.** Initialise Executors class with EventStream and BaseExecutorConfig. Please check Experiments section to see how this BaseExecutorConfig can also be modified at runtime.

```kotlin
    private val executors = Executors(
        getExecutorConfig(args),
        getExecutorEventsConfig(args)
    )
```


**4.** Use Executors class at all places to post work as coroutines in Kotlin
```kotlin
    CoroutineScope(executors.getExecutor(ExecutorName.USER_INITIATED).asCoroutineDispatcher()).launch {
        someCriticalWork()
    }
```

or runnables in Java

```java
    executors
        .getExecutor(ExecutorName.USER_INITIATED)
        .execute(
            new Runnable() {
                @Override
                public void run() {
                    someCriticalWork();
                }
        }
    );
```

## Experimentation
Experiments can be enabled on the Executors with different ExecutorSettings via CustomExperiment.

Initialise ExecutorConfig by parsing your server side

```kotlin
    private fun getExecutorConfig(args: String?): ExecutorConfig {
        return args?.let {
            return CustomExperiment.apply(BaseExecutorConfig, it)
        } ?: BaseExecutorConfig
    }
```


## Contributing

This project welcomes contributions and suggestions.  Most contributions require you to agree to a
Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us
the rights to use your contribution. For details, visit https://cla.opensource.microsoft.com.

When you submit a pull request, a CLA bot will automatically determine whether you need to provide
a CLA and decorate the PR appropriately (e.g., status check, comment). Simply follow the instructions
provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/).
For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or
contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

## Trademarks

This project may contain trademarks or logos for projects, products, or services. Authorized use of Microsoft 
trademarks or logos is subject to and must follow 
[Microsoft's Trademark & Brand Guidelines](https://www.microsoft.com/en-us/legal/intellectualproperty/trademarks/usage/general).
Use of Microsoft trademarks or logos in modified versions of this project must not cause confusion or imply Microsoft sponsorship.
Any use of third-party trademarks or logos are subject to those third-party's policies.
