package com.onemonster.kg.view

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.onemonster.kg.R

class InfoDialog(context: Context) : Dialog(context) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.popup_info)
    }
}