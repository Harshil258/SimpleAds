package com.vanillavideoplayer.commonvanillaads.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class AppDetailModel : Serializable {
    var adAppName: String = ""
    var adStatus: String = "OFF"
    var googleAdStatus: String = "OFF"
    var Nativegap: String = "3"
    var admobAppBanner: String = ""
    var appscreennumber: String = ""
    var admobNative: String = ""
    var admobInter: String = ""
    var admobReward: String = ""
    var admobAppOpen: String = ""
    var useCatchedNative: String = "OFF"

    var maxBanner: String = ""
    var maxInterstitial: String = ""
    var maxNative: String = ""
    var maxAppOpen: String = ""

    var nativeAdSize: String = ""
    var interstitialAdCounter: String = ""
    var premiumVideo: String = ""

    var shouldShowNativeAdOnRecycler: String = ""
    var shouldShowGoogleAdsOnRecycler: String = ""

    var appPrivacy: String = "https://vanillavideoplayer.blogspot.com/2024/01/privacy-policy.html"
    var appContactUs: String = ""

    var oneSignalId: String = ""

    var visibleAdvertiseDialog: String = ""

    var appDialogTime: String = ""
    var appDialogTitle: String = ""
    var appDialogDesc: String = ""
    var appDialogImage: String = ""
    var dialogPositiveLink: String = ""
    var gameZopeStatus: String = ""
    var interInNextBtn: String = ""
    var customBannerOnClick: String = ""
    var updateNeededVersions: ArrayList<String> = ArrayList()
    var forceNeededVersions: ArrayList<String> = ArrayList()
    
    @SerializedName("package")
    var pkgName: String = ""
}
