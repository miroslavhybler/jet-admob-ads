@file:Suppress("OPT_IN_USAGE")

package com.jet.admob

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.material.button.MaterialButton
import com.jet.admob.annotations.JetAdMobAlpha
import kotlin.random.Random

/**
 * Defines the available formats for the [AdMobNative] composable.
 * Each format corresponds to a specific XML layout template.
 *
 * @author Miroslav Hýbler
 * @since 1.0.0
 */
sealed class NativeAdFormat {
    /** A small, banner-like native ad format. */
    object Small : NativeAdFormat()

    /** A medium-sized native ad format that includes a [MediaView]. */
    object Medium : NativeAdFormat()

    /** A full-screen native ad format for a more immersive experience. */
    object FullScreen : NativeAdFormat()
}

/**
 * A data class for customizing the colors of the [AdMobNative] ad.
 *
 * @property containerColor The background color of the ad container.
 * @property contentColor The color of the ad's headline and body text.
 * @property buttonColor The background color of the call-to-action button.
 * @property buttonTextColor The text color of the call-to-action button.
 * @author Miroslav Hýbler
 * @since 1.0.0
 */
data class NativeAdColors(
    val containerColor: Color,
    val contentColor: Color,
    val buttonColor: Color,
    val buttonTextColor: Color,
)


/**
 * State holder for a native ad.
 *
 * Hoist this state above lazy list items to keep the loaded [NativeAd] and inflated
 * [NativeAdView] alive while the list item is temporarily disposed.
 *
 * @since 1.0.0
 */
@JetAdMobAlpha
class AdMobNativeAdState internal constructor(
    private val context: Context?,
    val adUnitId: String,
    private var loadAdRequest: () -> AdRequest,
    private var adListener: AdListener?,
) {

    internal var nativeAd: NativeAd? by mutableStateOf(value = null)
        private set

    private var isLoading: Boolean = false
    private var isDestroyed: Boolean = false
    private val nativeAdViews = mutableMapOf<NativeAdFormat, NativeAdView>()

    internal val isLoadedForInspection: Boolean
        get() = context == null

    /**
     * Loads the native ad if it is not already loaded or loading.
     *
     * @param loadAdRequest A lambda that returns an [AdRequest].
     * @param adListener An [AdListener] for observing ad events.
     * @since 1.0.0
     */
    fun loadAd(
        loadAdRequest: () -> AdRequest = this.loadAdRequest,
        adListener: AdListener? = this.adListener,
    ) {
        val context = context ?: return
        if (isLoading || nativeAd != null || isDestroyed) return

        isLoading = true
        loadNativeAd(
            context = context,
            adUnitId = adUnitId,
            loadAdRequest = loadAdRequest,
            adListener = adListener,
        ) { ad ->
            isLoading = false
            if (isDestroyed) {
                ad?.destroy()
                return@loadNativeAd
            }

            val previousAd = nativeAd
            nativeAd = ad
            previousAd?.destroy()
        }
    }

    internal fun updateLoadOptions(
        loadAdRequest: () -> AdRequest,
        adListener: AdListener?,
    ) {
        this.loadAdRequest = loadAdRequest
        this.adListener = adListener
    }

    internal fun getNativeAdView(adFormat: NativeAdFormat): NativeAdView? {
        val context = context ?: return null
        val adView = nativeAdViews.getOrPut(adFormat) {
            LayoutInflater.from(context).inflate(nativeAdLayout(adFormat), null) as NativeAdView
        }
        (adView.parent as? ViewGroup)?.removeView(adView)
        return adView
    }

    internal fun destroy() {
        isDestroyed = true
        nativeAd?.destroy()
        nativeAd = null
        nativeAdViews.clear()
    }
}


/**
 * Creates and remembers an [AdMobNativeAdState].
 *
 * Hoist this above lazy list items if the native ad is displayed inside a `LazyColumn`/`LazyRow`.
 *
 * @param adUnitId The ad unit ID for this native ad.
 * @param loadAdRequest A lambda that returns an [AdRequest].
 * @param adListener An [AdListener] for observing ad events.
 * @since 1.0.0
 */
@JetAdMobAlpha
@Composable
fun rememberAdMobNativeAdState(
    adUnitId: String,
    loadAdRequest: () -> AdRequest = { AdRequest.Builder().build() },
    adListener: AdListener? = null,
): AdMobNativeAdState {
    val context = LocalContext.current
    val isInspection = LocalInspectionMode.current

    val state = remember(context, adUnitId, isInspection) {
        AdMobNativeAdState(
            context = if (isInspection) null else context,
            adUnitId = adUnitId,
            loadAdRequest = loadAdRequest,
            adListener = adListener,
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


// Loads a native ad from AdMob.
private fun loadNativeAd(
    context: Context,
    adUnitId: String,
    loadAdRequest: () -> AdRequest,
    adListener: AdListener?,
    callback: (NativeAd?) -> Unit,
) {
    val builder = AdLoader.Builder(context, adUnitId)
        .forNativeAd { nativeAd ->
            callback(nativeAd)
        }

    val adLoader = builder
        .withAdListener(object : AdListener() {
            override fun onAdClicked() {
                adListener?.onAdClicked()
            }

            override fun onAdClosed() {
                adListener?.onAdClosed()
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e("NATIVE_AD", "Failed to load native ad: ${adError.message}")
                adListener?.onAdFailedToLoad(adError)
                callback(null)
            }

            override fun onAdImpression() {
                adListener?.onAdImpression()
            }

            override fun onAdLoaded() {
                adListener?.onAdLoaded()
            }

            override fun onAdOpened() {
                adListener?.onAdOpened()
            }

            override fun onAdSwipeGestureClicked() {
                adListener?.onAdSwipeGestureClicked()
            }
        })
        .withNativeAdOptions(
            NativeAdOptions.Builder()
                .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                .build()
        )
        .build()

    adLoader.loadAd(loadAdRequest())
}


private fun nativeAdLayout(adFormat: NativeAdFormat): Int {
    return when (adFormat) {
        is NativeAdFormat.Small -> R.layout.view_ad_small
        is NativeAdFormat.Medium -> R.layout.view_ad_medium
        is NativeAdFormat.FullScreen -> R.layout.view_ad_fullscreen
    }
}

/**
 * Provides default values for the [AdMobNative] composable.
 * @author Miroslav Hýbler
 * @since 1.0.0
 */
object NativeAdDefaults {

    /**
     * Creates a [NativeAdColors] instance with default colors from the current [MaterialTheme].
     */
    @Composable
    fun colors(
        containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        buttonColor: Color = MaterialTheme.colorScheme.primary,
        buttonTextColor: Color = MaterialTheme.colorScheme.onPrimary,
    ): NativeAdColors {
        return NativeAdColors(
            containerColor = containerColor,
            contentColor = contentColor,
            buttonColor = buttonColor,
            buttonTextColor = buttonTextColor
        )
    }

    /**
     * Provides a default shape based on the ad format.
     */
    @Composable
    fun shape(
        format: NativeAdFormat,
    ): Shape {
        return when (format) {
            is NativeAdFormat.Small -> RectangleShape
            is NativeAdFormat.Medium -> MaterialTheme.shapes.large
            is NativeAdFormat.FullScreen -> RectangleShape
        }
    }

    /**
     * Provides a default content padding based on the ad format.
     * For [NativeAdFormat.FullScreen], it uses the safe drawing insets to avoid system UI.
     * @since 1.0.0
     */
    @Composable
    fun contentPadding(format: NativeAdFormat): PaddingValues {
        return when (format) {
            is NativeAdFormat.Small -> PaddingValues(all = 8.dp)
            is NativeAdFormat.Medium -> PaddingValues(all = 12.dp)
            is NativeAdFormat.FullScreen -> WindowInsets.safeDrawing.asPaddingValues()
        }
    }
}


/**
 * A composable that displays a native ad from AdMob, offering a highly customizable experience that
 * can be styled to match the look and feel of your app.
 *
 * This composable uses an XML-based approach for laying out the ad assets. This is the recommended
 * method to ensure full compliance with AdMob policies, as it allows the SDK to correctly register
 * and track each individual view (like the headline, icon, and call-to-action button). For more
 * details on this requirement, see the [official AdMob documentation](https://developers.google.com/admob/android/native/advanced).
 *
 * @param modifier The modifier to be applied to the ad container.
 * @param adUnitId The ad unit ID for this native ad. See [Test ads](https://developers.google.com/admob/android/test-ads) for test ad unit IDs.
 * @param loadAdRequest A lambda that returns an [AdRequest]. Defaults to a new [AdRequest.Builder().build()].
 * @param adListener An [AdListener] for observing ad events.
 * @param adFormat The format of the native ad, which determines the underlying XML layout to be inflated. See [NativeAdFormat].
 * @param shape The shape of the ad container. Defaults to [RectangleShape].
 * @param buttonShape The shape to be applied to the call-to-action button. Defaults to [RectangleShape].
 * @param colors An instance of [NativeAdColors] to customize the colors of the ad container, content, and button.
 *               If null, the default colors from the XML template and app theme will be used.
 * @param contentPadding The padding to be applied to the content of the ad, inside the container.
 * @since 1.0.0
 */
@Deprecated(
    message = "This overload will become private soon. Use rememberAdMobNativeAdState() and AdMobNative(state = ...) instead.",
    replaceWith = ReplaceWith(
        expression = "AdMobNative(modifier = modifier, state = rememberAdMobNativeAdState(adUnitId = adUnitId, loadAdRequest = loadAdRequest, adListener = adListener), adFormat = adFormat, shape = shape, buttonShape = buttonShape, colors = colors, contentPadding = contentPadding)",
        imports = [
            "com.jet.admob.AdMobNative",
            "com.jet.admob.rememberAdMobNativeAdState",
        ],
    ),
)
@JetAdMobAlpha
@Composable
fun AdMobNative(
    modifier: Modifier = Modifier,
    adUnitId: String,
    loadAdRequest: () -> AdRequest = { AdRequest.Builder().build() },
    adListener: AdListener? = null,
    adFormat: NativeAdFormat = NativeAdFormat.Small,
    shape: Shape = NativeAdDefaults.shape(format = adFormat),
    buttonShape: Shape = ButtonDefaults.shape,
    colors: NativeAdColors = NativeAdDefaults.colors(),
    contentPadding: PaddingValues = NativeAdDefaults.contentPadding(format = adFormat),
) {
    val state = rememberAdMobNativeAdState(
        adUnitId = adUnitId,
        loadAdRequest = loadAdRequest,
        adListener = adListener,
    )

    AdMobNative(
        modifier = modifier,
        state = state,
        adFormat = adFormat,
        contentPadding = contentPadding,
        shape = shape,
        buttonShape = buttonShape,
        colors = colors,
    )
}


/**
 * A composable that displays a native ad from AdMob using a hoisted [AdMobNativeAdState].
 *
 * Hoist [state] above lazy list items to avoid reloading the ad whenever the item is disposed and
 * later composed again.
 *
 * @param modifier The modifier to be applied to the ad container.
 * @param state The remembered state that owns the loaded [NativeAd].
 * @param adFormat The format of the native ad, which determines the underlying XML layout to be inflated. See [NativeAdFormat].
 * @param shape The shape of the ad container. Defaults to [RectangleShape].
 * @param buttonShape The shape to be applied to the call-to-action button. Defaults to [RectangleShape].
 * @param colors An instance of [NativeAdColors] to customize the colors of the ad container, content, and button.
 * @param contentPadding The padding to be applied to the content of the ad, inside the container.
 * @since 1.0.0
 */
@JetAdMobAlpha
@Composable
fun AdMobNative(
    modifier: Modifier = Modifier,
    state: AdMobNativeAdState,
    adFormat: NativeAdFormat = NativeAdFormat.Small,
    shape: Shape = NativeAdDefaults.shape(format = adFormat),
    buttonShape: Shape = ButtonDefaults.shape,
    colors: NativeAdColors = NativeAdDefaults.colors(),
    contentPadding: PaddingValues = NativeAdDefaults.contentPadding(format = adFormat),
) {
    val isInspection = LocalInspectionMode.current

    if (isInspection || state.isLoadedForInspection) {
        AdMobNativePreview(
            modifier = modifier,
            adFormat = adFormat,
            contentPadding = contentPadding,
            shape = shape,
            buttonShape = buttonShape,
            colors = colors,
        )
    } else {
        AdMobNativeImpl(
            modifier = modifier,
            state = state,
            adFormat = adFormat,
            contentPadding = contentPadding,
            shape = shape,
            buttonShape = buttonShape,
            colors = colors,
        )
    }
}


@Composable
private fun AdMobNativeImpl(
    modifier: Modifier,
    state: AdMobNativeAdState,
    adFormat: NativeAdFormat,
    contentPadding: PaddingValues,
    shape: Shape,
    buttonShape: Shape,
    colors: NativeAdColors,
) {
    val density = LocalDensity.current

    val adModifier = when (adFormat) {
        is NativeAdFormat.FullScreen -> modifier.fillMaxSize()
        else -> modifier.fillMaxWidth()
    }

    state.nativeAd?.let { nativeAd ->
        key(adFormat) {
            AndroidView(
                modifier = adModifier,
                factory = {
                    state.getNativeAdView(adFormat = adFormat)
                        ?: LayoutInflater.from(it).inflate(nativeAdLayout(adFormat), null) as NativeAdView
                },
                update = { adView ->
                    //Apply content padding to the view so padding/margin will behave as expected for Compose
                    adView.getChildAt(0)?.let { contentView ->
                        applyPadding(
                            view = contentView,
                            contentPadding = contentPadding,
                            density = density,
                        )
                    }

                    val headlineView = adView.findViewById<TextView>(R.id.ad_headline)
                    val bodyView = adView.findViewById<TextView>(R.id.ad_body)
                    val callToActionView = adView.findViewById<MaterialButton>(R.id.ad_call_to_action)
                    val iconView = adView.findViewById<ImageView>(R.id.ad_icon)
                    val mediaView = adView.findViewById<MediaView>(R.id.ad_media)
                    val advertiserView = adView.findViewById<TextView>(R.id.ad_advertiser)
                    val starRatingView = adView.findViewById<RatingBar>(R.id.ad_stars)

                    //Setting up background of adView
                    applyShapeAndColor(
                        view = adView,
                        shape = shape,
                        color = colors.containerColor,
                        density = density,
                    )

                    headlineView?.setTextColor(colors.contentColor.toArgb())
                    bodyView?.setTextColor(colors.contentColor.toArgb())
                    advertiserView?.setTextColor(colors.contentColor.toArgb())

                    callToActionView?.let {
                        //Setting up background of action button
                        applyShapeAndColor(
                            view = it,
                            shape = buttonShape,
                            color = colors.buttonColor,
                            density = density,
                        )
                        it.setTextColor(colors.buttonTextColor.toArgb())
                    }


                    // Populate the views with the ad content
                    headlineView.text = nativeAd.headline
                    bodyView.text = nativeAd.body
                    callToActionView.text = nativeAd.callToAction
                    nativeAd.icon?.drawable?.let {
                        iconView.setImageDrawable(it)
                        iconView.visibility = View.VISIBLE
                        adView.iconView = iconView
                    } ?: run {
                        iconView.visibility = View.GONE
                    }
                    mediaView?.mediaContent = nativeAd.mediaContent
                    advertiserView?.text = nativeAd.advertiser
                    starRatingView?.rating = nativeAd.starRating?.toFloat() ?: 0f

                    // Register the views
                    adView.headlineView = headlineView
                    adView.bodyView = bodyView
                    adView.callToActionView = callToActionView
                    adView.advertiserView = advertiserView
                    adView.starRatingView = starRatingView
                    adView.mediaView = mediaView

                    // Set the native ad to the ad view
                    adView.setNativeAd(nativeAd)
                }
            )
        }
    }
}


/**
 * A preview of the AdMobNative composable.
 */
@Composable
private fun AdMobNativePreview(
    modifier: Modifier,
    adFormat: NativeAdFormat,
    contentPadding: PaddingValues,
    shape: Shape,
    buttonShape: Shape,
    colors: NativeAdColors,
) {
    val density = LocalDensity.current

    AndroidView(
        modifier = modifier,
        factory = {
            LayoutInflater.from(it).inflate(nativeAdLayout(adFormat), null) as NativeAdView
        },
        update = { adView ->
            //Apply content padding to the view so padding/margin will behave as expected for Compose
            adView.getChildAt(0)?.let { contentView ->
                applyPadding(
                    view = contentView,
                    contentPadding = contentPadding,
                    density = density,
                )
            }

            val headlineView = adView.findViewById<TextView>(R.id.ad_headline)
            val bodyView = adView.findViewById<TextView>(R.id.ad_body)
            val callToActionView = adView.findViewById<MaterialButton>(R.id.ad_call_to_action)
            val iconView = adView.findViewById<ImageView>(R.id.ad_icon)
            val mediaView = adView.findViewById<MediaView>(R.id.ad_media)
            val advertiserView = adView.findViewById<TextView>(R.id.ad_advertiser)
            val starRatingView = adView.findViewById<RatingBar>(R.id.ad_stars)

            //Setting up background of adView
            applyShapeAndColor(
                view = adView,
                shape = shape,
                color = colors.containerColor,
                density = density,
            )

            headlineView?.setTextColor(colors.contentColor.toArgb())
            bodyView?.setTextColor(colors.contentColor.toArgb())
            advertiserView?.setTextColor(colors.contentColor.toArgb())

            callToActionView?.let {
                //Setting up background of action button
                applyShapeAndColor(
                    view = it,
                    shape = buttonShape,
                    color = colors.buttonColor,
                    density = density,
                )
                it.setTextColor(colors.buttonTextColor.toArgb())
            }


            // Populate the views with the ad content for the preview
            headlineView.text = "Headline"
            bodyView.text = "Body"
            callToActionView.text = "Install"

            iconView.setImageDrawable(
                AppCompatResources.getDrawable(
                    adView.context,
                    R.drawable.jet_admob_ad_icon
                )
            )
            adView.iconView = iconView
            //mediaView?.mediaContent = nativeAd.mediaContent
            advertiserView?.text = "Advertiser"
            starRatingView?.rating = Random.nextFloat()
                .coerceIn(minimumValue = 0f, maximumValue = 5f)

            // Register the views
            adView.headlineView = headlineView
            adView.bodyView = bodyView
            adView.callToActionView = callToActionView
            adView.advertiserView = advertiserView
            adView.starRatingView = starRatingView
            adView.mediaView = mediaView
        }
    )
}

@JetAdMobAlpha
@Preview(showBackground = true)
@Composable
private fun AdMobNativeSmallPreview() {
    val nativeAdState = rememberAdMobNativeAdState(
        adUnitId = AdMobAdsUtil.TestIds.NATIVE,
    )

    AdMobNative(
        state = nativeAdState,
        adFormat = NativeAdFormat.Small,
    )
}

@JetAdMobAlpha
@Preview(showBackground = true)
@Composable
private fun AdMobNativeMediumPreview() {
    val nativeAdState = rememberAdMobNativeAdState(
        adUnitId = AdMobAdsUtil.TestIds.NATIVE,
    )

    AdMobNative(
        state = nativeAdState,
        adFormat = NativeAdFormat.Medium,
    )
}

@JetAdMobAlpha
@Preview(showBackground = true)
@Composable
private fun AdMobNativeFullScreenPreview() {
    val nativeAdState = rememberAdMobNativeAdState(
        adUnitId = AdMobAdsUtil.TestIds.NATIVE_VIDEO,
    )

    AdMobNative(
        state = nativeAdState,
        adFormat = NativeAdFormat.FullScreen,
    )
}


private fun applyShapeAndColor(
    view: View,
    shape: Shape,
    color: Color,
    density: Density,
) {
    view.post {
        val outline = shape.createOutline(
            Size(width = view.width.toFloat(), height = view.height.toFloat()),
            layoutDirection = if (view.layoutDirection == View.LAYOUT_DIRECTION_RTL)
                LayoutDirection.Rtl
            else
                LayoutDirection.Ltr,
            density = density,
        )

        if (view is MaterialButton) {
            view.backgroundTintList = ColorStateList.valueOf(color.toArgb())
            if (outline is Outline.Rounded) {
                val roundRect = outline.roundRect
                view.shapeAppearanceModel = view.shapeAppearanceModel.toBuilder()
                    .setTopLeftCornerSize(roundRect.topLeftCornerRadius.x)
                    .setTopRightCornerSize(roundRect.topRightCornerRadius.x)
                    .setBottomRightCornerSize(roundRect.bottomRightCornerRadius.x)
                    .setBottomLeftCornerSize(roundRect.bottomLeftCornerRadius.x)
                    .build()
            }
        } else {
            val background = GradientDrawable()
            background.color = ColorStateList.valueOf(color.toArgb())
            if (outline is Outline.Rounded) {
                val roundRect = outline.roundRect
                background.cornerRadii = floatArrayOf(
                    roundRect.topLeftCornerRadius.x,
                    roundRect.topLeftCornerRadius.y,
                    roundRect.topRightCornerRadius.x,
                    roundRect.topRightCornerRadius.y,
                    roundRect.bottomRightCornerRadius.x,
                    roundRect.bottomRightCornerRadius.y,
                    roundRect.bottomLeftCornerRadius.x,
                    roundRect.bottomLeftCornerRadius.y
                )
            }
            view.background = background
        }
    }
}


private fun applyPadding(
    view: View,
    contentPadding: PaddingValues,
    density: Density,
) {
    val viewLayoutDirection = if (view.layoutDirection == View.LAYOUT_DIRECTION_RTL)
        LayoutDirection.Rtl
    else
        LayoutDirection.Ltr
    with(receiver = density) {
        val left = contentPadding
            .calculateLeftPadding(layoutDirection = viewLayoutDirection)
            .roundToPx()
        val top = contentPadding
            .calculateTopPadding()
            .roundToPx()
        val right = contentPadding
            .calculateRightPadding(layoutDirection = viewLayoutDirection)
            .roundToPx()
        val bottom = contentPadding
            .calculateBottomPadding()
            .roundToPx()
        view.setPadding(
            left,
            top,
            right,
            bottom
        )
    }
}
