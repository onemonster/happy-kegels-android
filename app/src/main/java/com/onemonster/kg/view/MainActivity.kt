package com.onemonster.kg.view

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.design.widget.TabLayout
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
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

    private lateinit var screens: List<View>

    private lateinit var kgPreference: KGPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        MobileAds.initialize(this, "ca-app-pub-3315479630821302~5829933722")
        screens = listOf(screen_info, screen_main, screen_etc)
        inhaleAnimation = AnimationUtils.loadAnimation(this, R.anim.inhale_animation)
        exhaleAnimation = AnimationUtils.loadAnimation(this, R.anim.exhale_animation)

        screens.forEach { it.visible = false }

        val sharedPreferences = this.getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE)

        kgPreference = KGPreference(sharedPreferences)

        navigation.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab?.position?.let { screens[it].visible = false }
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.position?.let {
                    screens[it].visible = true
                    kgPreference.tabIndex = it
                }
                if (!inState(State.IDLE, State.PAUSE)) {
                    button_main.performClick()
                }
            }
        })

        navigation.getTabAt(kgPreference.tabIndex)?.select()
        screens[kgPreference.tabIndex].visible = true

        setViews()

        setMainViews()
        setMainEvents()
        setMainTicker()

        setInfoViews()

        setEtcViews()
        setEtcEvents()
    }

    private fun setViews() {
        val adRequest = AdRequest.Builder().build()
        ad_view.loadAd(adRequest)
    }

    private fun setMainViews(ticks: Int = 0) {
        val breathStart = ticks % BREATH_TICKS == 0
        val cycleLeftSec = MUSCLE_TICKS - (ticks % MUSCLE_TICKS)
        val sessionLeftSec = kgPreference.sessions * CYCLE_TICKS - ticks

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
                screen_main.setBackgroundColor(color(R.color.colorInhale))
                button_main.text = getString(R.string.session_start)
                button_main.background = drawable(R.drawable.button_exhale)
            }
            State.START -> {
                screen_main.setBackgroundColor(color(R.color.colorInhale))
                button_main.text = resources.getStringArray(R.array.start_count_down)[ticks]
                button_main.background = drawable(R.drawable.button_exhale)
            }
            State.RESTART -> {
                button_main.text = resources.getStringArray(R.array.restart_count_down)[ticks]
            }
            State.INHALE_HOLD -> {
                screen_main.setBackgroundColor(color(R.color.colorHold))
                text_session.text = getString(R.string.time_left, sessionLeftSec)
                text_muscle.text = getString(R.string.muscle_hold)
                text_breath.text = getString(R.string.breath_inhale)
                button_main.text = cycleLeftSec.toString()
                button_main.background = drawable(R.drawable.button_inhale)
            }
            State.EXHALE_HOLD -> {
                screen_main.setBackgroundColor(color(R.color.colorHold))
                text_session.text = getString(R.string.time_left, sessionLeftSec)
                text_muscle.text = getString(R.string.muscle_hold)
                text_breath.text = getString(R.string.breath_exhale)
                button_main.text = cycleLeftSec.toString()
                button_main.background = drawable(R.drawable.button_exhale)
            }
            State.INHALE_REST -> {
                screen_main.setBackgroundColor(color(R.color.colorRest))
                text_session.text = getString(R.string.time_left, sessionLeftSec)
                text_muscle.text = getString(R.string.muscle_rest)
                text_breath.text = getString(R.string.breath_inhale)
                button_main.text = cycleLeftSec.toString()
                button_main.background = drawable(R.drawable.button_inhale)
            }
            State.EXHALE_REST -> {
                screen_main.setBackgroundColor(color(R.color.colorRest))
                text_session.text = getString(R.string.time_left, sessionLeftSec)
                text_muscle.text = getString(R.string.muscle_rest)
                text_breath.text = getString(R.string.breath_exhale)
                button_main.text = cycleLeftSec.toString()
                button_main.background = drawable(R.drawable.button_exhale)
            }
            State.PAUSE -> {
                screen_main.setBackgroundColor(color(R.color.colorExhale))
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
                    ticker.start(READY_TICKS) { ticks ->
                        setMainViews(ticks)
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
                    setMainViews()
                    ticker.start(RESTART_TICKS) { ticks ->
                        setMainViews(ticks)
                    }
                }
            }
        }
    }

    private fun setMainTicker() {
        ticker = object : Ticker(kgPreference.sessions * CYCLE_TICKS * TICK_LENGTH, TICK_LENGTH) {
            override fun onTick(ticks: Int) {
                when (ticks % CYCLE_TICKS) {
                    in BREATH_TICKS * 0 until BREATH_TICKS * 1, in BREATH_TICKS * 2 until BREATH_TICKS * 3 -> state = State.INHALE_HOLD
                    in BREATH_TICKS * 1 until BREATH_TICKS * 2, in BREATH_TICKS * 3 until BREATH_TICKS * 4 -> state = State.EXHALE_HOLD
                    in BREATH_TICKS * 4 until BREATH_TICKS * 5, in BREATH_TICKS * 6 until BREATH_TICKS * 7 -> state = State.INHALE_REST
                    in BREATH_TICKS * 5 until BREATH_TICKS * 6, in BREATH_TICKS * 7 until BREATH_TICKS * 8 -> state = State.EXHALE_REST
                }
                setMainViews(ticks)
            }

            override fun onFinish() {
                state = State.IDLE
                setMainViews()
            }
        }
    }

    private fun setInfoViews() {
        val chapterSpan1 = SpannableString(getString(R.string.body_1))
        val chapter1Header1 = getString(R.string.header_1_1)
        val chapter1Header2 = getString(R.string.header_1_2)
        val chapter1Header3 = getString(R.string.header_1_3)
        val chapter1Header4 = getString(R.string.header_1_4)

        chapterSpan1.setSpan(
                RelativeSizeSpan(1.1f),
                chapterSpan1.indexOf(chapter1Header1),
                chapterSpan1.indexOf(chapter1Header1) + chapter1Header1.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        chapterSpan1.setSpan(
                RelativeSizeSpan(1.1f),
                chapterSpan1.indexOf(chapter1Header2),
                chapterSpan1.indexOf(chapter1Header2) + chapter1Header2.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        chapterSpan1.setSpan(
                RelativeSizeSpan(1.1f),
                chapterSpan1.indexOf(chapter1Header3),
                chapterSpan1.indexOf(chapter1Header3) + chapter1Header3.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        chapterSpan1.setSpan(
                RelativeSizeSpan(1.1f),
                chapterSpan1.indexOf(chapter1Header4),
                chapterSpan1.indexOf(chapter1Header4) + chapter1Header4.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        chapterSpan1.setSpan(
                StyleSpan(Typeface.BOLD),
                chapterSpan1.indexOf(chapter1Header1),
                chapterSpan1.indexOf(chapter1Header1) + chapter1Header1.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        chapterSpan1.setSpan(
                StyleSpan(Typeface.BOLD),
                chapterSpan1.indexOf(chapter1Header2),
                chapterSpan1.indexOf(chapter1Header2) + chapter1Header2.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        chapterSpan1.setSpan(
                StyleSpan(Typeface.BOLD),
                chapterSpan1.indexOf(chapter1Header3),
                chapterSpan1.indexOf(chapter1Header3) + chapter1Header3.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        chapterSpan1.setSpan(
                StyleSpan(Typeface.BOLD),
                chapterSpan1.indexOf(chapter1Header4),
                chapterSpan1.indexOf(chapter1Header4) + chapter1Header4.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        chapter_1.text = chapterSpan1
    }

    private fun setEtcViews() {
        renderDifficultyToggles()
    }

    private fun setEtcEvents() {
        toggle_difficulty_easy.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                kgPreference.sessions = EASY_SESSIONS
            }
            renderDifficultyToggles()
        }
        toggle_difficulty_medium.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                kgPreference.sessions = MEDIUM_SESSIONS
            }
            renderDifficultyToggles()
        }
        toggle_difficulty_hard.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                kgPreference.sessions = HARD_SESSIONS
            }
            renderDifficultyToggles()
        }
    }

    private fun stopStart() {
        ticker.pause()
        state = State.IDLE
        setMainViews()
    }

    private fun stopRestart() {
        ticker.pause()
        state = State.PAUSE
        setMainViews()
    }

    private fun pause() {
        ticker.pause()
        inhaleAnimation.cancel()
        exhaleAnimation.cancel()
        state = State.PAUSE
        setMainViews()
    }

    private fun renderDifficultyToggles() {
        when (kgPreference.sessions) {
            EASY_SESSIONS -> {
                toggle_difficulty_easy.isChecked = true
                toggle_difficulty_medium.isChecked = false
                toggle_difficulty_hard.isChecked = false
            }
            MEDIUM_SESSIONS -> {
                toggle_difficulty_easy.isChecked = false
                toggle_difficulty_medium.isChecked = true
                toggle_difficulty_hard.isChecked = false
            }
            HARD_SESSIONS -> {
                toggle_difficulty_easy.isChecked = false
                toggle_difficulty_medium.isChecked = false
                toggle_difficulty_hard.isChecked = true
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