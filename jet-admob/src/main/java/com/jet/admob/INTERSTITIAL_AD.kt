package com.jet.admob

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAd
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAdEventCallback
import com.jet.admob.annotations.JetAdMobAlpha

/**
 * A state object that can be used to control and observe the state of an AdMob interstitial ad.
 *
 * @param context The current Android context.
 * @param adUnitId The ad unit ID for the interstitial ad.
 * @param isAutoloading Whether to automatically load the ad when the state is first created and after when an ad is dismissed.
 *
 * @author Miroslav Hýbler <br>
 * created on 21.01.2024
 * @since 1.0.0
 */
@JetAdMobAlpha
class AdMobInterstitialAdState constructor(
    private val context: Context,
    private val adUnitId: String,
    private val isAutoloading: Boolean = true,
) {

    private var interstitialAd: InterstitialAd? = null
    private var presentingAd: InterstitialAd? = null
    private var isLoading: Boolean = false
    private var isDestroyed: Boolean = false

    /**
     * `True` if an ad is currently loaded and ready to be shown.
     * @since 1.0.0
     */
    var isAdLoaded: Boolean by mutableStateOf(value = false)
        private set

    /**
     * Holds the [LoadAdError] if the last ad request failed. `Null` otherwise.
     * @since 1.0.0
     */
    var adError: LoadAdError? by mutableStateOf(value = null)
        private set

    /**
     * Represents the current presentation state of the ad.
     * @since 1.0.0
     */
    var presentationState: AdPresentationState by mutableStateOf(value = AdPresentationState.Idle)
        private set


    init {
        if (isAutoloading) {
            loadAd()
        }
    }

    /**
     * Loads a new interstitial ad.
     * @since 1.0.0
     */
    fun loadAd() {
        runOnMainThread {
            loadAdOnMainThread()
        }
    }

    private fun loadAdOnMainThread() {
        if (isDestroyed || isLoading || isAdLoaded || interstitialAd != null) return

        isLoading = true
        val adRequest = AdRequest.Builder(adUnitId).build()
        InterstitialAd.load(
            adRequest,
            object : AdLoadCallback<InterstitialAd> {
                override fun onAdLoaded(ad: InterstitialAd) {
                    runOnMainThread {
                        isLoading = false
                        if (isDestroyed) {
                            ad.destroy()
                            return@runOnMainThread
                        }

                        interstitialAd?.destroy()
                        interstitialAd = ad
                        isAdLoaded = true
                        adError = null
                        presentationState = AdPresentationState.Idle
                    }
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    runOnMainThread {
                        isLoading = false
                        if (isDestroyed) return@runOnMainThread

                        interstitialAd?.destroy()
                        interstitialAd = null
                        isAdLoaded = false
                        this@AdMobInterstitialAdState.adError = adError
                    }
                }
            },
        )
    }

    /**
     * Shows the loaded interstitial ad if it's ready.
     *
     * @param onAdDismissed A lambda to be invoked when the ad is dismissed. This is a good place
     * to navigate to the next screen or grant a reward.
     * @since 1.0.0
     */
    fun show(
        onAdDismissed: () -> Unit = {},
    ) {
        runOnMainThread {
            showOnMainThread(onAdDismissed = onAdDismissed)
        }
    }

    private fun showOnMainThread(onAdDismissed: () -> Unit) {
        if (isDestroyed) return

        val activity = context.findActivity() ?: run {
            Log.e(
                "INTERSTITIAL_AD",
                "Unable to show InterstitialAd, the context.findActivity() returned null! " +
                        "Please report an issue https://github.com/miroslavhybler/jet-admob-ads/issues " +
                        "here and provide all the details that can be useful for debugging."
            )
            return
        }
        val ad = interstitialAd ?: return

        interstitialAd = null
        presentingAd = ad
        isAdLoaded = false

        ad.adEventCallback = object : InterstitialAdEventCallback {
            override fun onAdDismissedFullScreenContent() {
                runOnMainThread {
                    releasePresentingAd(ad)
                    if (isDestroyed) return@runOnMainThread

                    presentationState = AdPresentationState.Dismissed
                    onAdDismissed()
                    if (isAutoloading) {
                        loadAdOnMainThread()
                    }
                }
            }

            override fun onAdFailedToShowFullScreenContent(
                fullScreenContentError: FullScreenContentError,
            ) {
                runOnMainThread {
                    releasePresentingAd(ad)
                    if (isDestroyed) return@runOnMainThread

                    presentationState = AdPresentationState.FailedToShow(fullScreenContentError)
                    isAdLoaded = false
                }
            }

            override fun onAdShowedFullScreenContent() {
                runOnMainThread {
                    if (!isDestroyed) {
                        presentationState = AdPresentationState.Showed
                    }
                }
            }
        }
        ad.show(activity)
    }

    private fun releasePresentingAd(ad: InterstitialAd) {
        if (presentingAd === ad) {
            presentingAd = null
            ad.destroy()
        }
    }

    internal fun destroy() {
        runOnMainThread {
            if (isDestroyed) return@runOnMainThread

            isDestroyed = true
            isLoading = false
            interstitialAd?.destroy()
            presentingAd?.destroy()
            interstitialAd = null
            presentingAd = null
            isAdLoaded = false
        }
    }
}


/**
 * Represents the presentation state of an interstitial ad.
 * @since 1.0.0
 */
sealed interface AdPresentationState {

    /**
     * The ad is not currently being shown, default state.
     * @since 1.0.0
     */
    object Idle : AdPresentationState

    /**
     * The ad has been successfully shown to the user.
     * @since 1.0.0
     */
    object Showed : AdPresentationState

    /**
     * The ad was dismissed by the user.
     * @since 1.0.0
     */
    object Dismissed : AdPresentationState

    /**
     * The ad failed to show.
     * @since 1.0.0
     */
    class FailedToShow constructor(
        val error: FullScreenContentError,
    ) : AdPresentationState
}


/**
 * Creates and remembers an [AdMobInterstitialAdState].
 *
 * @param adUnitId The ad unit ID to use for the ad.
 * @param isAutoloading Whether to automatically load an ad.
 * Defaults to `true`. Setting this to `true` is recommended for a better user experience, as ads
 * are pre-loaded and can be shown instantly. However, this might affect your AdMob statistics,
 * as an ad request is made even if the ad is not shown. If this is a concern, you can set it to
 * `false` and manually call [AdMobInterstitialAdState.loadAd] to control when ads are loaded.
 * @since 1.0.0
 */
@JetAdMobAlpha
@Composable
fun rememberAdMobInterstitialAdState(
    adUnitId: String,
    isAutoloading: Boolean = true,
): AdMobInterstitialAdState {
    val context = LocalContext.current
    val state = remember(context, adUnitId, isAutoloading) {
        AdMobInterstitialAdState(
            context = context,
            adUnitId = adUnitId,
            isAutoloading = isAutoloading,
        )
    }

    DisposableEffect(key1 = state) {
        onDispose {
            state.destroy()
        }
    }

    return state
}
