package com.vanillavideoplayer.commonvanillaads.inters

import android.app.Activity

interface SimpleCallBack {

    fun onForwardAppFlow(operatingActivity: Activity)
    fun isLauncherOperating(activity: Activity, doOtherStuff: Boolean = false)
    fun hideLoadingAnimation()
}