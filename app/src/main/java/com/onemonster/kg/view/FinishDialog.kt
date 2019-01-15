package com.onemonster.kg.view

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.onemonster.kg.R
import kotlinx.android.synthetic.main.popup_finish.*

class FinishDialog(context: Context) : Dialog(context) {
    companion object {
        private const val DAILY_GOAL = 4
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.popup_finish)
    }

    fun show(doneToday: Int) {
        super.show()
        if (doneToday < DAILY_GOAL) {
            text_daily_stat.text = context.getString(R.string.finish_daily_stat, doneToday, DAILY_GOAL)
        } else {
            text_daily_stat.text = context.getString(R.string.finish_daily_finished)
        }
        text_tip.text = context.getString(R.string.finish_tip, DAILY_GOAL)
    }
}