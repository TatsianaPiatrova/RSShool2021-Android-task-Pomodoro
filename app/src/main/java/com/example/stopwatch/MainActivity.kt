package com.example.stopwatch

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.stopwatch.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), StopwatchListener, LifecycleObserver {

    private lateinit var binding: ActivityMainBinding

    private val stopwatchAdapter = StopwatchAdapter(this)
    private val stopwatches = mutableListOf<Stopwatch>()
    private var nextId = 0
    private var timerId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = stopwatchAdapter
        }

        binding.addNewStopwatchButton.setOnClickListener {
            var time = binding.hours.text.toString().toLongOrNull()?.let {
            binding.hours.text.toString().toLong() * 1000L * 60L * 60L
        } ?: 0L

            time += binding.minutes.text.toString().toLongOrNull()?.let {
                binding.minutes.text.toString().toLong() * 1000L * 60L
            } ?: 0L

            time += binding.seconds.text.toString().toLongOrNull()?.let {
                binding.seconds.text.toString().toLong() * 1000L
            } ?: 0L
            stopwatches.add(Stopwatch(nextId++, time, currentMs = time, isStarted = false))
            stopwatchAdapter.submitList(stopwatches.toList())
        }
    }

    override fun start(id: Int) {
        timerId = id
        changeStopwatch(id, null, true)
    }

    override fun stop(id: Int, currentMs: Long) {
        if(id == timerId)
            timerId = -1
        changeStopwatch(id, currentMs, false)
    }

    override fun delete(id: Int) {
        if(id == timerId)
            timerId = -1
        stopwatches.remove(stopwatches.find { it.id == id })
        stopwatchAdapter.submitList(stopwatches.toList())
    }

    private fun changeStopwatch(id: Int, currentMs: Long?, isStarted: Boolean) {
        val newTimers = mutableListOf<Stopwatch>()
        stopwatches.forEach {
            if (it.id == id) {
                newTimers.add(Stopwatch(it.id, it.time, currentMs ?: it.currentMs, isStarted, it.isFinish))
            } else if (it.isStarted) {
                newTimers.add(Stopwatch(it.id, it.time, currentMs ?: it.currentMs, isStarted = false, it.isFinish))
            }
            else {
                newTimers.add(it)
            }
        }
        stopwatchAdapter.submitList(newTimers)
        stopwatches.clear()
        stopwatches.addAll(newTimers)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        val startTime = stopwatchAdapter.currentList.find { it.id == timerId }?.currentMs ?: 0L
        if (startTime > 0) {
            val startIntent = Intent(this, ForegroundService::class.java)
            startIntent.putExtra(COMMAND_ID, COMMAND_START)
            startIntent.putExtra(STARTED_TIMER_TIME_MS, startTime)
            startService(startIntent)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        val stopIntent = Intent(this, ForegroundService::class.java)
        stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
        startService(stopIntent)
    }


}

    const val INVALID = "INVALID"
    const val COMMAND_START = "COMMAND_START"
    const val COMMAND_STOP = "COMMAND_STOP"
    const val COMMAND_ID = "COMMAND_ID"
    const val STARTED_TIMER_TIME_MS = "STARTED_TIMER_TIME"