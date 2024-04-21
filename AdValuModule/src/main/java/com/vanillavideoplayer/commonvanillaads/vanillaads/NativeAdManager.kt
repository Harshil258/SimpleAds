package com.vanillavideoplayer.commonvanillaads.vanillaads

import android.app.Activity
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Handler
import android.view.View
import android.view.ViewGroup.OnHierarchyChangeListener
import android.view.ViewGroup.TEXT_ALIGNMENT_CENTER
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import com.vanillavideoplayer.commonvanillaads.Enums.NativeAdSize
import com.vanillavideoplayer.commonvanillaads.app.SimpleApplicationClass
import com.vanillavideoplayer.commonvanillaads.inters.AdCallbacks
import com.vanillavideoplayer.commonvanillaads.model.AppDetailModel
import com.vanillavideoplayer.commonvanillaads.z.Gi
import com.vanillavideoplayer.commonvanillaads.z.SharedPrefConfig
import com.vanillavideoplayer.commonvanillaads.z.sharedPrefConfig
import com.vanillavideoplayer.videoplayer.R
import java.util.Random


interface AdLoadCallback {
    fun onAdsLoaded(isLoaded: Boolean)
}

class NativeAdManager {

    private var adCallbacksListener: AdCallbacks? = null
    private val BULK_AD_LOAD_COUNT = 3

    private var TAG = "NativeAdManager"

    fun loadBulkNativeAds(activity: Activity, adLoadCallback: AdLoadCallback? = null) {

        if (shouldReturnIfNativeAdStatusNull(activity)) {
            return
        }
        if (loadedAds.size >= BULK_AD_LOAD_COUNT) {
            adLoadCallback?.onAdsLoaded(true)
            return
        }

        if (!Gi().networkAvailableChheKeNai(activity.applicationContext)) {
            return
        }



        boolAdLoading = true
        val adLoader = AdLoader.Builder(activity, activity.sharedPrefConfig.appDetails.admobNative).forNativeAd { nativeAd ->
            boolAdLoading = false
            loadedAds.add(nativeAd)
            adLoadCallback?.onAdsLoaded(true)
        }.withAdListener(object : com.google.android.gms.ads.AdListener() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                boolAdLoading = false
            }

            override fun onAdClicked() {
                activity?.applicationContext?.let {
                    SharedPrefConfig.LogCustomEvent(it, "NativeAd", "NativeAd", "onAdClicked")
                }
            }

            override fun onAdImpression() {
                activity?.applicationContext?.let {
                    SharedPrefConfig.LogCustomEvent(it, "NativeAd", "NativeAd", "onAdImpression")
                }
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
            }
        }).withNativeAdOptions(NativeAdOptions.Builder().build()).build()

        // Load multiple ads at once
        repeat(BULK_AD_LOAD_COUNT) {
            adLoader.loadAd(AdRequest.Builder().build())
        }
    }

    fun getNextNativeAd(callback: (NativeAd?) -> Unit) {
        var gaveResponse = false
        if (loadedAds.isEmpty()) {
            val handler = Handler()
            var adsLoaded = false // Flag to track whether ads are loaded

            loadBulkNativeAds(SimpleApplicationClass.liveActivity, object : AdLoadCallback {
                override fun onAdsLoaded(isLoaded: Boolean) {
                    if (isLoaded && loadedAds.isNotEmpty()) {
                        if (!gaveResponse) {
                            gaveResponse = true
                            callback(loadedAds.removeAt(0))
                        }
                        adsLoaded = true // Set the flag to true when ads are loaded
                    }
                }
            })

            // Wait for 2 seconds for ads to be loaded
            handler.postDelayed({
                if (!adsLoaded) {
                    if (!gaveResponse) {
                        gaveResponse = true
                        callback(null)
                    }
                }
            }, 2000)
        } else {
            if (!gaveResponse) {
                gaveResponse = true
                callback(loadedAds.removeAt(0))
            }
            if (loadedAds.isEmpty()) {
                loadBulkNativeAds(SimpleApplicationClass.liveActivity)
            }
        }
    }


    private fun populateNativeAdView(
        activity: Activity, adContainer: FrameLayout, nativeAd: NativeAd, isBigSize: Boolean, background: Int? = null, primaryColor: Int? = null
    ) {

        val bigLayouts = listOf(
            R.layout.layout_ad_native_big, R.layout.layout_ad_native_big_2, R.layout.layout_ad_native_big_3,
            R.layout.layout_ad_native_big, R.layout.layout_ad_native_big_2, R.layout.layout_ad_native_big_3,
            R.layout.layout_ad_native_big, R.layout.layout_ad_native_big_2, R.layout.layout_ad_native_big_3,
        )
        val smallLayouts = listOf(
            R.layout.native_ad_small, R.layout.native_ad_small_2, R.layout.native_ad_small_3,
            R.layout.native_ad_small, R.layout.native_ad_small_2, R.layout.native_ad_small_3,
            R.layout.native_ad_small, R.layout.native_ad_small_2, R.layout.native_ad_small_3,
        )

        val layoutId = if (isBigSize) {
            bigLayouts[Random().nextInt(bigLayouts.size)]
        } else {
            smallLayouts[Random().nextInt(smallLayouts.size)]
        }


        val adView = activity.layoutInflater.inflate(layoutId, null) as NativeAdView
        val isDarkMode = activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES


        try {
            adView.apply {

                // Notify the ad callbacks listener that the ad has been loaded
                adCallbacksListener?.onAdLoaded()

                // Request layout to ensure proper rendering of the ad view
                adView.rootView.requestLayout()
                rootView.requestLayout()

                // Find and set the headline view
                val headlineView = adView.findViewById<MaterialTextView>(R.id.txtHead)
                if (nativeAd.headline != null) {
                    headlineView.text = nativeAd.headline
                    adView.headlineView = headlineView
                }


                // Find and set the body view
                val body = adView.findViewById<MaterialTextView>(R.id.dialogTextDesc)
                if (nativeAd.body != null) {
                    body.text = nativeAd.body
                    adView.bodyView = body
                }

                // Find and set the icon view
                val image = adView.findViewById<ImageView>(R.id.icon)
                if (nativeAd.icon != null && nativeAd.icon!!.drawable != null) {
                    Glide.with(context).load(nativeAd.icon!!.drawable).circleCrop().into(image)
                    adView.iconView = image
                }

                // Find and set the call to action button
                val button = adView.findViewById<TextView>(R.id.btnClick)
                if (nativeAd.callToAction != null) {
                    if (layoutId != R.layout.layout_ad_native_big_3) {
                        button.textAlignment = TEXT_ALIGNMENT_CENTER
                    }
                    button.text = nativeAd.callToAction
                    adView.callToActionView = button
                }

                // Adjust view based on ad size
                if (isBigSize) {
                    val mediaView = adView.findViewById<MediaView>(R.id.mediaView)
                    if (nativeAd.mediaContent != null) {
                        mediaView.mediaContent = nativeAd.mediaContent
                        adView.mediaView = mediaView
                        mediaView.bringToFront()
                    }
                    if (layoutId == R.layout.layout_ad_native_big_3) {
                        mediaView.setOnHierarchyChangeListener(object : OnHierarchyChangeListener {
                            override fun onChildViewAdded(parent: View, child: View) {
                                val scale = context.resources.displayMetrics.density

                                val maxHeightPixels = 300
                                val maxHeightDp = (maxHeightPixels * scale + 0.5f).toInt()

                                if (child is ImageView) { //Images
                                    val imageView = child
                                    imageView.adjustViewBounds = true
                                    imageView.maxHeight = maxHeightDp
                                } else { //Videos
                                    val params = child.layoutParams
                                    params.height = maxHeightDp
                                    child.layoutParams = params
                                }
                            }

                            override fun onChildViewRemoved(parent: View, child: View) {}
                        })
                    }
                }


                // Apply text color and set values for big ad layout
                if (layoutId == R.layout.layout_ad_native_big_3) {
                    val starTextview = adView.findViewById<TextView>(R.id.ratingTextview)
                    val otherText = adView.findViewById<TextView>(R.id.advertiserTextview)
                    val sponsered = adView.findViewById<TextView>(R.id.sponsered)
                    val downloadImage = adView.findViewById<ImageView>(R.id.downloadImage)

                    if (nativeAd.starRating != null) {
                        starTextview.text = "${nativeAd.starRating.toString()} ★"
                        adView.starRatingView = starTextview
                    }

                    if (nativeAd.advertiser != null) {
                        otherText.text = nativeAd.advertiser
                        adView.advertiserView = otherText
                    }

                    downloadImage.setColorFilter(if (isDarkMode) Color.WHITE else Color.BLACK, PorterDuff.Mode.SRC_IN)

                    // Apply text color for big ad layout
                    if (isDarkMode) {
                        starTextview.setTextColor(Color.WHITE)
                        otherText.setTextColor(Color.WHITE)
                        sponsered.setTextColor(Color.WHITE)

                    } else {
                        starTextview.setTextColor(Color.BLACK)
                        otherText.setTextColor(Color.BLACK)
                        sponsered.setTextColor(Color.BLACK)

                    }
                }
                val btnClickCard = adView.findViewById<MaterialCardView>(R.id.btnClickCard)

                // Apply background and text color
                background?.let { adContainer.setBackgroundColor(it) }
                headlineView.setTextColor(if (isDarkMode) Color.WHITE else Color.BLACK)
                body.setTextColor(if (isDarkMode) Color.WHITE else Color.BLACK)
                button.setTextColor(if (isDarkMode) Color.WHITE else Color.BLACK)
                if (layoutId != R.layout.layout_ad_native_big_3) {
                    btnClickCard.setCardBackgroundColor(if (isDarkMode) Color.GRAY else Color.LTGRAY)
                }


                adView.setNativeAd(nativeAd)
            }
        } catch (e: Exception) {
            
        }



        adContainer.removeAllViews()
        adContainer.addView(adView)

        prepareNativeAd(activity)
    }

    private fun shouldReturnIfNativeAdStatusNull(context: Activity?): Boolean {
        val appDetailModel: AppDetailModel = SimpleApplicationClass.liveActivity.sharedPrefConfig.appDetails ?: return true
        return appDetailModel.adStatus.isBlank() || appDetailModel.adStatus.trim() != "ON" || appDetailModel.admobNative.isBlank()
    }

    fun getNativeAdSizeStatus(context: Activity?): String {
        val appDetailModel = SimpleApplicationClass.liveActivity.sharedPrefConfig.appDetails
        return if (shouldReturnIfNativeAdStatusNull(context)) "" else appDetailModel.nativeAdSize
    }

    fun visibleAdIfReady(
        nativeAdLayoutBig: RelativeLayout, adContainerBig: FrameLayout, nativeAdLayoutSmall: RelativeLayout, adContainerSmall: FrameLayout, isFromRecyclerView: Boolean? = false, background: Int? = null, primaryColor: Int? = null, adCallbacksListener: AdCallbacks?
    ) {
        this.adCallbacksListener = adCallbacksListener

        if (shouldReturnIfNativeAdStatusNull(SimpleApplicationClass.liveActivity)) {
            nativeAdLayoutBig.visibility = View.GONE
            nativeAdLayoutSmall.visibility = View.GONE
            return
        }

        val isBigSize = getNativeAdSizeStatus(SimpleApplicationClass.liveActivity) == NativeAdSize.LARGE.name
        if (isBigSize) {
            nativeAdLayoutBig.visibility = View.INVISIBLE
            nativeAdLayoutSmall.visibility = View.GONE
        } else {
            nativeAdLayoutBig.visibility = View.GONE
            nativeAdLayoutSmall.visibility = View.INVISIBLE
        }

        getNextNativeAd() { nativeAd ->
            if (nativeAd != null) {
                if (isBigSize) {
                    nativeAdLayoutBig.visibility = View.VISIBLE
                    nativeAdLayoutSmall.visibility = View.GONE
                    populateNativeAdView(SimpleApplicationClass.liveActivity, adContainerBig, nativeAd, isBigSize, background, primaryColor)
                } else {
                    nativeAdLayoutBig.visibility = View.GONE
                    nativeAdLayoutSmall.visibility = View.VISIBLE
                    populateNativeAdView(SimpleApplicationClass.liveActivity, adContainerSmall, nativeAd, isBigSize, background, primaryColor)
                }
            } else {
                nativeAdLayoutBig.visibility = View.GONE
                nativeAdLayoutSmall.visibility = View.GONE
                adCallbacksListener!!.onAdLoadFailed()
            }
        }

        if (!Gi().networkAvailableChheKeNai(SimpleApplicationClass.liveActivity)) {
            nativeAdLayoutBig.visibility = View.GONE
            nativeAdLayoutSmall.visibility = View.GONE
            return
        }
    }


    fun prepareNativeAd(activity: Activity) {

        if (shouldReturnIfNativeAdStatusNull(activity)) {
            return
        }
        if (loadedAds.isNotEmpty() || boolAdLoading) {
            return
        }

        if (!Gi().networkAvailableChheKeNai(activity.applicationContext)) {
            return
        }

        boolAdLoading = true
        val adLoader = AdLoader.Builder(activity, SimpleApplicationClass.liveActivity.sharedPrefConfig.appDetails.admobNative).forNativeAd { nativeAd ->
            boolAdLoading = false
            loadedAds.add(nativeAd)
        }.withAdListener(object : com.google.android.gms.ads.AdListener() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                boolAdLoading = false
            }

            override fun onAdClicked() {
                activity?.applicationContext?.let { SharedPrefConfig.LogCustomEvent(it, "NativeAd", "NativeAd", "onAdClicked") }
            }

            override fun onAdImpression() {
                activity?.applicationContext?.let { SharedPrefConfig.LogCustomEvent(it, "NativeAd", "NativeAd", "onAdImpression") }
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
            }
        }).withNativeAdOptions(NativeAdOptions.Builder().build()).build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    companion object {
        private var boolAdLoading = false
        private var loadedAds: MutableList<NativeAd> = mutableListOf()
    }
}
