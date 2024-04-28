package com.googleadsdemo.SimpleAds.model

import com.google.gson.annotations.SerializedName

data class DataResponse(
    @SerializedName("AppData") val appData: AppRespoModel
)