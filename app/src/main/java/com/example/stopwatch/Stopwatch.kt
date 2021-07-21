package com.example.stopwatch

data class Stopwatch(
    val id: Int,
    val time:Long,
    var currentMs: Long,
    var isStarted: Boolean,
    var isFinish: Boolean = false
)