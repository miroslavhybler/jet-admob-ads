package com.jet.admob

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
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
data class NativeAdColors constructor(
    val containerColor: Color,
    val contentColor: Color,
    val buttonColor: Color,
    val buttonTextColor: Color,
)


// Loads a native ad from AdMob.
private fun loadNativeAd(
    context: Context,
    adUnitId: String,
    callback: (NativeAd?) -> Unit,
) {
    val builder = AdLoader.Builder(context, adUnitId)
        .forNativeAd { nativeAd ->
            callback(nativeAd)
        }

    val adLoader = builder
        .withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e("NATIVE_AD", "Failed to load native ad: ${"$"}{adError.message}")
                callback(null)
            }
        })
        .withNativeAdOptions(
            NativeAdOptions.Builder()
                .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                .build()
        )
        .build()

    adLoader.loadAd(AdRequest.Builder().build())
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
    fun shape(format: NativeAdFormat = NativeAdFormat.Small): Shape {
        return when (format) {
            is NativeAdFormat.Small -> RectangleShape
            is NativeAdFormat.Medium -> MaterialTheme.shapes.medium
            is NativeAdFormat.FullScreen -> RectangleShape
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
 * @param adFormat The format of the native ad, which determines the underlying XML layout to be inflated. See [NativeAdFormat].
 * @param shape The shape of the ad container. Defaults to [RectangleShape].
 * @param buttonShape The shape to be applied to the call-to-action button. Defaults to [RectangleShape].
 * @param colors An instance of [NativeAdColors] to customize the colors of the ad container, content, and button.
 *               If null, the default colors from the XML template and app theme will be used.
 * @since 1.0.0
 */
@Composable
fun AdMobNative(
    modifier: Modifier = Modifier,
    adUnitId: String,
    adFormat: NativeAdFormat = NativeAdFormat.Small,
    shape: Shape = NativeAdDefaults.shape(format = adFormat),
    buttonShape: Shape = ButtonDefaults.shape,
    colors: NativeAdColors = NativeAdDefaults.colors(),
) {

    val context = LocalContext.current
    var nativeAd by remember { mutableStateOf<NativeAd?>(value = null) }
    val density = LocalDensity.current

    LaunchedEffect(key1 = adUnitId) {
        loadNativeAd(
            context = context,
            adUnitId = adUnitId,
        ) { ad ->
            nativeAd = ad
        }
    }

    DisposableEffect(key1 = adUnitId) {
        onDispose {
            nativeAd?.destroy()
        }
    }

    nativeAd?.let { nativeAd ->
        AndroidView(
            modifier = modifier.fillMaxWidth(),
            factory = {
                val layout = when (adFormat) {
                    is NativeAdFormat.Small -> R.layout.view_ad_small
                    is NativeAdFormat.Medium -> R.layout.view_ad_medium
                    is NativeAdFormat.FullScreen -> R.layout.view_ad_fullscreen
                }
                LayoutInflater.from(it).inflate(layout, null) as NativeAdView
            },
            update = { adView ->
                // Find all the views from the layout
                val headlineView = adView.findViewById<TextView>(R.id.ad_headline)
                val bodyView = adView.findViewById<TextView>(R.id.ad_body)
                val callToActionView = adView.findViewById<MaterialButton>(R.id.ad_call_to_action)
                val iconView = adView.findViewById<ImageView>(R.id.ad_icon)
                val mediaView = adView.findViewById<MediaView>(R.id.ad_media)
                val advertiserView = adView.findViewById<TextView>(R.id.ad_advertiser)
                val starRatingView = adView.findViewById<RatingBar>(R.id.ad_stars)

                // Apply colors and shape
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
