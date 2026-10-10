@file:Suppress("OPT_IN_USAGE")

package com.jet.admob

import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.OneShotPreDrawListener
import androidx.core.view.doOnPreDraw
import com.google.android.gms.ads.nativead.AdChoicesView
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.jet.admob.annotations.JetAdMobAlpha
import kotlin.math.roundToInt

/**
 * Scope used to build a custom native ad layout.
 *
 * Native ad assets must be emitted through the corresponding scope function so the underlying
 * Android [View] can be registered with Google Mobile Ads. Do not install `Modifier.clickable`, a
 * `Button`, or any other click/gesture handler on an asset, the ad root, or content layered over
 * the ad. Google Mobile Ads must remain the sole owner of ad interaction handling.
 */
@JetAdMobAlpha
interface NativeAdLayoutScope {

    /**
     * Displays and registers the headline when it is available.
     *
     * If constrained, the headline must not be truncated before 25 characters. AdMob applies
     * language-specific limits for some Asian languages.
     */
    @Composable
    fun Headline(
        modifier: Modifier = Modifier,
        content: @Composable (String) -> Unit,
    )

    /**
     * Displays and registers the body when it is available.
     *
     * If constrained, the body must not be truncated before 90 characters. AdMob applies
     * language-specific limits for some Asian languages.
     */
    @Composable
    fun Body(
        modifier: Modifier = Modifier,
        content: @Composable (String) -> Unit,
    )

    /**
     * Displays and registers the call-to-action when it is available.
     *
     * The call-to-action must not be truncated before 15 characters and must remain clearly
     * legible. AdMob applies language-specific limits for some Asian languages.
     * The supplied content must not use `Button`, `Modifier.clickable`, or another click handler.
     * Use [NativeAdActionButton] for a button-shaped, non-clickable default.
     */
    @Composable
    fun CallToAction(
        modifier: Modifier = Modifier,
        content: @Composable (String) -> Unit,
    )

    /** Displays and registers the icon when it is available. */
    @Composable
    fun Icon(
        modifier: Modifier = Modifier,
        content: @Composable (Drawable) -> Unit,
    )

    /**
     * Displays and registers the SDK-owned media view when media is available.
     *
     * When the ad contains video, [modifier] must give the media view a size of at least
     * 120 x 120 dp. A null [scaleType] uses [ImageView.ScaleType.CENTER_INSIDE].
     */
    @Composable
    fun Media(
        modifier: Modifier = Modifier,
        scaleType: ImageView.ScaleType? = null,
    )

    /** Displays and registers the advertiser when it is available. */
    @Composable
    fun Advertiser(
        modifier: Modifier = Modifier,
        content: @Composable (String) -> Unit,
    )

    /** Displays and registers the star rating when it is available. */
    @Composable
    fun StarRating(
        modifier: Modifier = Modifier,
        content: @Composable (Double) -> Unit,
    )

    /** Displays and registers the price when it is available. */
    @Composable
    fun Price(
        modifier: Modifier = Modifier,
        content: @Composable (String) -> Unit,
    )

    /** Displays and registers the store when it is available. */
    @Composable
    fun Store(
        modifier: Modifier = Modifier,
        content: @Composable (String) -> Unit,
    )

    /**
     * Displays and registers a custom AdChoices view.
     *
     * This is optional, and access to custom AdChoices views is limited by Google. Most layouts
     * should omit this slot and leave the top-right corner clear for the SDK-inserted overlay. If
     * the account is not enabled for custom AdChoices views, the SDK can ignore this view and add
     * its own overlay instead.
     */
    @Composable
    fun AdChoices(modifier: Modifier = Modifier)

    /**
     * Displays the required publisher-rendered ad attribution.
     *
     * Pass a localized equivalent of "Ad", "Advertisement", or "Sponsored" when the default
     * English label is not appropriate.
     */
    @Composable
    fun AdAttribution(
        modifier: Modifier = Modifier,
        text: String = "Ad",
        shape: Shape = ButtonDefaults.shape,
        containerColor: Color = ButtonDefaults.buttonColors().containerColor,
        contentColor: Color = ButtonDefaults.buttonColors().contentColor,
        contentPadding: PaddingValues = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
    )
}

/**
 * Displays a loaded native ad with a developer-defined Compose layout.
 *
 * The implementation keeps [NativeAdView] as the root of the complete ad asset subtree. Each
 * asset emitted by [NativeAdLayoutScope] is backed by a distinct descendant Android [View] and is
 * registered before the native ad is associated with the root view.
 *
 * [state] remains the owner of the loaded [NativeAd]. Removing this composable releases only its
 * view hierarchy, which allows a hoisted state to survive temporary lazy-list disposal.
 *
 * The resulting native ad must be at least 32 x 32 dp. The [NativeAdLayoutScope.AdAttribution]
 * helper defaults to at least 15 x 15 dp, safely exceeding the policy minimum of 15 x 15 pixels.
 * Video media must be at least 120 x 120 dp. Unless using an enabled custom AdChoices view, the
 * caller must also leave the top-right corner visible for the SDK-inserted overlay.
 *
 * The layout must emit exactly one attribution and must emit Headline, CallToAction, Icon, and
 * Media whenever the loaded response provides the corresponding required asset. Missing or
 * duplicate required slots fail fast before the ad is bound.
 *
 * Do not apply a clickable or gesture-handling modifier to [modifier], and do not add click
 * handlers anywhere inside [content]. Use [NativeAdActionButton] for a CTA with button styling.
 */
@JetAdMobAlpha
@Composable
fun AdMobNativeLayout(
    state: AdMobNativeAdState,
    modifier: Modifier = Modifier,
    content: @Composable NativeAdLayoutScope.() -> Unit,
) {
    val nativeAd = state.nativeAd ?: return
    val parentComposition = rememberCompositionContext()
    val currentNativeAd = rememberUpdatedState(nativeAd)
    val currentContent = rememberUpdatedState(content)
    val registries = remember { mutableMapOf<NativeAdView, NativeAdAssetRegistry>() }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            val nativeAdView = NativeAdView(context)
            val registry = NativeAdAssetRegistry(nativeAdView)
            registries[nativeAdView] = registry
            val composeView = ComposeView(context).apply {
                id = View.generateViewId()
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                setParentCompositionContext(parentComposition)
            }

            nativeAdView.addView(composeView)
            composeView.setContent {
                val ad = currentNativeAd.value
                val scope = remember(ad) { NativeAdLayoutScopeImpl(ad) }

                CompositionLocalProvider(LocalNativeAdAssetRegistry provides registry) {
                    currentContent.value.invoke(scope)
                }

                // Nested AndroidViews can attach after Compose dispatches this SideEffect. Waiting
                // until pre-draw guarantees that all emitted asset views have been registered.
                SideEffect {
                    registry.requestCommit(ad)
                }
            }

            nativeAdView
        },
        onRelease = { nativeAdView ->
            registries.remove(nativeAdView)?.release()
            (nativeAdView.getChildAt(0) as? ComposeView)?.disposeComposition()
            nativeAdView.clearRegisteredAssetViews()
            nativeAdView.removeAllViews()
            // This releases SDK resources held by the view only. AdMobNativeAdState remains the
            // owner of NativeAd and destroys it when the hoisted state itself leaves composition.
            nativeAdView.destroy()
        },
    )
}

/**
 * A button-shaped call-to-action label that deliberately installs no click handler.
 *
 * Google Mobile Ads handles clicks on the Android view registered by
 * [NativeAdLayoutScope.CallToAction]. A regular Compose `Button` would consume those events.
 */
@JetAdMobAlpha
@Composable
fun NativeAdActionButton(
    text: String,
    modifier: Modifier = Modifier,
    shape: Shape = ButtonDefaults.shape,
    containerColor: Color = ButtonDefaults.buttonColors().containerColor,
    contentColor: Color = ButtonDefaults.buttonColors().contentColor,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
) {
    Box(
        modifier = modifier
            .background(color = containerColor, shape = shape)
            .padding(contentPadding),
    ) {
        Text(
            text = text,
            color = contentColor,
        )
    }
}

private val LocalNativeAdAssetRegistry = staticCompositionLocalOf<NativeAdAssetRegistry> {
    error("Native ad assets must be placed inside AdMobNativeLayout")
}

private enum class NativeAdAsset {
    Headline,
    Body,
    CallToAction,
    Icon,
    Media,
    Advertiser,
    StarRating,
    Price,
    Store,
    AdChoices,
}

private class NativeAdLayoutScopeImpl(
    private val nativeAd: NativeAd,
) : NativeAdLayoutScope {

    @Composable
    override fun Headline(
        modifier: Modifier,
        content: @Composable (String) -> Unit,
    ) {
        val value = nativeAd.headline ?: return
        NativeAdComposeAsset(
            asset = NativeAdAsset.Headline,
            modifier = modifier,
        ) {
            content(value)
        }
    }

    @Composable
    override fun Body(
        modifier: Modifier,
        content: @Composable (String) -> Unit,
    ) {
        val value = nativeAd.body ?: return
        NativeAdComposeAsset(
            asset = NativeAdAsset.Body,
            modifier = modifier,
        ) {
            content(value)
        }
    }

    @Composable
    override fun CallToAction(
        modifier: Modifier,
        content: @Composable (String) -> Unit,
    ) {
        val value = nativeAd.callToAction ?: return
        NativeAdComposeAsset(
            asset = NativeAdAsset.CallToAction,
            modifier = modifier,
        ) {
            content(value)
        }
    }

    @Composable
    override fun Icon(
        modifier: Modifier,
        content: @Composable (Drawable) -> Unit,
    ) {
        val value = nativeAd.icon?.drawable ?: return
        NativeAdComposeAsset(
            asset = NativeAdAsset.Icon,
            modifier = modifier,
        ) {
            content(value)
        }
    }

    @Composable
    override fun Media(
        modifier: Modifier,
        scaleType: ImageView.ScaleType?,
    ) {
        val mediaContent = nativeAd.mediaContent ?: return
        val registry = LocalNativeAdAssetRegistry.current

        AndroidView(
            modifier = modifier,
            factory = { context ->
                MediaView(context).apply {
                    id = View.generateViewId()
                }
            },
            update = { mediaView ->
                mediaView.mediaContent = mediaContent
                mediaView.setImageScaleType(scaleType ?: ImageView.ScaleType.CENTER_INSIDE)
                registry.register(NativeAdAsset.Media, mediaView)
            },
            onRelease = { mediaView ->
                registry.unregister(NativeAdAsset.Media, mediaView)
                mediaView.mediaContent = null
            },
        )
    }

    @Composable
    override fun Advertiser(
        modifier: Modifier,
        content: @Composable (String) -> Unit,
    ) {
        val value = nativeAd.advertiser ?: return
        NativeAdComposeAsset(
            asset = NativeAdAsset.Advertiser,
            modifier = modifier,
        ) {
            content(value)
        }
    }

    @Composable
    override fun StarRating(
        modifier: Modifier,
        content: @Composable (Double) -> Unit,
    ) {
        val value = nativeAd.starRating ?: return
        NativeAdComposeAsset(
            asset = NativeAdAsset.StarRating,
            modifier = modifier,
        ) {
            content(value)
        }
    }

    @Composable
    override fun Price(
        modifier: Modifier,
        content: @Composable (String) -> Unit,
    ) {
        val value = nativeAd.price ?: return
        NativeAdComposeAsset(
            asset = NativeAdAsset.Price,
            modifier = modifier,
        ) {
            content(value)
        }
    }

    @Composable
    override fun Store(
        modifier: Modifier,
        content: @Composable (String) -> Unit,
    ) {
        val value = nativeAd.store ?: return
        NativeAdComposeAsset(
            asset = NativeAdAsset.Store,
            modifier = modifier,
        ) {
            content(value)
        }
    }

    @Composable
    override fun AdChoices(modifier: Modifier) {
        val registry = LocalNativeAdAssetRegistry.current

        AndroidView(
            modifier = modifier,
            factory = { context ->
                val minimumSize = (15 * context.resources.displayMetrics.density).roundToInt()
                AdChoicesView(context).apply {
                    id = View.generateViewId()
                    minimumWidth = minimumSize
                    minimumHeight = minimumSize
                }
            },
            update = { adChoicesView ->
                registry.register(NativeAdAsset.AdChoices, adChoicesView)
            },
            onRelease = { adChoicesView ->
                registry.unregister(NativeAdAsset.AdChoices, adChoicesView)
            },
        )
    }

    @Composable
    override fun AdAttribution(
        modifier: Modifier,
        text: String,
        shape: Shape,
        containerColor: Color,
        contentColor: Color,
        contentPadding: PaddingValues,
    ) {
        require(text.isNotBlank()) { "Ad attribution text must not be blank" }
        val registry = LocalNativeAdAssetRegistry.current
        val registration = remember { Any() }

        DisposableEffect(registry, registration) {
            registry.registerAttribution(registration)
            onDispose {
                registry.unregisterAttribution(registration)
            }
        }

        Box(
            modifier = modifier
                .defaultMinSize(minWidth = 15.dp, minHeight = 15.dp)
                .background(color = containerColor, shape = shape)
                .padding(contentPadding),
        ) {
            Text(
                text = text,
                color = contentColor,
            )
        }
    }
}

@Composable
private fun NativeAdComposeAsset(
    asset: NativeAdAsset,
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    val registry = LocalNativeAdAssetRegistry.current
    val parentComposition = rememberCompositionContext()
    val currentContent = rememberUpdatedState(content)

    AndroidView(
        modifier = modifier,
        factory = { context ->
            ComposeView(context).apply {
                id = View.generateViewId()
                setParentCompositionContext(parentComposition)
                setContent {
                    currentContent.value.invoke()
                }
            }
        },
        update = { composeView ->
            registry.register(asset, composeView)
        },
        onRelease = { composeView ->
            registry.unregister(asset, composeView)
            composeView.disposeComposition()
        },
    )
}

private class NativeAdAssetRegistry(
    private val nativeAdView: NativeAdView,
) {
    private val assetViews = mutableMapOf<NativeAdAsset, View>()
    private val attributionRegistrations = mutableSetOf<Any>()
    private var generation: Int = 0
    private var committedGeneration: Int = -1
    private var committedAd: NativeAd? = null
    private var pendingAd: NativeAd? = null
    private var pendingCommit: OneShotPreDrawListener? = null
    private var isReleased: Boolean = false

    fun requestCommit(nativeAd: NativeAd) {
        if (isReleased) return

        pendingAd = nativeAd
        if (pendingCommit != null) return

        pendingCommit = nativeAdView.doOnPreDraw {
            pendingCommit = null
            pendingAd?.let(::commit)
        }
    }

    fun release() {
        isReleased = true
        pendingAd = null
        pendingCommit?.removeListener()
        pendingCommit = null
    }

    fun register(asset: NativeAdAsset, view: View) {
        val previous = assetViews[asset]
        check(previous == null || previous === view) {
            "Only one ${asset.name} may be registered in an AdMobNativeLayout"
        }
        if (previous !== view) {
            assetViews[asset] = view
            generation++
        }
    }

    fun unregister(asset: NativeAdAsset, view: View) {
        if (assetViews[asset] === view) {
            assetViews.remove(asset)
            generation++
        }
    }

    fun registerAttribution(registration: Any) {
        if (attributionRegistrations.add(registration)) {
            generation++
        }
    }

    fun unregisterAttribution(registration: Any) {
        if (attributionRegistrations.remove(registration)) {
            generation++
        }
    }

    private fun commit(nativeAd: NativeAd) {
        if (committedAd === nativeAd && committedGeneration == generation) return

        check(attributionRegistrations.size == 1) {
            "AdMobNativeLayout requires exactly one AdAttribution; " +
                "found ${attributionRegistrations.size}"
        }
        if (nativeAd.headline != null) {
            requireAsset(NativeAdAsset.Headline)
        }
        if (nativeAd.callToAction != null) {
            requireAsset(NativeAdAsset.CallToAction)
        }
        if (nativeAd.icon?.drawable != null) {
            requireAsset(NativeAdAsset.Icon)
        }
        if (nativeAd.mediaContent?.hasVideoContent() == true) {
            requireAsset(NativeAdAsset.Media)
        }

        nativeAdView.headlineView = assetViews[NativeAdAsset.Headline]
        nativeAdView.bodyView = assetViews[NativeAdAsset.Body]
        nativeAdView.callToActionView = assetViews[NativeAdAsset.CallToAction]
        nativeAdView.iconView = assetViews[NativeAdAsset.Icon]
        nativeAdView.mediaView = assetViews[NativeAdAsset.Media] as? MediaView
        nativeAdView.advertiserView = assetViews[NativeAdAsset.Advertiser]
        nativeAdView.starRatingView = assetViews[NativeAdAsset.StarRating]
        nativeAdView.priceView = assetViews[NativeAdAsset.Price]
        nativeAdView.storeView = assetViews[NativeAdAsset.Store]
        nativeAdView.adChoicesView = assetViews[NativeAdAsset.AdChoices] as? AdChoicesView

        // Must be the final SDK binding operation, after every asset view has been registered.
        nativeAdView.setNativeAd(nativeAd)
        committedAd = nativeAd
        committedGeneration = generation
    }

    private fun requireAsset(asset: NativeAdAsset) {
        check(assetViews[asset] != null) {
            "AdMobNativeLayout requires the ${asset.name} asset"
        }
    }
}

private fun NativeAdView.clearRegisteredAssetViews() {
    headlineView = null
    bodyView = null
    callToActionView = null
    iconView = null
    mediaView = null
    advertiserView = null
    starRatingView = null
    priceView = null
    storeView = null
    adChoicesView = null
}
