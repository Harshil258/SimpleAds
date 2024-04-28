package com.googleadsdemo.SimpleAds.Utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.googleadsdemo.SimpleAds.Application.SimpleApplicationClass

class HouseBtnReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_CLOSE_SYSTEM_DIALOGS) {
            val reason = intent.getStringExtra("reason")

            if (reason != null && (reason == "homekey" || reason == "recentapps")) {
                val result = SimpleApplicationClass.simpleApplicationClassInstance.returnGoogleInterstitialCls()
                SimpleApplicationClass.isAppInForeground = false
                result!!.stopLoadingDialog()
            }
        }
    }
}