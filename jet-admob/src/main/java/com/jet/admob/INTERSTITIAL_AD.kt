@file:Suppress("DEPRECATION")

package com.jet.admob

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.ads.AdError as GoogleAdError
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
 * @param isAutoloading Whether to automatically load the ad when the state is first created and after when an ad is dismissed.
 *
 * @author Miroslav Hýbler <br>
 * created on 21.01.2024
 * @since 1.0.0
 */
@JetAdMobAlpha
class AdMobInterstitialAdState private constructor(
    private val context: Context,
    private val adUnitId: String,
    private val isAutoloading: Boolean,
    private var requestConfig: AdRequestConfig,
    @Suppress("UNUSED_PARAMETER") compatibilityMarker: Unit,
) {

    constructor(
        context: Context,
        adUnitId: String,
        isAutoloading: Boolean = true,
    ) : this(
        context = context,
        adUnitId = adUnitId,
        isAutoloading = isAutoloading,
        requestConfig = AdRequestConfig(),
        compatibilityMarker = Unit,
    )

    internal constructor(
        context: Context,
        adUnitId: String,
        isAutoloading: Boolean,
        requestConfig: AdRequestConfig,
    ) : this(
        context = context,
        adUnitId = adUnitId,
        isAutoloading = isAutoloading,
        requestConfig = requestConfig,
        compatibilityMarker = Unit,
    )

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
    @Deprecated(message = LEGACY_GOOGLE_MOBILE_ADS_SDK_DEPRECATION_MESSAGE)
    var adError: LoadAdError? by mutableStateOf(value = null)
        private set

    /** SDK-independent details about the last loading failure. */
    var errorInfo: AdErrorInfo? by mutableStateOf(value = null)
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
        loadAd(requestConfig = requestConfig)
    }

    /** Loads a new interstitial using SDK-independent request options. */
    fun loadAd(requestConfig: AdRequestConfig) {
        if (isAdLoaded || interstitialAd != null) return
        this.requestConfig = requestConfig

        InterstitialAd.load(
            context,
            adUnitId,
            requestConfig.toAdRequest(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isAdLoaded = true
                    adError = null
                    errorInfo = null
                    presentationState = AdPresentationState.Idle
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    isAdLoaded = false
                    adError = error
                    errorInfo = error.toAdErrorInfo()
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
        val activity = context.findActivity() ?: run {
            Log.e(
                "INTERSTITIAL_AD",
                "Unable to show InterstitialAd, the context.findActivity() returned null! " +
                        "Please report an issue https://github.com/miroslavhybler/jet-admob-ads/issues " +
                        "here and provide all the details that can be useful for debugging."
            )
            return
        }
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

                override fun onAdFailedToShowFullScreenContent(adError: GoogleAdError) {
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
    class FailedToShow @Deprecated(
        message = LEGACY_GOOGLE_MOBILE_ADS_SDK_DEPRECATION_MESSAGE,
    ) constructor(
        @Deprecated(message = LEGACY_GOOGLE_MOBILE_ADS_SDK_DEPRECATION_MESSAGE)
        val error: GoogleAdError,
    ) : AdPresentationState {
        val errorInfo: AdErrorInfo = error.toAdErrorInfo()
    }
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
    isAutoloading: Boolean = true
): AdMobInterstitialAdState {
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


/** Creates and remembers an interstitial state with SDK-independent request options. */
@JetAdMobAlpha
@Composable
fun rememberAdMobInterstitialAdState(
    adUnitId: String,
    requestConfig: AdRequestConfig,
    isAutoloading: Boolean = true,
): AdMobInterstitialAdState {
    val context = LocalContext.current
    return remember(context, adUnitId, requestConfig, isAutoloading) {
        AdMobInterstitialAdState(
            context = context,
            adUnitId = adUnitId,
            isAutoloading = isAutoloading,
            requestConfig = requestConfig,
        )
    }
}
