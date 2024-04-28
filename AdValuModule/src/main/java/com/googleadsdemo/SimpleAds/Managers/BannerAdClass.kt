package com.ipomarketpro.motion.globalclasses

import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.browser.customtabs.CustomTabsIntent
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.googleadsdemo.SimpleAds.Application.SimpleApplicationClass
import com.googleadsdemo.SimpleAds.model.AppDetailModel
import com.googleadsdemo.SimpleAds.Utils.Gi
import com.googleadsdemo.SimpleAds.Utils.sharedPrefConfig
import com.vanillavideoplayer.videoplayer.R
import java.util.Random

class BannerAdManager {
    fun showBannerAd(
        bannerLayout: RelativeLayout,
        frameLayout: FrameLayout,
        loaderLayout: RelativeLayout
    ) {
        val appDetails = SimpleApplicationClass.liveActivity.sharedPrefConfig.appDetails

        bannerLayout.visibility = View.INVISIBLE

        if (!isNetworkAvailable()) {
            hideLoaderAndBanner(loaderLayout, bannerLayout)
            return
        }

        if (isAdStatusOff(appDetails) || isGoogleAdStatusOff(appDetails)) {
            hideLoaderAndBanner(loaderLayout, bannerLayout)
            return
        }

        if (!isAdmobBannerAvailable(appDetails) && isGameZopeAdAvailable(appDetails)) {
            showCustomBannerAd(frameLayout, bannerLayout, loaderLayout)
        } else {
            showAdMobBanner(frameLayout, bannerLayout, loaderLayout, appDetails.admobAppBanner)
        }
    }

    private fun showAdMobBanner(
        frameLayout: FrameLayout,
        bannerLayout: RelativeLayout,
        loaderLayout: RelativeLayout,
        admobAppBannerId: String
    ) {
        val adView = AdView(SimpleApplicationClass.liveActivity)
        adView.setAdSize(AdSize.BANNER)
        adView.adUnitId = admobAppBannerId

        frameLayout.removeAllViews()
        frameLayout.addView(adView)

        adView.loadAd(AdRequest.Builder().build())

        adView.adListener = object : AdListener() {
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                showCustomBannerAd(frameLayout, bannerLayout, loaderLayout)
            }

            override fun onAdLoaded() {
                loaderLayout.visibility = View.GONE
                bannerLayout.visibility = View.VISIBLE
            }

            override fun onAdImpression() {

            }
        }
    }

    private fun showCustomBannerAd(
        frameLayout: FrameLayout,
        bannerLayout: RelativeLayout,
        loaderLayout: RelativeLayout
    ) {
        val imageView = ImageView(SimpleApplicationClass.liveActivity)
        imageView.scaleType = ImageView.ScaleType.FIT_XY
        imageView.setBackgroundColor(
            SimpleApplicationClass.liveActivity.resources.getColor(R.color.transperent)
        )

        frameLayout.addView(imageView)

        val bannerList = SimpleApplicationClass.liveActivity.sharedPrefConfig.adDetails?.bannerlist ?: emptyList()
        val randomIndex = Random().nextInt(bannerList.size)
        val imageUrl = bannerList.getOrNull(randomIndex)

        val requestOptions = RequestOptions().transforms(CenterCrop(), RoundedCorners(5))
        if (SimpleApplicationClass.liveActivity.isDestroyed) return
        Glide.with(SimpleApplicationClass.liveActivity)
            .load(imageUrl)
            .transition(DrawableTransitionOptions.withCrossFade())
            .apply(requestOptions)
            .into(object : CustomTarget<Drawable?>() {
                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable?>?) {
                    imageView.setImageDrawable(resource)
                    imageView.layoutParams = FrameLayout.LayoutParams(bannerLayout.width, bannerLayout.height)
                    imageView.setOnClickListener {
                        val customTabsIntent = CustomTabsIntent.Builder().build()
                        customTabsIntent.launchUrl(SimpleApplicationClass.liveActivity, Uri.parse(SimpleApplicationClass.liveActivity.sharedPrefConfig.appDetails.customBannerOnClick))
                    }
                    loaderLayout.visibility = View.GONE
                    bannerLayout.visibility = View.VISIBLE
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }

    private fun isAdStatusOff(appDetails: AppDetailModel): Boolean {
        return appDetails.adStatus.isBlank() || appDetails.adStatus == "OFF"
    }

    private fun isGoogleAdStatusOff(appDetails: AppDetailModel): Boolean {
        return appDetails.googleAdStatus.isBlank() || appDetails.googleAdStatus == "OFF"
    }

    private fun isAdmobBannerAvailable(appDetails: AppDetailModel): Boolean {
        return !appDetails.admobAppBanner.isNullOrBlank()
    }

    private fun isNetworkAvailable(): Boolean {
        return Gi().networkAvailableChheKeNai(SimpleApplicationClass.liveActivity.applicationContext)
    }

    private fun isGameZopeAdAvailable(appDetails: AppDetailModel): Boolean {
        return !appDetails.gameZopeStatus.isNullOrBlank() && appDetails.gameZopeStatus != "OFF"
    }

    private fun hideLoaderAndBanner(loaderLayout: RelativeLayout, bannerLayout: RelativeLayout) {
        loaderLayout.visibility = View.GONE
        bannerLayout.visibility = View.GONE
    }
}
