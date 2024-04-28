package com.googleadsdemo.SimpleAds.SimpleAdClasses

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.browser.customtabs.CustomTabsIntent
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.ads.nativead.MediaView
import com.googleadsdemo.SimpleAds.Enums.AdStatus
import com.googleadsdemo.SimpleAds.Enums.NativeAdSize
import com.googleadsdemo.SimpleAds.Application.SimpleApplicationClass
import com.googleadsdemo.SimpleAds.Interfaces.AdCallbacks
import com.googleadsdemo.SimpleAds.model.AdItemModel
import com.googleadsdemo.SimpleAds.Utils.Gi
import com.googleadsdemo.SimpleAds.Utils.sharedPrefConfig
import com.vanillavideoplayer.videoplayer.R

class SimpleNative : RelativeLayout {
    lateinit var nativeGBigAd: RelativeLayout
    lateinit var nativeGBig: RelativeLayout
    lateinit var frameNativeBig: FrameLayout
    lateinit var nativeGSmall: RelativeLayout
    lateinit var nativeGSmallFrame: FrameLayout

    private var adCallbacks: AdCallbacks? = null
    private var isFromRecyclerview: Boolean = false
    private var isDarkModeProp: Boolean = false
    private var useDirectLoad: Boolean = false

    lateinit var inflater: LayoutInflater
    lateinit var view: View


    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    ) {

    }

    constructor(
        context: Context?,
        isFromRecyclerview: Boolean? = false,
        AdCallBack: AdCallbacks? = null,
        background: Int? = null,
        primaryColor: Int? = null
    ) : super(context) {
        useDirectLoad = true
        inflater = LayoutInflater.from(context)
        view = inflater.inflate(R.layout.general_native_layout_big, this, true)

        this.adCallbacks = AdCallBack
        nativeGBigAd = view.findViewById(R.id.rlAd)
        nativeGBig = view.findViewById(R.id.rlNativeBig)
        frameNativeBig = view.findViewById(R.id.frameNativeBig)
        nativeGSmall = view.findViewById(R.id.rlNativeSmall)
        nativeGSmallFrame = view.findViewById(R.id.frameNativeSmall)
        if (isFromRecyclerview != null) {
            this.isFromRecyclerview = isFromRecyclerview
        }

        if (useDirectLoad) {
            context?.let { initNativeGBig(it, background, primaryColor, false) }
        } else {
            hideNativeGBig()
        }

    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {

        inflater = LayoutInflater.from(context)
        view = inflater.inflate(R.layout.general_native_layout_big, this, true)


        nativeGBigAd = view.findViewById(R.id.rlAd)
        nativeGBig = view.findViewById(R.id.rlNativeBig)
        frameNativeBig = view.findViewById(R.id.frameNativeBig)
        nativeGSmall = view.findViewById(R.id.rlNativeSmall)
        nativeGSmallFrame = view.findViewById(R.id.frameNativeSmall)


        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.NativeGBig)
        isFromRecyclerview = typedArray.getBoolean(R.styleable.NativeGBig_isFromRecyclerView, false)
        useDirectLoad = typedArray.getBoolean(R.styleable.NativeGBig_useDirectNativeLoad, false)
        isDarkModeProp = typedArray.getBoolean(R.styleable.NativeGBig_isDarkMode, false)
        typedArray.recycle()

        if (useDirectLoad) {

            initNativeGBig(context, isDarkModeProp = isDarkModeProp)
        } else {

            hideNativeGBig()
        }
    }

    constructor(
        context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
    }


    fun initNativeGBig(
        context: Context,
        background: Int? = null,
        primaryColor: Int? = null,
        isDarkModeProp: Boolean? = false
    ) {
        try {

            val appDetailModel = context.sharedPrefConfig.appDetails

            if (inflater == null) {
                inflater = LayoutInflater.from(context)
                view = inflater.inflate(R.layout.general_native_layout_big, this, true)

                nativeGBigAd = view.findViewById(R.id.rlAd)
                nativeGBig = view.findViewById(R.id.rlNativeBig)
                frameNativeBig = view.findViewById(R.id.frameNativeBig)
                nativeGSmall = view.findViewById(R.id.rlNativeSmall)
                nativeGSmallFrame = view.findViewById(R.id.frameNativeSmall)
            }

            val isAdOn = appDetailModel?.adStatus == AdStatus.ON.name
            val isGoogleAdOn = appDetailModel?.googleAdStatus == AdStatus.ON.name
            val isGameZopeAdOn = appDetailModel?.gameZopeStatus == AdStatus.ON.name
            val shouldShowGoogleAdsOnRecycler =
                appDetailModel?.shouldShowGoogleAdsOnRecycler == AdStatus.ON.name
            val shouldShowNativeAdOnRecycler =
                appDetailModel?.shouldShowNativeAdOnRecycler == AdStatus.ON.name


            if (!isAdOn || !Gi().networkAvailableChheKeNai(context)) {
                view.visibility = View.GONE
                return
            }

            val isFromRecyclerView = isFromRecyclerview

            val showNativeAdCondition = if (isFromRecyclerView) {
                shouldShowNativeAdOnRecycler && shouldShowGoogleAdsOnRecycler
            } else {
                shouldShowNativeAdOnRecycler
            }

            if (isGoogleAdOn) {

                if (isFromRecyclerView && showNativeAdCondition) {

                    showNativeAd(background) {
                        if (it) {
                            rootView?.requestLayout()
                            adCallbacks?.onAdLoaded()
                        } else {
                            addCustomAdView(isGameZopeAdOn, background, isDarkModeProp)
                        }
                    }
                } else if (!isFromRecyclerView) {

                    showNativeAd(background, primaryColor) {

                        if (it) {
                            rootView?.requestLayout()
                            adCallbacks?.onAdLoaded()
                        } else {
                            addCustomAdView(isGameZopeAdOn, background, isDarkModeProp)
                        }
                    }
                } else {
                    if (shouldShowNativeAdOnRecycler) {
                        addCustomAdView(isGameZopeAdOn, background, isDarkModeProp)
                    } else {
                        hideNativeGBig()
                    }
                }
            } else {

                if (isFromRecyclerView && shouldShowNativeAdOnRecycler) {
                    addCustomAdView(isGameZopeAdOn)
                } else if (!isFromRecyclerView) {
                    addCustomAdView(isGameZopeAdOn)
                } else {
                    if (shouldShowNativeAdOnRecycler) {
                        addCustomAdView(isGameZopeAdOn)
                    } else {
                        hideNativeGBig()
                    }
                }
            }
        } catch (e: Exception) {

        }
    }


    private fun setupAdView(
        customAdView: View,
        adItemModel: AdItemModel,
        background: Int? = null,
        isDarkModeProp: Boolean? = false
    ) {
        try {
            var isDarkMode = isDarkModeProp
            val txtHead = customAdView.findViewById<TextView>(R.id.txtHead)
            val txtDescription = customAdView.findViewById<TextView>(R.id.dialogTextDesc)
            val mediaView = try {
                customAdView.findViewById<MediaView>(R.id.mediaView)
            } catch (e: Exception) {
            }
            val mediaViewImage = customAdView.findViewById<ImageView>(R.id.mediaViewImage)
            val btnClick = customAdView.findViewById<TextView>(R.id.btnClick)

//            mediaViewImage.bringToFront()

            if (isDarkMode == null) {
                isDarkMode =
                    customAdView.context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
            }


            // Set background color and text color based on mode
            if (isDarkMode) {
                background?.let { customAdView.setBackgroundColor(it) }
                txtHead.setTextColor(Color.WHITE)
                txtDescription.setTextColor(Color.WHITE)
                btnClick.setTextColor(Color.WHITE)
                btnClick.setBackgroundColor(Color.GRAY)
            } else {
                background?.let { customAdView.setBackgroundColor(it) }
                txtHead.setTextColor(Color.BLACK)
                txtDescription.setTextColor(Color.BLACK)
                btnClick.setTextColor(Color.BLACK)
                btnClick.setBackgroundColor(Color.LTGRAY)
            }

            // Set text and image content
            txtHead.text = adItemModel.adTitle
            txtDescription.text = adItemModel.adDescription

            Glide.with(SimpleApplicationClass.liveActivity).load(adItemModel.adImages)
                .apply(RequestOptions().transforms(CenterCrop(), RoundedCorners(5)))
                .transition(DrawableTransitionOptions.withCrossFade()).into(mediaViewImage)

            // Set click listener on the root view of customAdView
            customAdView.setOnClickListener {
                val customTabsIntent = CustomTabsIntent.Builder().build()
                customTabsIntent.launchUrl(
                    SimpleApplicationClass.liveActivity, Uri.parse(adItemModel.adOpenLink)
                )
            }
            txtHead.setOnClickListener {
                val customTabsIntent = CustomTabsIntent.Builder().build()
                customTabsIntent.launchUrl(
                    SimpleApplicationClass.liveActivity, Uri.parse(adItemModel.adOpenLink)
                )
            }
            txtDescription.setOnClickListener {
                val customTabsIntent = CustomTabsIntent.Builder().build()
                customTabsIntent.launchUrl(
                    SimpleApplicationClass.liveActivity, Uri.parse(adItemModel.adOpenLink)
                )
            }
            btnClick.setOnClickListener {
                val customTabsIntent = CustomTabsIntent.Builder().build()
                customTabsIntent.launchUrl(
                    SimpleApplicationClass.liveActivity, Uri.parse(adItemModel.adOpenLink)
                )
            }
            mediaViewImage.setOnClickListener {
                val customTabsIntent = CustomTabsIntent.Builder().build()
                customTabsIntent.launchUrl(
                    SimpleApplicationClass.liveActivity, Uri.parse(adItemModel.adOpenLink)
                )
            }
        } catch (e: Exception) {
            customAdView.visibility = View.GONE
        }
    }


    private fun addCustomAdView(
        isGameZopeAdOn: Boolean,
        background: Int? = null,
        isDarkModeProp: Boolean? = false
    ) {
        try {

            if (!isGameZopeAdOn) {
                nativeGBig.visibility = View.GONE
                nativeGSmall.visibility = View.GONE
                return
            }

            val appDetailModel = context.sharedPrefConfig.appDetails
            val adItemModel = if (appDetailModel?.nativeAdSize == NativeAdSize.LARGE.name) {
                context.sharedPrefConfig.adDetails?.forBiggerNative?.random()
            } else {
                context.sharedPrefConfig.adDetails?.forAverageNative?.random()
            }


            val customAdLayoutId = if (appDetailModel?.nativeAdSize == NativeAdSize.LARGE.name) {
                R.layout.layout_ad_native_big_2
            } else {
                R.layout.layout_custom_ad_native_small
            }


            val customAdView =
                SimpleApplicationClass.liveActivity.layoutInflater.inflate(customAdLayoutId, null)

            val layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
            customAdView.layoutParams = layoutParams

            setupAdView(customAdView, adItemModel!!, background, isDarkModeProp)

            if (appDetailModel.nativeAdSize == NativeAdSize.LARGE.name) {
                frameNativeBig.visibility = View.VISIBLE
                nativeGBig.addView(customAdView)
                nativeGBig.visibility = View.VISIBLE
                nativeGSmall.visibility = View.GONE
            } else {
                nativeGSmallFrame.visibility = View.VISIBLE
                nativeGSmall.addView(customAdView)
                nativeGSmall.visibility = View.VISIBLE
                nativeGBig.visibility = View.GONE
            }


        } catch (e: Exception) {

        }
    }


    fun showNativeAd(
        background: Int? = null, primaryColor: Int? = null, callback: (Boolean) -> Unit
    ) {

        try {
            SimpleApplicationClass.simpleApplicationClassInstance.returnNativeAdCls()
                .visibleAdIfReady(nativeGBig,
                    frameNativeBig,
                    nativeGSmall,
                    nativeGSmallFrame,
                    isFromRecyclerview,
                    background,
                    primaryColor,
                    object : AdCallbacks {
                        override fun onAdLoadFailed() {
                            callback.invoke(false)
                        }

                        override fun onAdLoaded() {
                            callback.invoke(true)
                        }
                    })
        } catch (e: Exception) {
        }
    }


    fun hideNativeGBig() {
        view.visibility = View.GONE
    } //    Call<GameModel> call;

}