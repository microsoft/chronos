// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.chronossample

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.microsoft.chronos.BaseExecutorConfig
import com.microsoft.chronos.ExecutorName
import com.microsoft.chronos.Executors
import com.microsoft.chronos.api.*
import com.microsoft.chronos.experiments.CustomExperiment
import com.microsoft.chronos.stream.FlowEventStream
import com.microsoft.chronossample.databinding.ActivityMain2Binding
import kotlinx.coroutines.GlobalScope

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMain2Binding
    private lateinit var executor: IExecutors
    private lateinit var eventStream: EventStream

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main2)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        executor = Executors(
            eventStream = getEventStream(ExecutorEventsConfig()),
            config = getExecutorConfig( null)      //No Experiment
        )
        executor.getExecutor(ExecutorName.BACKGROUND).execute {
            Thread.sleep(1000)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main2)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    private fun getEventStream(
        config: ExecutorEventsConfig
    ): EventStream {
        return FlowEventStream(
            ExecutorEventsConfig.toFlowEventStreamConfig(
                config,
                GlobalScope
            )
        ).apply {
            registerTransformer(object : EventTransformer<MeasureEvent> {
                override fun transform(event: MeasureEvent): MeasureEvent? {
                    TODO("Not yet implemented")
                }
            }, MeasureEvent::class)
            registerCollector(
                object : EventCollector<MeasureEvent> {
                    override fun onEvent(event: MeasureEvent) {
                        TODO("Not yet implemented")
                    }
                }, MeasureEvent::class
            )
        }
    }

    private fun getExecutorConfig(args: String?): ExecutorConfig {
        return args?.let {
            return CustomExperiment.apply(BaseExecutorConfig, it)
        } ?: BaseExecutorConfig
    }
}