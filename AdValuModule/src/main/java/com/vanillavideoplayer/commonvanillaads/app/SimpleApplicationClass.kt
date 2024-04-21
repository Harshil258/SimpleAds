package com.vanillavideoplayer.commonvanillaads.app

import android.app.Activity
import android.app.Application
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.cardview.widget.CardView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.tasks.Task
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.onesignal.OneSignal
import com.vanillavideoplayer.commonvanillaads.inters.DialogCB
import com.vanillavideoplayer.commonvanillaads.inters.GlobalInter
import com.vanillavideoplayer.commonvanillaads.inters.SimpleCallBack
import com.vanillavideoplayer.commonvanillaads.model.AppRespoModel
import com.vanillavideoplayer.commonvanillaads.vanillaads.InterstitialAdManager
import com.vanillavideoplayer.commonvanillaads.vanillaads.NativeAdManager
import com.vanillavideoplayer.commonvanillaads.vanillaads.OpenAppAdsManager
import com.vanillavideoplayer.commonvanillaads.z.HouseBtnReceiver
import com.vanillavideoplayer.commonvanillaads.z.sharedPrefConfig
import com.vanillavideoplayer.videoplayer.R

class SimpleApplicationClass(
    private val app: Application, private val simpleCallBack1: SimpleCallBack
) : LifecycleObserver, Application.ActivityLifecycleCallbacks, GlobalInter {

    private var openAppAdsManager: OpenAppAdsManager
    private var googleInterstitialAdClsAdClass: InterstitialAdManager
    private var nativeAdManager: NativeAdManager
    var advertiseAppDialog: Dialog? = null
    var firebaseRemoteConfig: FirebaseRemoteConfig
    val appContext: Context
        get() = app.applicationContext

    var homeButtonReceiver: HouseBtnReceiver? = null
    var updateVanillaDialog: Dialog? = null

    init {
        simpleCallBack = simpleCallBack1
        openAppAdsManager = OpenAppAdsManager()
        googleInterstitialAdClsAdClass = InterstitialAdManager()
        nativeAdManager = NativeAdManager()

        FirebaseApp.initializeApp(appContext)
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)
        OneSignal.initWithContext(appContext)
        OneSignal.setAppId(appContext.sharedPrefConfig.appDetails.oneSignalId)
        OneSignal.promptForPushNotifications()

        if (!appContext.sharedPrefConfig.isAppPurchased) {
            MobileAds.initialize(appContext) {}
        }


        registerReceiver()
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().detectAll().build())

        app.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().detectAll().build())
        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()

    }


    var vanillaHandler: Handler = Handler(Looper.getMainLooper())
    var vanillaRunnable: Runnable = Runnable {
        try {
            vanillaHandler.removeCallbacksAndMessages(null)
        } catch (e: Exception) {
            e.printStackTrace()
        }


        if (!OpenAppAdsManager.boolAdShowing && boolLauncherRunning) {
            simpleCallBack.onForwardAppFlow(liveActivity)
        }
    }


    var vanillaHandler2: Handler = Handler(Looper.getMainLooper())
    var vanillaRunnble2: Runnable = Runnable {
        val sharedPrefConfig = appContext.sharedPrefConfig


        try {
            vanillaHandler2.removeCallbacksAndMessages(null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (!OpenAppAdsManager.boolAdShowing) {
            if (sharedPrefConfig.appDetails.adStatus != "ON") {
                try {
                    vanillaHandler.removeCallbacksAndMessages(null)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                SimpleApplicationClass.simpleCallBack.onForwardAppFlow(SimpleApplicationClass.liveActivity)
            }
        }
    }


    private fun registerReceiver() {
        homeButtonReceiver = HouseBtnReceiver()
        val filter = IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            app.registerReceiver(homeButtonReceiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            app.registerReceiver(homeButtonReceiver, filter)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        if (!boolLauncherRunning && (openAppAdsManager.isAdAvailable() && !OpenAppAdsManager.boolAdShowing)) {
            showOpenApp()
        }
    }


    private fun showOpenApp() {
        if (InterstitialAdManager.boolAdShowing) return
        if (OpenAppAdsManager.shouldStopOpenApp) {
            OpenAppAdsManager.shouldStopOpenApp = false
            return
        }
        if (simpleApplicationClassInstance.openAppAdsManager.isValidOpenAppStatus()) {
            simpleApplicationClassInstance.openAppAdsManager.showOpenAppAd(liveActivity) {
                if (boolLauncherRunning) {
                    boolLauncherRunning = false
                    Handler(Looper.getMainLooper()).postDelayed({
                        simpleCallBack.onForwardAppFlow(liveActivity)
                    }, 750)
                }
            }
        }
    }

    fun showUpdateVanillaDialog(
        context: Activity,
        header: String?,
        desc: String?,
        positive: String?,
        negative: String,
        boolCancelable: Boolean,
        dialogCB: DialogCB
    ) {
        try {
            if (updateVanillaDialog != null && updateVanillaDialog!!.isShowing) {
                return
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        updateVanillaDialog = Dialog(context)
        updateVanillaDialog!!.setContentView(R.layout.layout_dialog_update)
        updateVanillaDialog!!.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        updateVanillaDialog!!.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        updateVanillaDialog!!.setCancelable(boolCancelable)
        val txtTitle = updateVanillaDialog!!.findViewById<MaterialTextView>(R.id.dialogTitle)
        val txtDesc = updateVanillaDialog!!.findViewById<MaterialTextView>(R.id.dialogTextDesc)
        val ok_btn = updateVanillaDialog!!.findViewById<RelativeLayout>(R.id.dialogOk)
        val ok_btn_text = updateVanillaDialog!!.findViewById<MaterialTextView>(R.id.dialogOkText)
        val dialogCancel = updateVanillaDialog!!.findViewById<RelativeLayout>(R.id.dialogCancel)
        val dialogCancelText =
            updateVanillaDialog!!.findViewById<MaterialTextView>(R.id.dialogCancelText)


        if (negative.isEmpty()) {
            dialogCancel.visibility = View.GONE
        } else {
            dialogCancel.visibility = View.VISIBLE
        }

        ok_btn_text.text = positive
        txtDesc.text = desc
        txtTitle.text = header
        ok_btn.setOnClickListener { view: View? ->
            dialogCB.onRightClick(updateVanillaDialog!!)
        }
        dialogCancel.setOnClickListener { dialogCB.onWrongClick(updateVanillaDialog!!) }
        try {
            if (!context.isFinishing) {
                updateVanillaDialog!!.show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showAdAppDialog(
        context: Activity?,
        titleText: String?,
        descriptionText: String?,
        positiveBtnText: String?,
        negativeBtnText: String?,
        imageLink: String?,
        boolCancelable: Boolean,
        dialogCB: DialogCB
    ) {
        var context = context
        context = liveActivity

        val finalActivity = context
        context!!.runOnUiThread {
            advertiseAppDialog = Dialog(finalActivity!!)
            advertiseAppDialog!!.setContentView(R.layout.layout_dialog_advertisement)

            val window = advertiseAppDialog!!.window
            if (window != null) {
                window.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                )
                window.setBackgroundDrawableResource(android.R.color.transparent)
            }

            advertiseAppDialog!!.setCancelable(boolCancelable)

            val dialogTitle = advertiseAppDialog!!.findViewById<MaterialTextView>(R.id.dialogTitle)
            val dialogTextDesc =
                advertiseAppDialog!!.findViewById<MaterialTextView>(R.id.dialogTextDesc)
            val dialogOk = advertiseAppDialog!!.findViewById<RelativeLayout>(R.id.dialogOk)
            val dialogCancel = advertiseAppDialog!!.findViewById<RelativeLayout>(R.id.dialogCancel)
            val dialogOkText =
                advertiseAppDialog!!.findViewById<MaterialTextView>(R.id.dialogOkText)
            val dialogCancelText =
                advertiseAppDialog!!.findViewById<MaterialTextView>(R.id.dialogCancelText)
            val dialogImageCard = advertiseAppDialog!!.findViewById<CardView>(R.id.dialogImageCard)
            val dialogMainCard = advertiseAppDialog!!.findViewById<ImageView>(R.id.dialogMainCard)

            if (!imageLink.isNullOrEmpty()) {
                dialogImageCard.visibility = View.VISIBLE
                try {
                    Glide.with(finalActivity).load(imageLink)
                        .transition(DrawableTransitionOptions.withCrossFade()).into(dialogMainCard)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                dialogImageCard.visibility = View.GONE
            }

            dialogOkText.text = positiveBtnText
            dialogCancelText.text = negativeBtnText
            dialogTextDesc.text = descriptionText
            dialogTitle.text = titleText

            dialogOk.setOnClickListener { view: View? ->
                dialogCB.onRightClick(advertiseAppDialog!!)
            }

            dialogCancel.setOnClickListener { view: View? ->
                dialogCB.onWrongClick(advertiseAppDialog!!)
            }

            advertiseAppDialog!!.show()
            try {
                if (!finalActivity.isFinishing) {
                    advertiseAppDialog!!.show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun fetchApiResponse() {

        firebaseRemoteConfig.fetchAndActivate().addOnCompleteListener { task: Task<Boolean?> ->
            if (task.isSuccessful) {
                val respString = firebaseRemoteConfig.getString("AppData")
                try {
                    val respo = Gson().fromJson(respString, AppRespoModel::class.java)
                    if (respo != null) {
                        handleApiRespo(respo)
                    }
                } catch (e: JsonSyntaxException) {
                    e.printStackTrace()
                }
            } else {
                val responseString = firebaseRemoteConfig.getString("AppData")
                try {
                    val resp = Gson().fromJson(responseString, AppRespoModel::class.java)
                    if (resp != null) {
                        handleApiRespo(resp)
                    }
                } catch (e: JsonSyntaxException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun handleApiRespo(respo: AppRespoModel?) {
        if (respo?.appdetail?.pkgName != null && respo.appdetail.pkgName != "") {

            var sharedPrefConfig = appContext.sharedPrefConfig
            if (sharedPrefConfig.isAppPurchased) {
                respo.appdetail.adStatus = "OFF"
            }

//            respo.appdetail.admobAppBanner = "ca-app-pub-3940256099942544/6300978111"
//            respo.appdetail.admobAppOpen = "ca-app-pub-3940256099942544/9257395921"
//            respo.appdetail.admobInter = "ca-app-pub-3940256099942544/1033173712"
//            respo.appdetail.admobNative = "ca-app-pub-3940256099942544/2247696110"

            respo.appdetail.interstitialAdCounter =
                firebaseRemoteConfig.getString("InterstitialGap")
            respo.appdetail.Nativegap = firebaseRemoteConfig.getString("NativeGap")
            respo.appdetail.useCatchedNative = firebaseRemoteConfig.getString("useCatchedNative")
            respo.appdetail.nativeAdSize = firebaseRemoteConfig.getString("NativeSize")

//            respo.appdetail.adStatus = "ON"
//            respo.appdetail.nativeAdSize = "LARGE"
//            respo.appdetail.nativeAdSize = "MEDIUM"


            sharedPrefConfig.appDetails = respo.appdetail
            sharedPrefConfig.adDetails = respo.adsdetail

            FirebaseApp.initializeApp(appContext)
            OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)
            OneSignal.initWithContext(appContext)
            OneSignal.setAppId(sharedPrefConfig.appDetails.oneSignalId)
            OneSignal.promptForPushNotifications()

            startTimerForAdStatus()
            validateAndShowAdIfAvailable()
        } else {
            startTimerForAdStatus()
            validateAndShowAdIfAvailable()
        }
    }

    private fun validateAndShowAdIfAvailable() {

        var sharedPrefConfig = appContext.sharedPrefConfig
        updateClickCounts()

        val appDetails = sharedPrefConfig.appDetails
        if (appDetails.adStatus == "ON") {
            showOpenApp()

            googleInterstitialAdClsAdClass.loadInterstitial(liveActivity)
            nativeAdManager.prepareNativeAd(liveActivity)


        } else {
            if (!OpenAppAdsManager.boolAdShowing) {
                vanillaHandler2.removeCallbacksAndMessages(null)
                vanillaHandler2.removeCallbacks(vanillaRunnble2)

                vanillaHandler.removeCallbacksAndMessages(null)
                vanillaHandler.removeCallbacks(vanillaRunnable)

                simpleCallBack.onForwardAppFlow(liveActivity)
            }
        }
    }

    fun updateClickCounts() {
        val appDetailsModel = appContext.sharedPrefConfig.appDetails
        if (appDetailsModel.interstitialAdCounter.isNotEmpty() && appDetailsModel.interstitialAdCounter != "" && TextUtils.isDigitsOnly(
                appDetailsModel.interstitialAdCounter
            )
        ) {
            try {

                InterstitialAdManager.clickCounts = appDetailsModel.interstitialAdCounter.toInt()
            } catch (e: Exception) {
                InterstitialAdManager.clickCounts = 3
                e.printStackTrace()
            }
        } else {
            InterstitialAdManager.clickCounts = 3
        }
    }

    private fun startTimerForAdStatus() {
        try {
            vanillaHandler2.removeCallbacksAndMessages(null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        vanillaHandler2.postDelayed(vanillaRunnble2, 2000)
    }

    override fun onActivityPreCreated(activity: Activity, savedInstanceState: Bundle?) {
        super<Application.ActivityLifecycleCallbacks>.onActivityPreCreated(
            activity, savedInstanceState
        )
        Log.e("dfrhsdfhsfdh", "onActivityPreCreated: 1111")

        liveActivity = activity
        simpleCallBack.isLauncherOperating(activity, false)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        liveActivity = activity
        Log.e("dfrhsdfhsfdh", "onActivityPreCreated: 11112")

        simpleCallBack.isLauncherOperating(activity, true)
    }

    override fun onActivityStarted(activity: Activity) {
        liveActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        liveActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {
        isAppInForeground = false
        googleInterstitialAdClsAdClass.stopLoadingDialog()
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
    }


    companion object {
        lateinit var simpleApplicationClassInstance: SimpleApplicationClass
        lateinit var liveActivity: Activity
        var isAppInForeground: Boolean = false
        var boolLauncherRunning: Boolean = false
        lateinit var simpleCallBack: SimpleCallBack
    }


    override fun onGlobalCreate() {
        Log.e("dfrhsdfhsfdh", "startNextFlowLogic: 4")

        simpleApplicationClassInstance = this

        val firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()

        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(1) // Set how often to fetch the remote config (1 hour in this example)
            .build()
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings)
        firebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)


        MobileAds.initialize(app) { initializationStatus ->
            val statusMap = initializationStatus.adapterStatusMap
            for (adapterClass in statusMap.keys) {
                val status = statusMap[adapterClass]
            }

        }

        registerReceiver()
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().detectAll().build())

        app.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().detectAll().build())
        simpleApplicationClassInstance.initVanillaOpenClass()
        googleInterstitialAdClsAdClass = InterstitialAdManager()
        nativeAdManager = NativeAdManager()

        Log.e("dfrhsdfhsfdh", "startNextFlowLogic: 5")

    }

    fun initVanillaOpenClass() {
        openAppAdsManager = OpenAppAdsManager()
    }

    fun returnGoogleInterstitialCls(): InterstitialAdManager {
        return googleInterstitialAdClsAdClass
    }

    fun returnNativeAdCls(): NativeAdManager {
        return nativeAdManager
    }

    fun startTimerForForwardFlow(duration: Int) {
        try {
            vanillaHandler.removeCallbacksAndMessages(null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        vanillaHandler.postDelayed(vanillaRunnable, duration.toLong())
    }

    override fun onGlobalTerminate() {
        app.unregisterReceiver(homeButtonReceiver)
    }


    override fun onGlobalActPreCreated() {
    }

    override fun onGlobalActCreated() {
    }

    override fun onGlobalActStarted() {
    }

    override fun onGlobalActResumed() {
    }

    override fun onGlobalActPaused() {
    }

    override fun onGlobalActStopped() {
    }

    override fun onGlobalActSaveInstanceState() {
    }

    override fun onGlobalActDestroyed() {
    }

    override fun setOpenAppClass() {
    }
}
