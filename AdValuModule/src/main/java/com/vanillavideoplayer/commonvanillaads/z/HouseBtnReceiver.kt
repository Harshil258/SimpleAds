package com.vanillavideoplayer.commonvanillaads.z

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.vanillavideoplayer.commonvanillaads.app.SimpleApplicationClass

class HouseBtnReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_CLOSE_SYSTEM_DIALOGS) {
            val reason = intent.getStringExtra("reason")

            if (reason != null && (reason == "homekey" || reason == "recentapps")) {
                val result = SimpleApplicationClass.simpleApplicationClassInstance.returnGoogleInterstitialCls()
                SimpleApplicationClass.isAppInForeground = false
//                try {
//                    if (result!!.timer != null) {
//                        result!!.timer!!.pauseCDTimer()
//                        result!!.timer!!.cancelCDTimer()
//                    }
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }

                result!!.stopLoadingDialog()
            }
        }
    }
}