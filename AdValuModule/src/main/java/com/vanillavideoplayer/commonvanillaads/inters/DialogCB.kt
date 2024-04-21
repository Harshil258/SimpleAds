package com.vanillavideoplayer.commonvanillaads.inters

import android.app.Dialog

interface DialogCB {
    fun onRightClick(dialog: Dialog)
    fun onWrongClick(dialog: Dialog)
}
