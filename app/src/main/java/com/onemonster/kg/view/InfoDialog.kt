package com.onemonster.kg.view

import android.app.Dialog
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import com.onemonster.kg.R
import kotlinx.android.synthetic.main.popup_info.*

class InfoDialog(context: Context) : Dialog(context) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.popup_info)
        setViews()
    }

    private fun setViews() {
        val infoInhaleHoldSpan = SpannableString(context.getString(R.string.info_inhale_hold))
        val infoExhaleHoldSpan = SpannableString(context.getString(R.string.info_exhale_hold))
        val infoInhaleRestSpan = SpannableString(context.getString(R.string.info_inhale_rest))
        val infoExhaleRestSpan = SpannableString(context.getString(R.string.info_exhale_rest))

        val infoBoldHold = context.getString(R.string.info_bold_hold)
        val infoBoldHolding = context.getString(R.string.info_bold_holding)
        val infoBoldRest = context.getString(R.string.info_bold_rest)
        val infoBoldResting = context.getString(R.string.info_bold_resting)
        val infoBoldBreathIn = context.getString(R.string.info_bold_breath_in)
        val infoBoldBreathOut = context.getString(R.string.info_bold_breath_out)

        setBoldSpan(infoInhaleHoldSpan, infoBoldHold)
        setBoldSpan(infoInhaleHoldSpan, infoBoldBreathIn)

        setBoldSpan(infoExhaleHoldSpan, infoBoldHolding)
        setBoldSpan(infoExhaleHoldSpan, infoBoldBreathOut)

        setBoldSpan(infoInhaleRestSpan, infoBoldRest)
        setBoldSpan(infoInhaleRestSpan, infoBoldBreathIn)

        setBoldSpan(infoExhaleRestSpan, infoBoldResting)
        setBoldSpan(infoExhaleRestSpan, infoBoldBreathOut)

        text_inhale_hold.text = infoInhaleHoldSpan
        text_exhale_hold.text = infoExhaleHoldSpan
        text_inhale_rest.text = infoInhaleRestSpan
        text_exhale_rest.text = infoExhaleRestSpan
    }

    private fun setBoldSpan(span: SpannableString, bold: String) {
        val start = span.indexOf(bold)
        val end = start + bold.length
        val flags = Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        span.setSpan(StyleSpan(Typeface.BOLD), start, end, flags)
        span.setSpan(RelativeSizeSpan(1.05f), start, end, flags)
    }
}