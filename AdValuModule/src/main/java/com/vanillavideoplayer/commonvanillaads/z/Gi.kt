package com.vanillavideoplayer.commonvanillaads.z

import android.content.Context
import android.net.ConnectivityManager
import android.os.SystemClock
import android.util.Log

class Gi {

    fun networkAvailableChheKeNai(context: Context): Boolean {
        return try {
            val manager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = manager.activeNetworkInfo
            var isAvailable = false
            if (networkInfo != null && networkInfo.isConnectedOrConnecting) {
                isAvailable = true
            }
            isAvailable
        } catch (e: Exception) {
            false
        }
    }


    fun checkForMultipleClick(duration: Long): Boolean {
        if (SystemClock.elapsedRealtime() - lastClickMilles < duration) {
            return true
        }
        lastClickMilles = SystemClock.elapsedRealtime()
        return false
    }

    companion object {
        private var lastClickMilles: Long = 0
    }




}