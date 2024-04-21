package com.vanillavideoplayer.commonvanillaads.finalcls


import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import com.ipomarketpro.motion.globalclasses.BannerAdManager
import com.vanillavideoplayer.videoplayer.R

class BigGBanner @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val bannerLayout: RelativeLayout
    private val loaderLayout: RelativeLayout
    private val lottieBanner: LottieAnimationView
    private val adTitleTextView: TextView
    private val frameBanner: FrameLayout
    private var useDirectLoad: Boolean = true

    init {

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.h_banner_big, this, true)

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.BigGBanner)
        useDirectLoad = typedArray.getBoolean(R.styleable.BigGBanner_useDirectLoad, true)
        typedArray.recycle()

        bannerLayout = view.findViewById(R.id.rlBanner)
        loaderLayout = view.findViewById(R.id.rlLoader)
        lottieBanner = view.findViewById(R.id.lottieBanner)
        adTitleTextView = view.findViewById(R.id.txtAdTitle)
        frameBanner = view.findViewById(R.id.frameBanner)



        if (useDirectLoad) {
            showBigGBanner()
        } else {
            hideBigGBanner()
        }
    }

    fun showBigGBanner() {
        bannerLayout.visibility = View.INVISIBLE
        BannerAdManager().showBannerAd(bannerLayout, frameBanner, loaderLayout)
    }

    fun hideBigGBanner() {
        bannerLayout.visibility = View.GONE
    }
}
