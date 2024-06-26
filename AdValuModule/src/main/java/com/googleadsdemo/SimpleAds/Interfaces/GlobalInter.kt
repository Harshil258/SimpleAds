package com.googleadsdemo.SimpleAds.Interfaces

interface GlobalInter {

    fun onGlobalCreate()
    fun onGlobalTerminate()
    fun onGlobalActPreCreated()
    fun onGlobalActCreated()
    fun onGlobalActStarted()
    fun onGlobalActResumed()
    fun onGlobalActPaused()
    fun onGlobalActStopped()
    fun onGlobalActSaveInstanceState()
    fun onGlobalActDestroyed()
    fun setOpenAppClass()
}