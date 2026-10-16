package com.jet.admob

import android.os.Handler
import android.os.Looper
import com.google.android.libraries.ads.mobile.sdk.common.AdValue
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError


/**
 * Compatibility callback for observing Google Mobile Ads events.
 *
 * The Next-Gen SDK separates loading callbacks from format-specific ad event callbacks. This class
 * keeps one listener surface for jet-admob callers while the library adapts those callbacks
 * internally. Callbacks dispatched by jet-admob are invoked on the main thread.
 *
 * @since 2.0.0
 */
open class AdMobAdListener {

    open fun onAdClicked() = Unit

    open fun onAdClosed() = Unit

    open fun onAdDismissedFullScreenContent() = onAdClosed()

    open fun onAdFailedToLoad(adError: LoadAdError) = Unit

    open fun onAdFailedToRefresh(adError: LoadAdError) = onAdFailedToLoad(adError)

    open fun onAdFailedToShowFullScreenContent(adError: FullScreenContentError) = Unit

    open fun onAdImpression() = Unit

    open fun onAdLoaded() = Unit

    open fun onAdMetadataChanged() = Unit

    open fun onAdOpened() = Unit

    open fun onAdPaid(adValue: AdValue) = Unit

    open fun onAdRefreshed() = onAdLoaded()

    open fun onAdShowedFullScreenContent() = onAdOpened()

    open fun onAdSwipeGestureClicked() = Unit

    open fun onAppEvent(name: String, data: String?) = Unit

    open fun onCustomMuteThisAdReported() = Unit
}


private val mainThreadHandler: Handler by lazy {
    Handler(Looper.getMainLooper())
}


/** Dispatches [block] to the Android main thread, running it immediately when already there. */
internal fun runOnMainThread(block: () -> Unit) {
    if (Looper.myLooper() == Looper.getMainLooper()) {
        block()
    } else {
        mainThreadHandler.post(block)
    }
}
