package com.googleadsdemo.SimpleAds.Managers

import android.app.Activity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.googleadsdemo.SimpleAds.Application.SimpleApplicationClass
import com.googleadsdemo.SimpleAds.Interfaces.InterAdCB
import com.googleadsdemo.SimpleAds.Utils.Gi
import com.googleadsdemo.SimpleAds.Utils.SharedPrefConfig
import com.googleadsdemo.SimpleAds.Utils.sharedPrefConfig

class InterstitialAdManager {
    private var isAdLoading = false
    private var counter: Int = 3


    fun loadAndShowInterstitial(activity: Activity, callBack: InterAdCB) {
        try {
            if (!Gi().networkAvailableChheKeNai(activity.applicationContext)){
                callBack.onForwardFlow()
                return
            }

            SimpleApplicationClass.isAppInForeground = true

            if (shouldSkipInterstitial()) return

            if (interstitialAd != null) {
                handleExistingInterstitial(activity, callBack)
                return
            }else{
                callBack.onForwardFlow()
            }

            if (shouldSkipLoadingInterstitial(activity)) {
                callBack.onForwardFlow()
                return
            }

            loadInterstitial(activity)
            showLoadingDialog(activity, callBack)
            startTimerForContinueFlow(activity,  callBack)
        } catch (e: Exception) {
        }
    }

    private fun shouldSkipInterstitial(): Boolean {
        return Gi().checkForMultipleClick(750) || OpenAppAdsManager.boolAdShowing || isAdLoading
    }

    private fun handleExistingInterstitial(activity: Activity, callBack: InterAdCB) {
        if (!OpenAppAdsManager.boolAdShowing && isCounterSatisfy()) {
            startTimerForContinueFlow(activity, callBack)
        } else {
            callBack.onForwardFlow()
        }
    }

    private fun shouldSkipLoadingInterstitial(activity: Activity): Boolean {
        return isInterStatusDisabled() || !Gi().networkAvailableChheKeNai(activity.applicationContext)
    }

    fun loadInterstitial(activity: Activity) {
        val adRequest = AdRequest.Builder().build()
        val AD_UNIT = activity.sharedPrefConfig.appDetails.admobInter
        isAdLoading = true

        InterstitialAd.load(activity, AD_UNIT, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(interstitialAd12: InterstitialAd) {
                isAdLoading = false
                interstitialAd = interstitialAd12
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                isAdLoading = false
                interstitialAd = null
            }
        })
    }

    private fun isInterStatusDisabled(): Boolean {
        val appDetailModel = SimpleApplicationClass.liveActivity.sharedPrefConfig.appDetails
        return (appDetailModel.adStatus ?: "OFF") == "OFF" || (appDetailModel.googleAdStatus ?: "OFF") == "OFF"
    }

    private fun isCounterSatisfy(): Boolean {
        val appDetailModel =  SimpleApplicationClass.liveActivity.sharedPrefConfig.appDetails
        val counter = appDetailModel.interstitialAdCounter?.toIntOrNull() ?: 3

        if (clickCounts >= counter) {
            return true
        } else {
            clickCounts++
            return false
        }
    }

    private fun startTimerForContinueFlow(activity: Activity,callBack: InterAdCB) {
        if (SimpleApplicationClass.isAppInForeground && interstitialAd != null && !isAdLoading) {
            if (!OpenAppAdsManager.boolAdShowing) {
                showInterstitial(activity, callBack)
            }
            stopLoadingDialog()
        }
    }

    private fun showInterstitial(activity: Activity?, callBack: InterAdCB) {
        if (isAdLoading || OpenAppAdsManager.boolAdShowing || boolAdShowing || interstitialAd == null) {
            stopLoadingDialog()
            callBack.onForwardFlow()
            return
        }

        interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                boolAdShowing = false
                interstitialAd = null
                callBack.onForwardFlow()
                stopLoadingDialog()
                activity?.let { loadInterstitial(it) }
            }

            override fun onAdImpression() {
                super.onAdImpression()
                activity?.applicationContext?.let { SharedPrefConfig.LogCustomEvent(it, "interstitialAd", "interstitialAd", "onAdImpression") }
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                boolAdShowing = false
                interstitialAd = null
                callBack.onForwardFlow()
            }

            override fun onAdClicked() {
                super.onAdClicked()
                activity?.applicationContext?.let { SharedPrefConfig.LogCustomEvent(it, "interstitialAd", "interstitialAd", "onAdClicked") }
            }

            override fun onAdShowedFullScreenContent() {
                boolAdShowing = true
                stopLoadingDialog()
                interstitialAd = null
                if (clickCounts >= counter) {
                    clickCounts = 1
                }
            }
        }

        interstitialAd?.show(activity!!)
    }

    fun stopLoadingDialog() {
        // Implement if needed
    }

    private fun showLoadingDialog(activity: Activity, callBack: InterAdCB) {
    }

    companion object {
        var interstitialAd: InterstitialAd? = null

        var boolAdShowing: Boolean = false
        var clickCounts = 0
    }
}
