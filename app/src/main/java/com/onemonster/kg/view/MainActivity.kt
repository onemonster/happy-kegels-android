package com.onemonster.kg.view

import android.os.Bundle
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.design.widget.BottomNavigationView
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.onemonster.kg.R
import com.onemonster.kg.util.Ticker
import com.onemonster.kg.util.visible
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val TICK_LENGTH = 1000L

    private val BREATH_TICKS = 3
    private val MUSCLE_TICKS = 12
    private val CYCLE_TICKS = 24
    private val SESSION_TICKS = 144
    private val SESSION_LENGTH = SESSION_TICKS * TICK_LENGTH

    private val READY_TICKS = 4
    private val RESTART_TICKS = 2

    private var state: State = State.IDLE
    private lateinit var ticker: Ticker

    private lateinit var inhaleAnimation: Animation
    private lateinit var exhaleAnimation: Animation

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_info -> {
                info_screen.visible = true
                main_screen.visible = false
                stat_screen.visible = false
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_main -> {
                info_screen.visible = false
                main_screen.visible = true
                stat_screen.visible = false
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_stat -> {
                info_screen.visible = false
                main_screen.visible = false
                stat_screen.visible = true
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inhaleAnimation = AnimationUtils.loadAnimation(this, R.anim.inhale_animation)
        exhaleAnimation = AnimationUtils.loadAnimation(this, R.anim.exhale_animation)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        setViews()
        setEvents()
        setTicker()

    }

    private fun setViews(ticks: Int = 0) {
        val breathStart = ticks % BREATH_TICKS == 0
        val cycleLeftSec = MUSCLE_TICKS - (ticks % MUSCLE_TICKS)
        val sessionLeftSec = SESSION_TICKS - ticks

        // handle visibility
        when (state) {
            State.IDLE, State.START -> {
                text_breath.visible = false
                text_muscle.visible = false
                text_session.visible = false
            }
            State.RESTART,
            State.INHALE_HOLD,
            State.EXHALE_HOLD,
            State.INHALE_REST,
            State.EXHALE_REST,
            State.PAUSE -> {
                text_breath.visible = true
                text_muscle.visible = true
                text_session.visible = true
            }
        }

        // handle background and text
        when (state) {
            State.IDLE -> {
                main_screen.setBackgroundColor(color(R.color.colorInhale))
                button_main.text = getString(R.string.session_start)
                button_main.background = drawable(R.drawable.button_exhale)
            }
            State.START -> {
                main_screen.setBackgroundColor(color(R.color.colorInhale))
                button_main.text = resources.getStringArray(R.array.start_count_down)[ticks]
                button_main.background = drawable(R.drawable.button_exhale)
            }
            State.RESTART -> {
                button_main.text = resources.getStringArray(R.array.restart_count_down)[ticks]
            }
            State.INHALE_HOLD -> {
                main_screen.setBackgroundColor(color(R.color.colorHold))
                text_session.text = getString(R.string.time_left, sessionLeftSec)
                text_muscle.text = getString(R.string.muscle_hold)
                text_breath.text = getString(R.string.breath_inhale)
                button_main.text = cycleLeftSec.toString()
                button_main.background = drawable(R.drawable.button_inhale)
            }
            State.EXHALE_HOLD -> {
                main_screen.setBackgroundColor(color(R.color.colorHold))
                text_session.text = getString(R.string.time_left, sessionLeftSec)
                text_muscle.text = getString(R.string.muscle_hold)
                text_breath.text = getString(R.string.breath_exhale)
                button_main.text = cycleLeftSec.toString()
                button_main.background = drawable(R.drawable.button_exhale)
            }
            State.INHALE_REST -> {
                main_screen.setBackgroundColor(color(R.color.colorRest))
                text_session.text = getString(R.string.time_left, sessionLeftSec)
                text_muscle.text = getString(R.string.muscle_rest)
                text_breath.text = getString(R.string.breath_inhale)
                button_main.text = cycleLeftSec.toString()
                button_main.background = drawable(R.drawable.button_inhale)
            }
            State.EXHALE_REST -> {
                main_screen.setBackgroundColor(color(R.color.colorRest))
                text_session.text = getString(R.string.time_left, sessionLeftSec)
                text_muscle.text = getString(R.string.muscle_rest)
                text_breath.text = getString(R.string.breath_exhale)
                button_main.text = cycleLeftSec.toString()
                button_main.background = drawable(R.drawable.button_exhale)
            }
            State.PAUSE -> {
                main_screen.setBackgroundColor(color(R.color.colorExhale))
                button_main.text = getString(R.string.session_paused)
                button_main.background = drawable(R.drawable.button_inhale)
            }
        }

        // handle animation
        when (state) {
            State.INHALE_HOLD, State.INHALE_REST -> {
                if (breathStart) {
                    button_main.startAnimation(inhaleAnimation)
                }
            }

            State.EXHALE_HOLD, State.EXHALE_REST -> {
                if (breathStart) {
                    button_main.startAnimation(exhaleAnimation)
                }
            }
        }
    }

    private fun setEvents() {
        button_main.setOnClickListener {
            when (state) {
                State.IDLE -> {
                    state = State.START
                    setViews()
                    ticker.start(READY_TICKS) { ticks ->
                        setViews(ticks)
                    }
                }
                State.INHALE_HOLD,
                State.EXHALE_HOLD,
                State.INHALE_REST,
                State.EXHALE_REST -> {
                    ticker.pause()
                    inhaleAnimation.cancel()
                    exhaleAnimation.cancel()
                    state = State.PAUSE
                    setViews()
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
    }

    private fun setTicker() {
        ticker = object : Ticker(SESSION_LENGTH, TICK_LENGTH) {
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

    fun drawable(@DrawableRes id: Int) = ContextCompat.getDrawable(this, id)
    fun color(@ColorRes id: Int) = ContextCompat.getColor(this, id)
}