package com.onemonster.kg.util

import android.content.SharedPreferences

class KGPreference(private val sharedPreferences: SharedPreferences) {
    var tabIndex: Int
        get() = readInt(PreferenceKey.TabIndex, MAIN_SCREEN_INDEX)
        set(value) = writeInt(PreferenceKey.TabIndex, value)


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
    TabIndex
}