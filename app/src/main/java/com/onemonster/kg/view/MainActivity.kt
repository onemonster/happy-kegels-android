package com.onemonster.kg.view

import android.os.Bundle
import android.os.CountDownTimer
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.design.widget.BottomNavigationView
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.onemonster.kg.R
import com.onemonster.kg.util.visible
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val BREATH_INTERVAL_SEC = 3  //   3 seconds
    private val MUSCLE_INTERVAL_SEC = 12 //  12 seconds
    private val CYCLE_LENGTH_SEC = 24    //  24 seconds
    private val SESSION_LENGTH_SEC = 144 // 144 seconds
    private val READY_LENGTH_SEC = 3     //   3 seconds
    private val COUNT_DOWN_SEC = 1       //   1 second

    private val BREATH_INTERVAL_MIL = BREATH_INTERVAL_SEC * 1000L
    private val MUSCLE_INTERVAL_MIL = MUSCLE_INTERVAL_SEC * 1000L
    private val CYCLE_LENGTH_MIL = CYCLE_LENGTH_SEC * 1000L
    private val SESSION_LENGTH_MIL = SESSION_LENGTH_SEC * 1000L
    private val READY_LENGTH_MIL = READY_LENGTH_SEC * 1000L
    private val COUNT_DOWN_MIL = COUNT_DOWN_SEC * 1000L

    private var state: State = State.IDLE
    private var startCountDownTimer: CountDownTimer? = null
    private var mainCountDownTimer: CountDownTimer? = null

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
        setMainViews()
        setMainEvents()
        setMainCountDownTimers()

    }

    private fun setMainViews(ticks: Int = 0) {
        val startIndex = ticks
        val breathStart = ticks % BREATH_INTERVAL_SEC == 0
        val cycleLeftSec = MUSCLE_INTERVAL_SEC - (ticks % MUSCLE_INTERVAL_SEC)
        val sessionLeftSec = SESSION_LENGTH_SEC - ticks

        // handle visibility
        when (state) {
            State.IDLE, State.START -> {
                text_breath.visible = false
                text_muscle.visible = false
                text_session.visible = false
            }
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
                button_main.text = resources.getStringArray(R.array.start_count_down)[startIndex]
                button_main.background = drawable(R.drawable.button_exhale)
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

    private fun setMainEvents() {
        button_main.setOnClickListener {
            when (state) {
                State.IDLE -> {
                    state = State.START
                    setMainViews()
                    startCountDownTimer?.start()
                }
                State.INHALE_HOLD,
                State.EXHALE_HOLD,
                State.INHALE_REST,
                State.EXHALE_REST -> {
//                    state = State.PAUSE
                }
            }
        }
    }

    private fun setMainCountDownTimers() {
        startCountDownTimer = object : CountDownTimer(READY_LENGTH_MIL + COUNT_DOWN_MIL, COUNT_DOWN_MIL) {
            private var ticks = 0

            override fun onFinish() {
                state = State.INHALE_HOLD
                mainCountDownTimer?.start()
                ticks = 0
            }

            override fun onTick(millisUntilFinished: Long) {
                setMainViews(ticks)
                ticks++
            }
        }

        mainCountDownTimer = object : CountDownTimer(SESSION_LENGTH_MIL, COUNT_DOWN_MIL) {
            private var ticks = 0

            override fun onFinish() {
                state = State.IDLE
                setMainViews()
                ticks = 0
                // TODO: show popup, update stats
            }

            override fun onTick(millisUntilFinished: Long) {
                when (ticks % CYCLE_LENGTH_SEC) {
                    0, 6 -> state = State.INHALE_HOLD
                    3, 9 -> state = State.EXHALE_HOLD
                    12, 18 -> state = State.INHALE_REST
                    15, 21 -> state = State.EXHALE_REST
                }
                setMainViews(ticks)
                ticks++
            }
        }
    }

    enum class State {
        IDLE,
        START,
        INHALE_HOLD,
        EXHALE_HOLD,
        INHALE_REST,
        EXHALE_REST,
        PAUSE
    }

    fun drawable(@DrawableRes id: Int) = ContextCompat.getDrawable(this, id)
    fun color(@ColorRes id: Int) = ContextCompat.getColor(this, id)
}