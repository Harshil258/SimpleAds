package com.mahabharata.simpleads


import android.app.Activity
import android.app.Application
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.LifecycleObserver
import com.vanillavideoplayer.commonvanillaads.app.SimpleApplicationClass
import com.vanillavideoplayer.commonvanillaads.app.SimpleApplicationClass.Companion.liveActivity
import com.vanillavideoplayer.commonvanillaads.inters.SimpleCallBack
import com.vanillavideoplayer.commonvanillaads.inters.DialogCB
import com.vanillavideoplayer.commonvanillaads.z.SharedPrefConfig
import com.vanillavideoplayer.commonvanillaads.z.sharedPrefConfig


class ApplicationClass : Application(), Application.ActivityLifecycleCallbacks, LifecycleObserver {

    lateinit var simpleApplicationClass: SimpleApplicationClass
    override fun onCreate() {
        super.onCreate()

        registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {

        Log.e("dfrhsdfhsfdh", "onActivityCreated: 2")

//        try {
            if (activity is LauncherActivity) {
                Log.e("dfrhsdfhsfdh", "startNextFlowLogic: 2")
                simpleApplicationClass =
                    SimpleApplicationClass(this@ApplicationClass, object : SimpleCallBack {

                        override fun hideLoadingAnimation() {
                            try {
                                LauncherActivity.launcherInstance.runOnUiThread {}
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }

                        override fun isLauncherOperating(
                            activity: Activity,
                            doOtherStuff: Boolean
                        ) {
                            Log.e("dfrhsdfhsfdh", "startNextFlowLogic: 2  1")

                            if (doOtherStuff && activity is LauncherActivity) {
                                Log.e("dfrhsdfhsfdh", "startNextFlowLogic: 2  2")
                                SimpleApplicationClass.boolLauncherRunning = true
                                simpleApplicationClass.fetchApiResponse()
                                simpleApplicationClass.startTimerForForwardFlow(4000)
                            } else if (activity is LauncherActivity) {
                                SimpleApplicationClass.boolLauncherRunning = true
                            }
                        }

                        override fun onForwardAppFlow(operatingActivity: Activity) {
                            Log.d("dfrhsdfhsfdh", "onForwardAppFlow")

                            startNextAppFlow(operatingActivity)
                        }
                    })
                Log.e("dfrhsdfhsfdh", "startNextFlowLogic: 2  6")
                simpleApplicationClass.onGlobalCreate()
                Log.e("dfrhsdfhsfdh", "startNextFlowLogic: 2  7")

            }
            liveActivity = activity
            SimpleApplicationClass.simpleCallBack.isLauncherOperating(activity, true)
//        } catch (e: Exception) {
//        }
    }

    override fun onActivityStarted(activity: Activity) {}

    override fun onActivityResumed(activity: Activity) {}

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {}

    private fun startNextAppFlow(runningActivity: Activity) {
        Log.e("dfrhsdfhsfdh", "startNextFlowLogic: 1")

        try {
            val pInfo =
                runningActivity.packageManager.getPackageInfo(runningActivity.packageName, 0)
            val version = pInfo.versionName

            val appDetailModel = sharedPrefConfig.appDetails
            val updateNeededVersions = appDetailModel.updateNeededVersions
            if (updateNeededVersions.size > 0 && updateNeededVersions.contains(version)) {
                showUpdateDialog(runningActivity, true)
                return
            }

            val forceNeededVersions = appDetailModel.forceNeededVersions
            if (forceNeededVersions.size > 0 && forceNeededVersions.contains(version)) {
                showUpdateDialog(runningActivity, false)
                return
            }
        } catch (e: PackageManager.NameNotFoundException) {
        }

        startNextFlowLogic(runningActivity)
    }

    private fun showUpdateDialog(runningActivity: Activity, isUpdate: Boolean) {
        val dialogTitle = "Upgrade Alert!"
        val dialogMessage = """
            Install the latest version for exciting improvements.! 🌟
            We've implemented crucial bug fixes and introduced exciting new features to elevate your experience with this ${
            resources.getString(
                R.string.app_name
            )
        }. Update now to enjoy the enhanced version!
            Tap 'Update' now to access the latest version. Thank you for choosing ${
            resources.getString(
                R.string.app_name
            )
        }. 📱✨
        """.trimIndent()

        val dialogCB = object : DialogCB {
            override fun onRightClick(dialog: Dialog) {
                val appPackageName = packageName
                try {
                    runningActivity.startActivity(
                        Intent(
                            Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")
                        )
                    )
                } catch (anfe: ActivityNotFoundException) {
                    runningActivity.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                        )
                    )
                }
            }

            override fun onWrongClick(dialog: Dialog) {
                startNextFlowLogic(runningActivity)
            }
        }

        simpleApplicationClass!!.showUpdateVanillaDialog(
            runningActivity,
            dialogTitle,
            dialogMessage,
            "Update",
            if (isUpdate) "cancel" else "",
            isUpdate,
            dialogCB
        )
    }

    private fun startNextFlowLogic(runningActivity: Activity) {
        try {
            if (sharedPrefConfig.appDetails.visibleAdvertiseDialog == "1") {
                object : CountDownTimer(
                    sharedPrefConfig.appDetails.appDialogTime.toInt() * 1000L,
                    2000L
                ) {
                    override fun onTick(millisUntilFinished: Long) {}

                    override fun onFinish() {
                        simpleApplicationClass!!.showAdAppDialog(
                            runningActivity,
                            sharedPrefConfig.appDetails.appDialogTitle,
                            sharedPrefConfig.appDetails.appDialogDesc,
                            "Visit",
                            "Dismiss",
                            sharedPrefConfig.appDetails.appDialogImage,
                            true,
                            object : DialogCB {
                                override fun onRightClick(dialog: Dialog) {
                                    runningActivity.startActivity(
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse(sharedPrefConfig.appDetails.dialogPositiveLink)
                                        )
                                    )
                                    dialog.cancel()
                                    dialog.dismiss()
                                }

                                override fun onWrongClick(dialog: Dialog) {
                                    dialog.cancel()
                                    dialog.dismiss()
                                }
                            })
                    }
                }.start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        Log.d("dfrhsdfhsfdh", "startNextFlowLogic: ")



        val intent = Intent(runningActivity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        runningActivity.finish()
        SimpleApplicationClass.boolLauncherRunning = false
    }
}

