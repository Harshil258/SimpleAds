package com.googleadsdemo.SimpleAds.Interfaces

import android.app.Dialog

interface DialogCB {
    fun onRightClick(dialog: Dialog)
    fun onWrongClick(dialog: Dialog)
}
