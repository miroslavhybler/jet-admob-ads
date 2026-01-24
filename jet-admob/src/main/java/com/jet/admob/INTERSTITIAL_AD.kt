package com.jet.admob

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.jet.admob.annotations.JetAdMobAlpha

/**
 * A state object that can be used to control and observe the state of an AdMob interstitial ad.
 *
 * @param context The current Android context.
 * @param adUnitId The ad unit ID for the interstitial ad.
 * @param isAutoloading Whether to automatically load the ad when the state is first created.
 *
 * @author Miroslav Hýbler <br>
 * created on 21.01.2024
 * @since 1.0.0
 */
@JetAdMobAlpha
class AdMobInterstitialAdState constructor(
    private val context: Context,
    private val adUnitId: String,
    private val isAutoloading: Boolean = true
) {

    /**
     * Holds actual [InterstitialAd].
     * @since 1.0.0
     */
    private var interstitialAd: InterstitialAd? by mutableStateOf(value = null)

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
        if (isAdLoaded || interstitialAd != null) return

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isAdLoaded = true
                    adError = null
                    presentationState = AdPresentationState.Idle
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    isAdLoaded = false
                    adError = error
                }
            }
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
        val activity = context.findActivity() ?: return
        interstitialAd?.let { ad ->
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    presentationState = AdPresentationState.Dismissed
                    interstitialAd = null
                    isAdLoaded = false
                    onAdDismissed()
                    if (isAutoloading) {
                        loadAd() // Preload the next ad
                    }
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    presentationState = AdPresentationState.FailedToShow(adError)
                    interstitialAd = null
                    isAdLoaded = false
                }

                override fun onAdShowedFullScreenContent() {
                    presentationState = AdPresentationState.Showed
                }
            }
            ad.show(activity)
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
        val error: AdError,
    ) : AdPresentationState
}


/**
 * Creates and remembers an [AdMobInterstitialAdState].
 *
 * @param adUnitId The ad unit ID to use for the ad.
 * @param isAutoloading Whether to automatically load the ad.
 * @since 1.0.0
 */
@JetAdMobAlpha
@Composable
fun rememberAdMobInterstitialAdState(
    adUnitId: String,
    isAutoloading: Boolean = true
): AdMobInterstitialAdState {
    //TODO use LocalActivity
    val context = LocalContext.current
    val state = remember(key1 = adUnitId) {
        AdMobInterstitialAdState(
            context = context,
            adUnitId = adUnitId,
            isAutoloading = isAutoloading,
        )
    }
    return state
}


/**
 * Helper function to find the current Activity from a given Context.
 * @since 1.0.0
 */
private fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}