package com.googleadsdemo.SimpleAds.Managers

import android.app.Activity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback
import com.googleadsdemo.SimpleAds.Application.SimpleApplicationClass
import com.googleadsdemo.SimpleAds.Interfaces.OpenAppCB
import com.googleadsdemo.SimpleAds.Utils.Gi
import com.googleadsdemo.SimpleAds.Utils.SharedPrefConfig
import com.googleadsdemo.SimpleAds.Utils.sharedPrefConfig
import java.util.Date

class OpenAppAdsManager {
    fun showOpenAppAd(activity: Activity, callBack: OpenAppCB) {
        if (!Gi().networkAvailableChheKeNai(activity)) {
            openAppAd = null
            boolAdShowing = false
            callBack.onDismissOpenApp(true)
            loadOpenApp(activity, callBack)
            return
        }
        if (isValidOpenAppStatus()) {
            if (isAdAvailable() || boolAdShowing) {
                if (!boolAdShowing) {
                    boolAdShowing = true
                    openAppAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdClicked() {
                            super.onAdClicked()
                            logEvent("onAdClicked", activity)
                        }

                        override fun onAdDismissedFullScreenContent() {
                            openAppAd = null
                            boolAdShowing = false
                            callBack.onDismissOpenApp(true)
                            loadOpenApp(activity, callBack)
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            openAppAd = null
                            boolAdShowing = false
                            callBack.onDismissOpenApp(true)
                            loadOpenApp(activity, callBack)
                        }

                        override fun onAdImpression() {
                            super.onAdImpression()
                            

                            logEvent("onAdImpression", activity)
                        }

                        override fun onAdShowedFullScreenContent() {
                            boolAdShowing = true
                        }
                    }
                    openAppAd?.show(activity)
                }
            } else {
                loadOpenApp(activity, callBack)
            }
        } else {
            boolAdShowing = false
        }
    }

    fun loadOpenApp(activity: Activity, callBack: OpenAppCB) {
        if (boolAdLoading || !isValidOpenAppStatus() || openAppAd != null) {
            return
        }

        openAppAdLoadCB = object : AppOpenAdLoadCallback() {
            override fun onAdLoaded(ad: AppOpenAd) {
                openAppAd = ad
                boolAdLoading = false
                loadTimeVal = Date().time
                if (SimpleApplicationClass.boolLauncherRunning) showOpenAppAd(activity, callBack)
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                boolAdLoading = false
            }

        }

        try {
            val adRequest = AdRequest.Builder().build()
            AppOpenAd.load(
                SimpleApplicationClass.simpleApplicationClassInstance!!.appContext,
                SimpleApplicationClass.liveActivity.sharedPrefConfig.appDetails.admobAppOpen,
                adRequest,
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                openAppAdLoadCB
            )
            boolAdLoading = true
        } catch (e: Exception) {
            boolAdLoading = false
        }
    }

    fun isValidOpenAppStatus(): Boolean {
        val appDetailModel = SimpleApplicationClass.liveActivity.sharedPrefConfig.appDetails
        return appDetailModel.let {
            it.adStatus.isNotBlank() && it.admobAppOpen.isNotBlank() && it.adStatus.trim().equals("ON", ignoreCase = true)
        } ?: false
    }

    fun isAdAvailable(): Boolean {
        return openAppAd != null && isLoadTimeLessThanNHours(4)
    }

    fun isLoadTimeLessThanNHours(hours: Long): Boolean {
        val dateDifference = Date().time - loadTimeVal
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * hours
    }

    companion object {
        var openAppAd: AppOpenAd? = null
        var boolAdShowing: Boolean = false
        var boolAdLoading: Boolean = false
        var shouldStopOpenApp: Boolean = false
        private var loadTimeVal: Long = 0

        private lateinit var openAppAdLoadCB: AppOpenAdLoadCallback

        private fun logEvent(eventName: String, activity: Activity) {
            activity.applicationContext?.let { SharedPrefConfig.LogCustomEvent(it, "openAppAd", "openAppAd", eventName) }
        }
    }
}
