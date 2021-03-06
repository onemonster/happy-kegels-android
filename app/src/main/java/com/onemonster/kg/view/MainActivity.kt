package com.onemonster.kg.view

import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.onemonster.kg.R
import com.onemonster.kg.util.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private var state: State = State.IDLE
    private lateinit var ticker: Ticker

    private lateinit var inhaleAnimation: Animation
    private lateinit var exhaleAnimation: Animation

    private lateinit var kgPreference: KGPreference

    private lateinit var infoDialog: InfoDialog
    private lateinit var finishDialog: FinishDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        MobileAds.initialize(this, getString(R.string.GOOGLE_AD_MOB_APP_ID))
        inhaleAnimation = AnimationUtils.loadAnimation(this, R.anim.inhale_animation)
        exhaleAnimation = AnimationUtils.loadAnimation(this, R.anim.exhale_animation)

        val sharedPreferences = this.getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE)

        kgPreference = KGPreference(sharedPreferences)

        kgPreference.cyclesPerSessions = MEDIUM_SESSIONS

        infoDialog = InfoDialog(this)
        infoDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        finishDialog = FinishDialog(this)
        finishDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        loadBannerAd()

        setViews()
        setEvents()
        setTicker()
    }

    override fun onPause() {
        super.onPause()
        if (!ticker.isPaused) {
            pause()
        }
    }

    private fun loadBannerAd() {
        val adRequest = AdRequest.Builder().build()
        ad_view.loadAd(adRequest)
    }

    private fun setViews(ticks: Int = 0) {
        val breathStart = ticks % BREATH_TICKS == 0
        val muscleStart = ticks % MUSCLE_TICKS == 0
        val isMouthOpen = ticks % 2 == 1
        val cycleLeftSec = MUSCLE_TICKS - (ticks % MUSCLE_TICKS)
        val sessionLeftSec = kgPreference.cyclesPerSessions * CYCLE_TICKS - ticks

        // handle visibility
        text_restart.visible = state == State.PAUSE
        text_session_left.visible = !inState(State.IDLE, State.PAUSE, State.RESTART, State.START)
        text_session_left.text = getString(R.string.time_left, sessionLeftSec)

        // handle background and text
        when (state) {
            State.IDLE -> {
                image_main.setImageResource(R.drawable.idle_mouth_close)
                text_main.text = getString(R.string.session_start)
            }
            State.START -> {
                val strings = resources.getStringArray(R.array.start_count_down)
                text_main.text = strings.getOrElse(ticks) { strings.last() }
                image_main.setImageResource(if (isMouthOpen) {
                    R.drawable.idle_mouth_open
                } else {
                    R.drawable.idle_mouth_close
                })
            }
            State.RESTART -> {
                val strings = resources.getStringArray(R.array.restart_count_down)
                text_main.text = strings.getOrElse(ticks) { strings.last() }
                image_main.setImageResource(if (isMouthOpen) {
                    R.drawable.idle_mouth_open
                } else {
                    R.drawable.idle_mouth_close
                })
            }
            State.INHALE_HOLD -> {
                image_main.setImageResource(R.drawable.inhale_hold)
                if (muscleStart) {
                    text_main.text = getString(R.string.muscle_hold)
                } else {
                    text_main.text = cycleLeftSec.toString()
                }
            }
            State.EXHALE_HOLD -> {
                image_main.setImageResource(R.drawable.exhale_hold)
                text_main.text = cycleLeftSec.toString()
            }
            State.INHALE_REST -> {
                image_main.setImageResource(R.drawable.inhale_rest)
                if (muscleStart) {
                    text_main.text = getString(R.string.muscle_rest)
                } else {
                    text_main.text = cycleLeftSec.toString()
                }
            }
            State.EXHALE_REST -> {
                image_main.setImageResource(R.drawable.exhale_rest)
                text_main.text = cycleLeftSec.toString()
            }
            State.PAUSE -> {
                image_main.setImageResource(R.drawable.idle_mouth_close)
                text_main.text = getString(R.string.session_paused)
            }
        }

        // handle animation
        when (state) {
            State.INHALE_HOLD, State.INHALE_REST -> {
                if (breathStart) {
                    image_main.startAnimation(inhaleAnimation)
                }
            }

            State.EXHALE_HOLD, State.EXHALE_REST -> {
                if (breathStart) {
                    image_main.startAnimation(exhaleAnimation)
                }
            }
        }
    }

    private fun setEvents() {
        button_info.setOnClickListener {
            infoDialog.show()
            if (!ticker.isPaused) {
                pause()
            }
        }
        button_main.setOnClickListener {
            when (state) {
                State.IDLE -> {
                    state = State.START
                    setViews()
                    ticker.start(READY_TICKS) { ticks ->
                        setViews(ticks)
                    }
                }
                State.START -> stopStart()
                State.RESTART -> stopRestart()
                State.INHALE_HOLD,
                State.EXHALE_HOLD,
                State.INHALE_REST,
                State.EXHALE_REST -> {
                    pause()
                }
                State.PAUSE -> {
                    state = State.RESTART
                    setViews()
                    ticker.start(RESTART_TICKS) { ticks ->
                        setViews(ticks)
                    }
                }
            }
        }
        text_restart.setOnClickListener {
            stopStart()
        }
    }

    private fun setTicker() {
        ticker = object : Ticker(
                kgPreference.cyclesPerSessions * CYCLE_TICKS * TICK_LENGTH,
                TICK_LENGTH,
                { window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) },
                { window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) },
                { finishDialog.show(kgPreference.registerSessionDoneToday()) }
        ) {
            override fun onTick(ticks: Int) {
                when (ticks % CYCLE_TICKS) {
                    in BREATH_TICKS * 0 until BREATH_TICKS * 1, in BREATH_TICKS * 2 until BREATH_TICKS * 3 -> state = State.INHALE_HOLD
                    in BREATH_TICKS * 1 until BREATH_TICKS * 2, in BREATH_TICKS * 3 until BREATH_TICKS * 4 -> state = State.EXHALE_HOLD
                    in BREATH_TICKS * 4 until BREATH_TICKS * 5, in BREATH_TICKS * 6 until BREATH_TICKS * 7 -> state = State.INHALE_REST
                    in BREATH_TICKS * 5 until BREATH_TICKS * 6, in BREATH_TICKS * 7 until BREATH_TICKS * 8 -> state = State.EXHALE_REST
                }
                setViews(ticks)
            }

            override fun onFinish() {
                state = State.IDLE
                setViews()
            }
        }
    }

    private fun stopStart() {
        ticker.pause()
        ticker.cancel()
        state = State.IDLE
        setViews()
    }

    private fun stopRestart() {
        ticker.pause()
        state = State.PAUSE
        setViews()
    }

    private fun pause() {
        ticker.pause()
        inhaleAnimation.cancel()
        exhaleAnimation.cancel()
        state = State.PAUSE
        setViews()
    }

    enum class State {
        IDLE,
        START,
        RESTART,
        INHALE_HOLD,
        EXHALE_HOLD,
        INHALE_REST,
        EXHALE_REST,
        PAUSE
    }

    private fun inState(vararg states: State): Boolean {
        val currentState = state
        for (state in states) {
            if (state == currentState) {
                return true
            }
        }
        return false
    }

    fun drawable(@DrawableRes id: Int) = ContextCompat.getDrawable(this, id)
    fun color(@ColorRes id: Int) = ContextCompat.getColor(this, id)
}