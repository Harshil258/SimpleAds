package com.googleadsdemo.SimpleAds.model

import com.google.gson.annotations.SerializedName


data class AppRespoModel(
    @JvmField
    @SerializedName("appdetail")
    val appdetail: AppDetailModel,
    @JvmField
    @SerializedName("adsdetail")
    val adsdetail: AdDetailModel,
)

