@file:Suppress("OPT_IN_USAGE", "DEPRECATION")

package com.jet.admob

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize as GoogleAdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.jet.admob.annotations.JetAdMobAlpha


/**
 * State holder for a banner ad.
 *
 * Hoist this state above lazy list items to keep the underlying [AdView] alive while the list
 * item is temporarily disposed.
 *
 * @since 1.0.0
 */
@JetAdMobAlpha
class AdMobBannerState internal constructor(
    val adUnitId: String,
    @Deprecated(
        message = "Use size. The legacy Google Mobile Ads AdSize will be removed in 2.0.0.",
        replaceWith = ReplaceWith(expression = "size"),
    )
    val adSize: GoogleAdSize,
    val size: AdSize,
    private val adView: AdView?,
    private var loadAdRequest: () -> AdRequest,
    private var adListener: AdListener?,
) {

    internal constructor(
        adUnitId: String,
        adSize: GoogleAdSize,
        adView: AdView?,
        loadAdRequest: () -> AdRequest,
        adListener: AdListener?,
    ) : this(
        adUnitId = adUnitId,
        adSize = adSize,
        size = AdSize.fromGoogleAdSize(adSize),
        adView = adView,
        loadAdRequest = loadAdRequest,
        adListener = adListener,
    )

    private var isLoading: Boolean = false
    private var isLoaded: Boolean = false
    private var isDestroyed: Boolean = false

    internal val isLoadedForInspection: Boolean
        get() = adView == null

    companion object {

        /**
         * Constant that will cause the height of the ad to scale based on the height of the device
         * in the current orientation.
         *
         * @since 1.0.0
         */
        @Deprecated(message = LEGACY_GOOGLE_MOBILE_ADS_SDK_DEPRECATION_MESSAGE)
        @JvmField
        val AUTO_HEIGHT: Int = GoogleAdSize.AUTO_HEIGHT

        /**
         * Mobile Marketing Association banner ad size (320x50 dp).
         *
         * @since 1.0.0
         */
        @Deprecated(
            message = "Use AdSize.BANNER.",
            replaceWith = ReplaceWith(
                expression = "AdSize.BANNER",
                imports = ["com.jet.admob.AdSize"],
            ),
        )
        @JvmField
        val BANNER: GoogleAdSize = GoogleAdSize.BANNER

        /**
         * A dynamically sized banner that matches its parent's width and changes height to match
         * the loaded ad content.
         *
         * @since 1.0.0
         */
        @Deprecated(
            message = "Use AdSize.FLUID.",
            replaceWith = ReplaceWith(
                expression = "AdSize.FLUID",
                imports = ["com.jet.admob.AdSize"],
            ),
        )
        @JvmField
        val FLUID: GoogleAdSize = GoogleAdSize.FLUID

        /**
         * Interactive Advertising Bureau full banner ad size (468x60 dp).
         *
         * @since 1.0.0
         */
        @Deprecated(
            message = "Use AdSize.FULL_BANNER.",
            replaceWith = ReplaceWith(
                expression = "AdSize.FULL_BANNER",
                imports = ["com.jet.admob.AdSize"],
            ),
        )
        @JvmField
        val FULL_BANNER: GoogleAdSize = GoogleAdSize.FULL_BANNER

        /**
         * Constant that will cause the width of the ad to match the width of the device in the
         * current orientation.
         *
         * @since 1.0.0
         */
        @Deprecated(message = LEGACY_GOOGLE_MOBILE_ADS_SDK_DEPRECATION_MESSAGE)
        @JvmField
        val FULL_WIDTH: Int = GoogleAdSize.FULL_WIDTH

        /**
         * An invalid legacy ad size that will cause the ad request to fail immediately.
         *
         * @since 1.0.0
         */
        @Deprecated(message = LEGACY_GOOGLE_MOBILE_ADS_SDK_DEPRECATION_MESSAGE)
        @JvmField
        val INVALID: GoogleAdSize = GoogleAdSize.INVALID

        /**
         * Large banner ad size (320x100 dp).
         *
         * @since 1.0.0
         */
        @Deprecated(
            message = "Use AdSize.LARGE_BANNER.",
            replaceWith = ReplaceWith(
                expression = "AdSize.LARGE_BANNER",
                imports = ["com.jet.admob.AdSize"],
            ),
        )
        @JvmField
        val LARGE_BANNER: GoogleAdSize = GoogleAdSize.LARGE_BANNER

        /**
         * Interactive Advertising Bureau leaderboard ad size (728x90 dp).
         *
         * @since 1.0.0
         */
        @Deprecated(
            message = "Use AdSize.LEADERBOARD.",
            replaceWith = ReplaceWith(
                expression = "AdSize.LEADERBOARD",
                imports = ["com.jet.admob.AdSize"],
            ),
        )
        @JvmField
        val LEADERBOARD: GoogleAdSize = GoogleAdSize.LEADERBOARD

        /**
         * Interactive Advertising Bureau medium rectangle ad size (300x250 dp).
         *
         * @since 1.0.0
         */
        @Deprecated(
            message = "Use AdSize.MEDIUM_RECTANGLE.",
            replaceWith = ReplaceWith(
                expression = "AdSize.MEDIUM_RECTANGLE",
                imports = ["com.jet.admob.AdSize"],
            ),
        )
        @JvmField
        val MEDIUM_RECTANGLE: GoogleAdSize = GoogleAdSize.MEDIUM_RECTANGLE

        /**
         * IAB wide skyscraper ad size (160x600 dp).
         *
         * @since 1.0.0
         */
        @Deprecated(
            message = "Use AdSize.WIDE_SKYSCRAPER.",
            replaceWith = ReplaceWith(
                expression = "AdSize.WIDE_SKYSCRAPER",
                imports = ["com.jet.admob.AdSize"],
            ),
        )
        @JvmField
        val WIDE_SKYSCRAPER: GoogleAdSize = GoogleAdSize.WIDE_SKYSCRAPER

        /**
         * Smart banner ad size.
         *
         * @since 1.0.0
         */
        @Deprecated(
            message = LEGACY_GOOGLE_MOBILE_ADS_SDK_DEPRECATION_MESSAGE,
        )
        @Suppress("DEPRECATION")
        @JvmField
        val SMART_BANNER: GoogleAdSize = GoogleAdSize.SMART_BANNER

        /**
         * Returns an inline adaptive banner [AdSize] with the given width and max height.
         *
         * @since 1.0.0
         */
        @Deprecated(
            message = "Use AdSize.getInlineAdaptiveBannerAdSize().",
            replaceWith = ReplaceWith(
                expression = "AdSize.getInlineAdaptiveBannerAdSize(width, maxHeight)",
                imports = ["com.jet.admob.AdSize"],
            ),
        )
        @JvmStatic
        fun getInlineAdaptiveBannerAdSize(
            width: Int,
            maxHeight: Int,
        ): GoogleAdSize = GoogleAdSize.getInlineAdaptiveBannerAdSize(width, maxHeight)

        /**
         * Returns an inline adaptive banner [AdSize] for the current orientation.
         *
         * @since 1.0.0
         */
        @Deprecated(
            message = "Use AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize().",
            replaceWith = ReplaceWith(
                expression = "AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(context, width)",
                imports = ["com.jet.admob.AdSize"],
            ),
        )
        @JvmStatic
        fun getCurrentOrientationInlineAdaptiveBannerAdSize(
            context: Context,
            width: Int,
        ): GoogleAdSize =
            GoogleAdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(context, width)

        /**
         * Returns an inline adaptive banner [AdSize] for landscape orientation.
         *
         * @since 1.0.0
         */
        @Deprecated(
            message = "Use AdSize.getLandscapeInlineAdaptiveBannerAdSize().",
            replaceWith = ReplaceWith(
                expression = "AdSize.getLandscapeInlineAdaptiveBannerAdSize(context, width)",
                imports = ["com.jet.admob.AdSize"],
            ),
        )
        @JvmStatic
        fun getLandscapeInlineAdaptiveBannerAdSize(
            context: Context,
            width: Int,
        ): GoogleAdSize = GoogleAdSize.getLandscapeInlineAdaptiveBannerAdSize(context, width)

        /**
         * Returns an inline adaptive banner [AdSize] for portrait orientation.
         *
         * @since 1.0.0
         */
        @Deprecated(
            message = "Use AdSize.getPortraitInlineAdaptiveBannerAdSize().",
            replaceWith = ReplaceWith(
                expression = "AdSize.getPortraitInlineAdaptiveBannerAdSize(context, width)",
                imports = ["com.jet.admob.AdSize"],
            ),
        )
        @JvmStatic
        fun getPortraitInlineAdaptiveBannerAdSize(
            context: Context,
            width: Int,
        ): GoogleAdSize = GoogleAdSize.getPortraitInlineAdaptiveBannerAdSize(context, width)

        /**
         * Returns a large anchored adaptive banner [AdSize] for the current orientation.
         *
         * @since 1.0.0
         */
        @Deprecated(
            message = "Use AdSize.getLargeAnchoredAdaptiveBannerAdSize().",
            replaceWith = ReplaceWith(
                expression = "AdSize.getLargeAnchoredAdaptiveBannerAdSize(context, width)",
                imports = ["com.jet.admob.AdSize"],
            ),
        )
        @JvmStatic
        fun getLargeAnchoredAdaptiveBannerAdSize(
            context: Context,
            width: Int,
        ): GoogleAdSize = GoogleAdSize.getLargeAnchoredAdaptiveBannerAdSize(context, width)

        /**
         * Returns a large anchored adaptive banner [AdSize] for landscape orientation.
         *
         * @since 1.0.0
         */
        @Deprecated(
            message = "Use AdSize.getLargeLandscapeAnchoredAdaptiveBannerAdSize().",
            replaceWith = ReplaceWith(
                expression = "AdSize.getLargeLandscapeAnchoredAdaptiveBannerAdSize(context, width)",
                imports = ["com.jet.admob.AdSize"],
            ),
        )
        @JvmStatic
        fun getLargeLandscapeAnchoredAdaptiveBannerAdSize(
            context: Context,
            width: Int,
        ): GoogleAdSize =
            GoogleAdSize.getLargeLandscapeAnchoredAdaptiveBannerAdSize(context, width)

        /**
         * Returns a large anchored adaptive banner [AdSize] for portrait orientation.
         *
         * @since 1.0.0
         */
        @Deprecated(
            message = "Use AdSize.getLargePortraitAnchoredAdaptiveBannerAdSize().",
            replaceWith = ReplaceWith(
                expression = "AdSize.getLargePortraitAnchoredAdaptiveBannerAdSize(context, width)",
                imports = ["com.jet.admob.AdSize"],
            ),
        )
        @JvmStatic
        fun getLargePortraitAnchoredAdaptiveBannerAdSize(
            context: Context,
            width: Int,
        ): GoogleAdSize =
            GoogleAdSize.getLargePortraitAnchoredAdaptiveBannerAdSize(context, width)

        /**
         * Returns an anchored adaptive banner [AdSize] for the current orientation.
         *
         * @since 1.0.0
         */
        @Deprecated(
            message = LEGACY_GOOGLE_MOBILE_ADS_SDK_DEPRECATION_MESSAGE,
        )
        @Suppress("DEPRECATION")
        @JvmStatic
        fun getCurrentOrientationAnchoredAdaptiveBannerAdSize(
            context: Context,
            width: Int,
        ): GoogleAdSize =
            GoogleAdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, width)

        /**
         * Returns an anchored adaptive banner [AdSize] for landscape orientation.
         *
         * @since 1.0.0
         */
        @Deprecated(
            message = LEGACY_GOOGLE_MOBILE_ADS_SDK_DEPRECATION_MESSAGE,
        )
        @Suppress("DEPRECATION")
        @JvmStatic
        fun getLandscapeAnchoredAdaptiveBannerAdSize(
            context: Context,
            width: Int,
        ): GoogleAdSize = GoogleAdSize.getLandscapeAnchoredAdaptiveBannerAdSize(context, width)

        /**
         * Returns an anchored adaptive banner [AdSize] for portrait orientation.
         *
         * @since 1.0.0
         */
        @Deprecated(
            message = LEGACY_GOOGLE_MOBILE_ADS_SDK_DEPRECATION_MESSAGE,
        )
        @Suppress("DEPRECATION")
        @JvmStatic
        fun getPortraitAnchoredAdaptiveBannerAdSize(
            context: Context,
            width: Int,
        ): GoogleAdSize = GoogleAdSize.getPortraitAnchoredAdaptiveBannerAdSize(context, width)
    }

    /**
     * Loads the banner ad if it is not already loaded or loading.
     *
     * @param loadAdRequest A lambda that returns an [AdRequest].
     * @param adListener An [AdListener] for observing ad events.
     * @since 1.0.0
     */
    @Deprecated(message = LEGACY_GOOGLE_MOBILE_ADS_SDK_DEPRECATION_MESSAGE)
    fun loadAd(
        loadAdRequest: () -> AdRequest = this.loadAdRequest,
        adListener: AdListener? = this.adListener,
    ) {
        val adView = adView ?: return
        if (isLoading || isLoaded || isDestroyed) return

        isLoading = true
        this.loadAdRequest = loadAdRequest
        this.adListener = adListener
        updateAdListener(adListener = adListener)
        adView.loadAd(loadAdRequest())
    }

    internal fun getAdView(): AdView? {
        (adView?.parent as? ViewGroup)?.removeView(adView)
        return adView
    }

    internal fun updateLoadOptions(
        loadAdRequest: () -> AdRequest,
        adListener: AdListener?,
    ) {
        this.loadAdRequest = loadAdRequest
        this.adListener = adListener
        updateAdListener(adListener = adListener)
    }

    private fun updateAdListener(adListener: AdListener?) {
        adView?.adListener = object : AdListener() {
            override fun onAdClicked() {
                adListener?.onAdClicked()
            }

            override fun onAdClosed() {
                adListener?.onAdClosed()
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                isLoading = false
                isLoaded = false
                adListener?.onAdFailedToLoad(adError)
            }

            override fun onAdImpression() {
                adListener?.onAdImpression()
            }

            override fun onAdLoaded() {
                isLoading = false
                isLoaded = true
                adListener?.onAdLoaded()
            }

            override fun onAdOpened() {
                adListener?.onAdOpened()
            }

            override fun onAdSwipeGestureClicked() {
                adListener?.onAdSwipeGestureClicked()
            }
        }
    }

    internal fun resume() {
        adView?.resume()
    }

    internal fun pause() {
        adView?.pause()
    }

    internal fun destroy() {
        isDestroyed = true
        adView?.destroy()
    }
}


/**
 * Creates and remembers an [AdMobBannerState].
 *
 * Hoist this above lazy list items if the banner is displayed inside a `LazyColumn`/`LazyRow`.
 *
 * @param adUnitId The ad unit ID for this banner ad.
 * @param adSize The SDK-independent size of the banner ad.
 * @since 1.1.0
 */
@JetAdMobAlpha
@Composable
fun rememberAdMobBannerState(
    adUnitId: String,
    adSize: AdSize,
): AdMobBannerState = rememberAdMobBannerStateInternal(
    adUnitId = adUnitId,
    adSize = adSize,
    loadAdRequest = { AdRequest.Builder().build() },
    adListener = null,
)


/**
 * Creates and remembers an [AdMobBannerState] with legacy request customization.
 *
 * @since 1.1.0
 */
@JetAdMobAlpha
@Deprecated(message = LEGACY_GOOGLE_MOBILE_ADS_SDK_DEPRECATION_MESSAGE)
@Composable
fun rememberAdMobBannerState(
    adUnitId: String,
    adSize: AdSize,
    loadAdRequest: () -> AdRequest,
    adListener: AdListener? = null,
): AdMobBannerState = rememberAdMobBannerStateInternal(
    adUnitId = adUnitId,
    adSize = adSize,
    loadAdRequest = loadAdRequest,
    adListener = adListener,
)


/**
 * Creates and remembers an [AdMobBannerState] using the legacy SDK's ad size.
 *
 * @since 1.0.0
 */
@JetAdMobAlpha
@Deprecated(message = LEGACY_GOOGLE_MOBILE_ADS_SDK_DEPRECATION_MESSAGE)
@Composable
fun rememberAdMobBannerState(
    adUnitId: String,
    adSize: GoogleAdSize,
    loadAdRequest: () -> AdRequest = { AdRequest.Builder().build() },
    adListener: AdListener? = null,
): AdMobBannerState = rememberAdMobBannerStateInternal(
    adUnitId = adUnitId,
    adSize = AdSize.fromGoogleAdSize(adSize),
    googleAdSize = adSize,
    loadAdRequest = loadAdRequest,
    adListener = adListener,
)


@Composable
private fun rememberAdMobBannerStateInternal(
    adUnitId: String,
    adSize: AdSize,
    googleAdSize: GoogleAdSize? = null,
    loadAdRequest: () -> AdRequest,
    adListener: AdListener?,
): AdMobBannerState {
    val context = LocalContext.current
    val isInspection = LocalInspectionMode.current
    val resolvedAdSize = remember(context, adSize, googleAdSize) {
        googleAdSize ?: adSize.toGoogleAdSize(context)
    }

    val state = remember(context, adUnitId, adSize, resolvedAdSize, isInspection) {
        AdMobBannerState(
            adUnitId = adUnitId,
            adSize = resolvedAdSize,
            size = adSize,
            loadAdRequest = loadAdRequest,
            adListener = adListener,
            adView = if (isInspection) {
                null
            } else {
                AdView(context).apply {
                    id = View.generateViewId()
                    this.adUnitId = adUnitId
                    setAdSize(resolvedAdSize)
                }
            },
        )
    }

    SideEffect {
        state.updateLoadOptions(
            loadAdRequest = loadAdRequest,
            adListener = adListener,
        )
    }

    LaunchedEffect(key1 = state) {
        state.loadAd(
            loadAdRequest = loadAdRequest,
            adListener = adListener,
        )
    }

    DisposableEffect(key1 = state) {
        onDispose {
            state.destroy()
        }
    }

    return state
}


/**
 * A composable that displays a banner ad from AdMob.
 *
 * **Important:** Modifying or obscuring the ad creative is a violation of AdMob policies.
 * Do not apply rounded corners or other clip shapes to this composable, as it can
 * obscure the ad and/or the required AdChoices icon, leading to suspension of your
 * AdMob account.
 *
 * More info about Banner ads: [Banner ad overview](https://developers.google.com/admob/android/banner)
 * Sizes are described by the SDK-independent [AdSize].
 *
 * @param modifier The modifier to be applied to the ad.
 * @param adUnitId The ad unit ID for this banner ad. See [Test ads](https://developers.google.com/admob/android/test-ads) for test ad unit IDs.
 * @param adSize The size of the banner ad.
 * @param loadAdRequest A lambda that returns an [AdRequest]. Defaults to a new [AdRequest.Builder().build()].
 * @param adListener An [AdListener] for observing ad events.
 * @param preOccupySpace If true, the composable will pre-occupy the space of the ad size before the ad is loaded. This can prevent layout shifts.
 *
 * @author Miroslav Hýbler <br>
 * created on 06.01.2026
 * @since 1.0.0
 */
@JetAdMobAlpha
@Composable
private fun AdMobBanner(
    modifier: Modifier = Modifier,
    adUnitId: String,
    adSize: AdSize,
    loadAdRequest: () -> AdRequest = { AdRequest.Builder().build() },
    adListener: AdListener? = null,
    preOccupySpace: Boolean = false,
) {
    val state = rememberAdMobBannerState(
        adUnitId = adUnitId,
        adSize = adSize,
        loadAdRequest = loadAdRequest,
        adListener = adListener,
    )

    AdMobBanner(
        modifier = modifier,
        state = state,
        preOccupySpace = preOccupySpace,
    )
}


/**
 * A composable that displays a banner ad from AdMob using a hoisted [AdMobBannerState].
 *
 * Hoist [state] above lazy list items to avoid reloading the ad whenever the item is disposed and
 * later composed again.
 *
 * @param modifier The modifier to be applied to the ad.
 * @param state The remembered state that owns the underlying [AdView].
 * @param preOccupySpace If true, the composable will pre-occupy the space of the ad size before the ad is loaded. This can prevent layout shifts.
 * @since 1.0.0
 */
@JetAdMobAlpha
@Composable
fun AdMobBanner(
    modifier: Modifier = Modifier,
    state: AdMobBannerState,
    preOccupySpace: Boolean = false,
) {
    val isInspection = LocalInspectionMode.current

    if (isInspection || state.isLoadedForInspection) {
        AdMobBannerInspection(
            modifier = modifier.size(
                width = state.adSize.width.dp,
                height = state.adSize.height.dp
            ),
            adSize = state.adSize,
        )
    } else {
        AdMobBannerImplementation(
            modifier = modifier,
            preOccupySpace = preOccupySpace,
            state = state,
        )
    }
}


/**
 * Implementation of Banner
 * @since 1.0.0
 */
@Composable
private fun AdMobBannerImplementation(
    modifier: Modifier,
    preOccupySpace: Boolean,
    state: AdMobBannerState,
) {

    val adjustedModifier = if (preOccupySpace) {
        modifier.size(
            width = state.adSize.width.dp,
            height = state.adSize.height.dp
        )
    } else {
        modifier.wrapContentSize()
    }

    LifecycleResumeEffect(key1 = state) {
        state.resume()
        onPauseOrDispose {
            state.pause()
        }
    }

    AndroidView(
        modifier = adjustedModifier,
        factory = { state.getAdView() ?: View(it) },
    )
}


/**
 * Preview to show how much space will banner occupy
 * @param adSize The size of the banner ad.
 */
@Composable
private fun AdMobBannerInspection(
    modifier: Modifier,
    adSize: GoogleAdSize,
) {
    val contentColor = LocalContentColor.current
    Column(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "AdMob Banner Ad Preview\nsize: ${adSize}",
            color = contentColor,
            textAlign = TextAlign.Center,
        )
    }
}


@Composable
@PreviewLightDark
private fun AdMobBannerPreview() {
    val bannerState = rememberAdMobBannerState(
        adUnitId = AdMobAdsUtil.TestIds.FIXED_SIZE_BANNER,
        adSize = AdSize.LARGE_BANNER,
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.spacedBy(space = 24.dp)
    ) {
        AdMobBanner(
            modifier = Modifier,
            state = bannerState,
        )
    }
}
