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
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.banner.AdView
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRefreshCallback
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdValue
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
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
    val adSize: AdSize,
    private val adView: AdView?,
    private var loadAdRequest: () -> BannerAdRequest,
    private var adListener: AdMobAdListener?,
) {

    private var isLoading: Boolean = false
    private var isLoaded: Boolean = false
    private var isDestroyed: Boolean = false

    internal val isLoadedForInspection: Boolean
        get() = adView == null

    companion object {
        /**
         * Mobile Marketing Association banner ad size (320x50 dp).
         *
         * @since 1.0.0
         */
        @JvmField
        val BANNER: AdSize = AdSize.BANNER

        /**
         * A dynamically sized banner that matches its parent's width and changes height to match
         * the loaded ad content.
         *
         * @since 1.0.0
         */
        @JvmField
        val FLUID: AdSize = AdSize.FLUID

        /**
         * Interactive Advertising Bureau full banner ad size (468x60 dp).
         *
         * @since 1.0.0
         */
        @JvmField
        val FULL_BANNER: AdSize = AdSize.FULL_BANNER

        /**
         * Large banner ad size (320x100 dp).
         *
         * @since 1.0.0
         */
        @JvmField
        val LARGE_BANNER: AdSize = AdSize.LARGE_BANNER

        /**
         * Interactive Advertising Bureau leaderboard ad size (728x90 dp).
         *
         * @since 1.0.0
         */
        @JvmField
        val LEADERBOARD: AdSize = AdSize.LEADERBOARD

        /**
         * Interactive Advertising Bureau medium rectangle ad size (300x250 dp).
         *
         * @since 1.0.0
         */
        @JvmField
        val MEDIUM_RECTANGLE: AdSize = AdSize.MEDIUM_RECTANGLE

        /**
         * IAB wide skyscraper ad size (160x600 dp).
         *
         * @since 1.0.0
         */
        @JvmField
        val WIDE_SKYSCRAPER: AdSize = AdSize(160, 600)

        /**
         * Returns an inline adaptive banner [AdSize] with the given width and max height.
         *
         * @since 1.0.0
         */
        @JvmStatic
        fun getInlineAdaptiveBannerAdSize(
            width: Int,
            maxHeight: Int,
        ): AdSize = AdSize.getInlineAdaptiveBannerAdSize(width, maxHeight)

        /**
         * Returns an inline adaptive banner [AdSize] for the current orientation.
         *
         * @since 1.0.0
         */
        @JvmStatic
        fun getCurrentOrientationInlineAdaptiveBannerAdSize(
            context: Context,
            width: Int,
        ): AdSize = AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(context, width)

        /**
         * Returns an inline adaptive banner [AdSize] for landscape orientation.
         *
         * @since 1.0.0
         */
        @JvmStatic
        fun getLandscapeInlineAdaptiveBannerAdSize(
            context: Context,
            width: Int,
        ): AdSize = AdSize.getLandscapeInlineAdaptiveBannerAdSize(context, width)

        /**
         * Returns an inline adaptive banner [AdSize] for portrait orientation.
         *
         * @since 1.0.0
         */
        @JvmStatic
        fun getPortraitInlineAdaptiveBannerAdSize(
            context: Context,
            width: Int,
        ): AdSize = AdSize.getPortraitInlineAdaptiveBannerAdSize(context, width)

        /**
         * Returns a large anchored adaptive banner [AdSize] for the current orientation.
         *
         * @since 1.0.0
         */
        @JvmStatic
        fun getLargeAnchoredAdaptiveBannerAdSize(
            context: Context,
            width: Int,
        ): AdSize = AdSize.getLargeAnchoredAdaptiveBannerAdSize(context, width)

        /**
         * Returns a large anchored adaptive banner [AdSize] for landscape orientation.
         *
         * @since 1.0.0
         */
        @JvmStatic
        fun getLargeLandscapeAnchoredAdaptiveBannerAdSize(
            context: Context,
            width: Int,
        ): AdSize = AdSize.getLargeLandscapeAnchoredAdaptiveBannerAdSize(context, width)

        /**
         * Returns a large anchored adaptive banner [AdSize] for portrait orientation.
         *
         * @since 1.0.0
         */
        @JvmStatic
        fun getLargePortraitAnchoredAdaptiveBannerAdSize(
            context: Context,
            width: Int,
        ): AdSize = AdSize.getLargePortraitAnchoredAdaptiveBannerAdSize(context, width)

        /**
         * Returns an anchored adaptive banner [AdSize] for the current orientation.
         *
         * @since 1.0.0
         */
        @JvmStatic
        fun getCurrentOrientationAnchoredAdaptiveBannerAdSize(
            context: Context,
            width: Int,
        ): AdSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, width)

        /**
         * Returns an anchored adaptive banner [AdSize] for landscape orientation.
         *
         * @since 1.0.0
         */
        @JvmStatic
        fun getLandscapeAnchoredAdaptiveBannerAdSize(
            context: Context,
            width: Int,
        ): AdSize = AdSize.getLandscapeAnchoredAdaptiveBannerAdSize(context, width)

        /**
         * Returns an anchored adaptive banner [AdSize] for portrait orientation.
         *
         * @since 1.0.0
         */
        @JvmStatic
        fun getPortraitAnchoredAdaptiveBannerAdSize(
            context: Context,
            width: Int,
        ): AdSize = AdSize.getPortraitAnchoredAdaptiveBannerAdSize(context, width)
    }

    /**
     * Loads the banner ad if it is not already loaded or loading.
     *
     * @param loadAdRequest A lambda that returns a [BannerAdRequest].
     * @param adListener An [AdMobAdListener] for observing ad events.
     * @since 1.0.0
     */
    fun loadAd(
        loadAdRequest: () -> BannerAdRequest = this.loadAdRequest,
        adListener: AdMobAdListener? = this.adListener,
    ) {
        runOnMainThread {
            this.loadAdRequest = loadAdRequest
            this.adListener = adListener

            val adView = adView ?: return@runOnMainThread
            if (isLoading || isLoaded || isDestroyed) return@runOnMainThread

            isLoading = true
            try {
                adView.loadAd(
                    loadAdRequest(),
                    object : AdLoadCallback<BannerAd> {
                        override fun onAdLoaded(ad: BannerAd) {
                            ad.adEventCallback = createAdEventCallback()
                            ad.bannerAdRefreshCallback = createAdRefreshCallback()

                            runOnMainThread {
                                if (isDestroyed) {
                                    ad.destroy()
                                    return@runOnMainThread
                                }
                                isLoading = false
                                isLoaded = true
                                this@AdMobBannerState.adListener?.onAdLoaded()
                            }
                        }

                        override fun onAdFailedToLoad(adError: LoadAdError) {
                            runOnMainThread {
                                if (isDestroyed) return@runOnMainThread
                                isLoading = false
                                isLoaded = false
                                this@AdMobBannerState.adListener?.onAdFailedToLoad(adError)
                            }
                        }
                    },
                )
            } catch (error: Throwable) {
                isLoading = false
                throw error
            }
        }
    }

    internal fun getAdView(): AdView? {
        (adView?.parent as? ViewGroup)?.removeView(adView)
        return adView
    }

    internal fun updateLoadOptions(
        loadAdRequest: () -> BannerAdRequest,
        adListener: AdMobAdListener?,
    ) {
        this.loadAdRequest = loadAdRequest
        this.adListener = adListener
    }

    private fun createAdEventCallback(): BannerAdEventCallback {
        return object : BannerAdEventCallback {
            override fun onAdClicked() {
                notifyListener { it.onAdClicked() }
            }

            override fun onAdDismissedFullScreenContent() {
                notifyListener { it.onAdDismissedFullScreenContent() }
            }

            override fun onAdFailedToShowFullScreenContent(
                fullScreenContentError: FullScreenContentError,
            ) {
                notifyListener {
                    it.onAdFailedToShowFullScreenContent(fullScreenContentError)
                }
            }

            override fun onAdImpression() {
                notifyListener { it.onAdImpression() }
            }

            override fun onAdPaid(value: AdValue) {
                notifyListener { it.onAdPaid(value) }
            }

            override fun onAdShowedFullScreenContent() {
                notifyListener { it.onAdShowedFullScreenContent() }
            }

            override fun onAppEvent(name: String, data: String?) {
                notifyListener { it.onAppEvent(name, data) }
            }
        }
    }

    private fun createAdRefreshCallback(): BannerAdRefreshCallback {
        return object : BannerAdRefreshCallback {
            override fun onAdRefreshed() {
                notifyListener { it.onAdRefreshed() }
            }

            override fun onAdFailedToRefresh(adError: LoadAdError) {
                notifyListener { it.onAdFailedToRefresh(adError) }
            }
        }
    }

    private fun notifyListener(callback: (AdMobAdListener) -> Unit) {
        runOnMainThread {
            if (!isDestroyed) {
                adListener?.let(callback)
            }
        }
    }

    internal fun destroy() {
        runOnMainThread {
            if (isDestroyed) return@runOnMainThread
            isDestroyed = true
            isLoading = false
            isLoaded = false
            (adView?.parent as? ViewGroup)?.removeView(adView)
            adView?.destroy()
        }
    }
}


/**
 * Creates and remembers an [AdMobBannerState].
 *
 * Hoist this above lazy list items if the banner is displayed inside a `LazyColumn`/`LazyRow`.
 *
 * @param adUnitId The ad unit ID for this banner ad.
 * @param adSize The size of the banner ad.
 * @param loadAdRequest A lambda that returns a [BannerAdRequest].
 * @param adListener An [AdMobAdListener] for observing ad events.
 * @since 1.0.0
 */
@JetAdMobAlpha
@Composable
fun rememberAdMobBannerState(
    adUnitId: String,
    adSize: AdSize,
    loadAdRequest: () -> BannerAdRequest = {
        BannerAdRequest.Builder(adUnitId, adSize).build()
    },
    adListener: AdMobAdListener? = null,
): AdMobBannerState {
    val context = LocalContext.current
    val isInspection = LocalInspectionMode.current

    val state = remember(context, adUnitId, adSize, isInspection) {
        AdMobBannerState(
            adUnitId = adUnitId,
            adSize = adSize,
            loadAdRequest = loadAdRequest,
            adListener = adListener,
            adView = if (isInspection) {
                null
            } else {
                AdView(context).apply {
                    id = View.generateViewId()
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
 * More info about Banner ads: [Banner ad overview](https://developers.google.com/admob/android/next-gen/banner)
 * [AdSize docs](https://developers.google.com/admob/android/next-gen/reference/kotlin/com/google/android/libraries/ads/mobile/sdk/banner/AdSize)
 *
 * @param modifier The modifier to be applied to the ad.
 * @param adUnitId The ad unit ID for this banner ad. See [Test ads](https://developers.google.com/admob/android/test-ads) for test ad unit IDs.
 * @param adSize The size of the banner ad. See [AdSize](https://developers.google.com/admob/android/next-gen/reference/kotlin/com/google/android/libraries/ads/mobile/sdk/banner/AdSize) for more information.
 * @param loadAdRequest A lambda that returns a [BannerAdRequest].
 * @param adListener An [AdMobAdListener] for observing ad events.
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
    loadAdRequest: () -> BannerAdRequest = {
        BannerAdRequest.Builder(adUnitId, adSize).build()
    },
    adListener: AdMobAdListener? = null,
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
    adSize: AdSize,
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
