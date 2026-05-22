@file:Suppress("OPT_IN_USAGE")

package com.jet.admob

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
import com.google.android.gms.ads.AdSize
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
    val adSize: AdSize,
    private val adView: AdView?,
    private var loadAdRequest: () -> AdRequest,
    private var adListener: AdListener?,
) {

    private var isLoading: Boolean = false
    private var isLoaded: Boolean = false
    private var isDestroyed: Boolean = false

    internal val isLoadedForInspection: Boolean
        get() = adView == null

    /**
     * Loads the banner ad if it is not already loaded or loading.
     *
     * @param loadAdRequest A lambda that returns an [AdRequest].
     * @param adListener An [AdListener] for observing ad events.
     * @since 1.0.0
     */
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
 * @param adSize The size of the banner ad.
 * @param loadAdRequest A lambda that returns an [AdRequest].
 * @param adListener An [AdListener] for observing ad events.
 * @since 1.0.0
 */
@JetAdMobAlpha
@Composable
fun rememberAdMobBannerState(
    adUnitId: String,
    adSize: AdSize,
    loadAdRequest: () -> AdRequest = { AdRequest.Builder().build() },
    adListener: AdListener? = null,
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
                    this.adUnitId = adUnitId
                    setAdSize(adSize)
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
 * [AdSize docs](https://developers.google.com/admob/android/reference/com/google/android/gms/ads/AdSize)
 *
 * @param modifier The modifier to be applied to the ad.
 * @param adUnitId The ad unit ID for this banner ad. See [Test ads](https://developers.google.com/admob/android/test-ads) for test ad unit IDs.
 * @param adSize The size of the banner ad. See [AdSize](https://developers.google.com/android/reference/com/google/android/gms/ads/AdSize) for more information.
 * @param loadAdRequest A lambda that returns an [AdRequest]. Defaults to a new [AdRequest.Builder().build()].
 * @param adListener An [AdListener] for observing ad events.
 * @param preOccupySpace If true, the composable will pre-occupy the space of the ad size before the ad is loaded. This can prevent layout shifts.
 *
 * @author Miroslav Hýbler <br>
 * created on 06.01.2026
 * @since 1.0.0
 */
@Deprecated(
    message = "This overload will become private soon. Use rememberAdMobBannerState() and AdMobBanner(state = ...) instead.",
    replaceWith = ReplaceWith(
        expression = "AdMobBanner(modifier = modifier, state = rememberAdMobBannerState(adUnitId = adUnitId, adSize = adSize, loadAdRequest = loadAdRequest, adListener = adListener), preOccupySpace = preOccupySpace)",
        imports = [
            "com.jet.admob.AdMobBanner",
            "com.jet.admob.rememberAdMobBannerState",
        ],
    ),
)
@JetAdMobAlpha
@Composable
fun AdMobBanner(
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
