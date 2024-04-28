package com.googleadsdemo.SimpleAds.Utils

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.googleadsdemo.SimpleAds.model.AdDetailModel
import com.googleadsdemo.SimpleAds.model.AppDetailModel


val Context.sharedPrefConfig: SharedPrefConfig
    get() = SharedPrefConfig.newInstance(applicationContext)


fun Context.getSharedPrefs(): SharedPreferences =
    getSharedPreferences("GlobalAdPrefs", Context.MODE_PRIVATE)


class SharedPrefConfig(context: Context) {

    protected val prefs = context.getSharedPrefs()


    private val APP_DETAILS = "app_details"
    private val AD_DETAILS = "ad_details"
    private val ALL_DATA = "all_data"
    var HOW_TO_USE_DONE: String = "how_to_use_done"

    var langCode: String
        get() = prefs.getString("vanilla_app_lang_pref", "") ?: ""
        set(value) = prefs.edit().putString("vanilla_app_lang_pref", value).apply()

    var isAppPurchased: Boolean
        get() = prefs.getBoolean("IsAppPurchased", false)
        set(value) = prefs.edit().putBoolean("IsAppPurchased", value).apply()


    fun getStringPref(key: String?, defaultValue: Boolean?): Boolean? {
        return prefs.getBoolean(key, false)
    }

    fun setStringPref(key: String?, value: String?) {
        prefs.edit().putString(key, value).apply()
    }

    var appDetails: AppDetailModel
        get() {
            return try {
                Gson().fromJson(prefs.getString(APP_DETAILS, null), AppDetailModel::class.java)
            } catch (e: Exception) {
                AppDetailModel()
            }
        }
        set(value) {
            prefs.edit().putString(APP_DETAILS, Gson().toJson(value)).apply()
        }


    var adDetails: AdDetailModel?
        get() {
            return try {
                Gson().fromJson(
                    prefs.getString(AD_DETAILS, null), object : TypeToken<AdDetailModel?>() {}.type
                )
            } catch (e: Exception) {
                null
            }
        }
        set(value) = prefs.edit().putString(AD_DETAILS, Gson().toJson(value)).apply()


    companion object {
        fun newInstance(context: Context) = SharedPrefConfig(context)

        fun LogCustomEvent(
            context: Context, eventName: String, parameterName: String, parameterValue: String
        ) {
            val firebaseAnalytics = FirebaseAnalytics.getInstance(context)
            val params = Bundle().apply {
                putString(parameterName, parameterValue)
            }
            firebaseAnalytics.logEvent("$eventName$parameterName$parameterValue", params)
        }
    }
}

