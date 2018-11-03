package com.onemonster.kg.util

import android.os.CountDownTimer

abstract class Ticker(private val millisInFuture: Long, private val countDownInterval: Long) {
    private var millisRemaining = millisInFuture
    private var ticks = 0

    private var countDownTimer: CountDownTimer? = null
    private var delayCountDownTimer: CountDownTimer? = null

    private var isPaused = true

    private fun createCountDownTimer() {
        countDownTimer = object : CountDownTimer(millisRemaining, countDownInterval) {
            override fun onTick(millisUntilFinished: Long) {
                millisRemaining = millisUntilFinished
                this@Ticker.onTick(ticks)
                ticks += 1
            }

            override fun onFinish() {
                this@Ticker.onFinish()
                millisRemaining = millisInFuture
                ticks = 0
                isPaused = true
            }
        }
    }

    abstract fun onTick(ticks: Int)
    abstract fun onFinish()

    fun cancel() {
        countDownTimer?.cancel()
        millisRemaining = millisInFuture
        ticks = 0
        isPaused = true
    }

    @Synchronized
    fun start(delayTicks: Int = 0, onTick: ((Int) -> Unit)? = null): Ticker {
        if (isPaused) {
            isPaused = false
            delayCountDownTimer = object : CountDownTimer(delayTicks * countDownInterval, countDownInterval) {
                var ticks = 0
                override fun onTick(millisUntilFinished: Long) {
                    onTick?.invoke(ticks)
                    ticks += 1
                }

                override fun onFinish() {
                    createCountDownTimer()
                    countDownTimer?.start()
                }
            }.start()
        }
        return this
    }

    fun pause() {
        if (!isPaused) {
            delayCountDownTimer?.cancel()
            countDownTimer?.cancel()
        }
        isPaused = true
    }
}