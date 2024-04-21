package com.vanillavideoplayer.commonvanillaads.model

import com.google.gson.annotations.SerializedName

data class AdItemModel(
    @JvmField @SerializedName("Images")
    val adImages: String,
    @JvmField @SerializedName("Description")
    val adDescription: String,
    @JvmField @SerializedName("Title")
    val adTitle: String,
    @JvmField @SerializedName("OpenLink")
    val adOpenLink: String
)