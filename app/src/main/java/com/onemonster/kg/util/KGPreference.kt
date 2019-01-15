package com.onemonster.kg.util

import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

class KGPreference(private val sharedPreferences: SharedPreferences) {
    var cyclesPerSessions: Int
        get() = readInt(PreferenceKey.CYCLES_PER_SESSIONS, MEDIUM_SESSIONS)
        set(value) = writeInt(PreferenceKey.CYCLES_PER_SESSIONS, value)

    private var today: String
        get() = readString(PreferenceKey.TODAY, "")
        set(value) = writeString(PreferenceKey.TODAY, value)

    var sessionsDoneToday: Int
        get() = readInt(PreferenceKey.SESSIONS_DONE_TODAY, 0)
        private set(value) = writeInt(PreferenceKey.SESSIONS_DONE_TODAY, value)

    fun registerSessionDoneToday(): Int {
        val date = SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().time)
        if (today == date) {
            sessionsDoneToday += 1
        } else {
            today = date
            sessionsDoneToday = 1
        }
        totalSessionsDone += 1
        return sessionsDoneToday
    }

    var totalSessionsDone: Int
        get() = readInt(PreferenceKey.TOTAL_SESSIONS_DONE, 0)
        private set(value) = writeInt(PreferenceKey.TOTAL_SESSIONS_DONE, value)

    private fun readInt(key: PreferenceKey, defaultValue: Int): Int =
            sharedPreferences.getInt(key.name, defaultValue)

    private fun writeInt(key: PreferenceKey, value: Int) {
        val edit = sharedPreferences.edit()
        edit.putInt(key.name, value)
        edit.commit()
    }

    private fun readString(key: PreferenceKey, defaultValue: String): String =
            sharedPreferences.getString(key.name, defaultValue)

    private fun writeString(key: PreferenceKey, value: String) {
        val edit = sharedPreferences.edit()
        edit.putString(key.name, value)
        edit.commit()
    }
}

enum class PreferenceKey {
    CYCLES_PER_SESSIONS,
    TODAY,
    SESSIONS_DONE_TODAY,
    TOTAL_SESSIONS_DONE
}