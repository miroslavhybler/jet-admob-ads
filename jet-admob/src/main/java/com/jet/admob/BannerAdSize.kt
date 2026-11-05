package com.jet.admob

import android.content.Context
import com.google.android.gms.ads.AdSize as GoogleAdSize
import com.jet.admob.annotations.JetAdMobAlpha


/**
 * SDK-independent description of a banner ad size.
 *
 * This type is converted to the active Google Mobile Ads SDK representation internally, keeping
 * SDK-specific `AdSize` classes out of application code.
 *
 * @since 1.1.0
 */
@JetAdMobAlpha
class BannerAdSize private constructor(
    val width: Int,
    val height: Int,
    private val descriptor: Descriptor,
) {

    /**
     * Creates a custom banner ad size in density-independent pixels.
     *
     * @since 1.1.0
     */
    constructor(
        width: Int,
        height: Int,
    ) : this(
        width = width,
        height = height,
        descriptor = Descriptor.Custom(width = width, height = height),
    )

    internal fun toAdSize(context: Context): GoogleAdSize = when (val value = descriptor) {
        Descriptor.Banner -> GoogleAdSize.BANNER
        Descriptor.Fluid -> GoogleAdSize.FLUID
        Descriptor.FullBanner -> GoogleAdSize.FULL_BANNER
        Descriptor.LargeBanner -> GoogleAdSize.LARGE_BANNER
        Descriptor.Leaderboard -> GoogleAdSize.LEADERBOARD
        Descriptor.MediumRectangle -> GoogleAdSize.MEDIUM_RECTANGLE
        Descriptor.WideSkyscraper -> GoogleAdSize.WIDE_SKYSCRAPER
        is Descriptor.Custom -> GoogleAdSize(value.width, value.height)
        is Descriptor.InlineAdaptive -> GoogleAdSize.getInlineAdaptiveBannerAdSize(
            value.width,
            value.maxHeight,
        )
        is Descriptor.CurrentOrientationInlineAdaptive ->
            GoogleAdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(context, value.width)
        is Descriptor.LandscapeInlineAdaptive ->
            GoogleAdSize.getLandscapeInlineAdaptiveBannerAdSize(context, value.width)
        is Descriptor.PortraitInlineAdaptive ->
            GoogleAdSize.getPortraitInlineAdaptiveBannerAdSize(context, value.width)
        is Descriptor.LargeAnchoredAdaptive ->
            GoogleAdSize.getLargeAnchoredAdaptiveBannerAdSize(context, value.width)
        is Descriptor.LargeLandscapeAnchoredAdaptive ->
            GoogleAdSize.getLargeLandscapeAnchoredAdaptiveBannerAdSize(context, value.width)
        is Descriptor.LargePortraitAnchoredAdaptive ->
            GoogleAdSize.getLargePortraitAnchoredAdaptiveBannerAdSize(context, value.width)
        is Descriptor.Legacy -> value.adSize
    }

    override fun equals(other: Any?): Boolean = this === other ||
        other is BannerAdSize &&
        width == other.width &&
        height == other.height &&
        descriptor == other.descriptor

    override fun hashCode(): Int {
        var result = width
        result = 31 * result + height
        result = 31 * result + descriptor.hashCode()
        return result
    }

    override fun toString(): String = descriptor.toString()

    companion object {

        internal fun fromAdSize(adSize: GoogleAdSize): BannerAdSize = BannerAdSize(
            width = adSize.width,
            height = adSize.height,
            descriptor = Descriptor.Legacy(adSize),
        )

        /** Mobile Marketing Association banner ad size (320x50 dp). */
        @JvmField
        val BANNER: BannerAdSize = BannerAdSize(
            width = 320,
            height = 50,
            descriptor = Descriptor.Banner,
        )

        /** A dynamically sized banner that matches its parent's width and content height. */
        @JvmField
        val FLUID: BannerAdSize = BannerAdSize(
            width = GoogleAdSize.FLUID.width,
            height = GoogleAdSize.FLUID.height,
            descriptor = Descriptor.Fluid,
        )

        /** Interactive Advertising Bureau full banner ad size (468x60 dp). */
        @JvmField
        val FULL_BANNER: BannerAdSize = BannerAdSize(
            width = 468,
            height = 60,
            descriptor = Descriptor.FullBanner,
        )

        /** Large banner ad size (320x100 dp). */
        @JvmField
        val LARGE_BANNER: BannerAdSize = BannerAdSize(
            width = 320,
            height = 100,
            descriptor = Descriptor.LargeBanner,
        )

        /** Interactive Advertising Bureau leaderboard ad size (728x90 dp). */
        @JvmField
        val LEADERBOARD: BannerAdSize = BannerAdSize(
            width = 728,
            height = 90,
            descriptor = Descriptor.Leaderboard,
        )

        /** Interactive Advertising Bureau medium rectangle ad size (300x250 dp). */
        @JvmField
        val MEDIUM_RECTANGLE: BannerAdSize = BannerAdSize(
            width = 300,
            height = 250,
            descriptor = Descriptor.MediumRectangle,
        )

        /** Interactive Advertising Bureau wide skyscraper ad size (160x600 dp). */
        @JvmField
        val WIDE_SKYSCRAPER: BannerAdSize = BannerAdSize(
            width = 160,
            height = 600,
            descriptor = Descriptor.WideSkyscraper,
        )

        /** Returns an inline adaptive size with the given width and maximum height. */
        @JvmStatic
        fun getInlineAdaptiveBannerAdSize(
            width: Int,
            maxHeight: Int,
        ): BannerAdSize = adaptive(
            googleAdSize = GoogleAdSize.getInlineAdaptiveBannerAdSize(width, maxHeight),
            descriptor = Descriptor.InlineAdaptive(width = width, maxHeight = maxHeight),
        )

        /** Returns an inline adaptive size for the device's current orientation. */
        @JvmStatic
        fun getCurrentOrientationInlineAdaptiveBannerAdSize(
            context: Context,
            width: Int,
        ): BannerAdSize = adaptive(
            googleAdSize = GoogleAdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(
                context,
                width,
            ),
            descriptor = Descriptor.CurrentOrientationInlineAdaptive(width = width),
        )

        /** Returns an inline adaptive size for landscape orientation. */
        @JvmStatic
        fun getLandscapeInlineAdaptiveBannerAdSize(
            context: Context,
            width: Int,
        ): BannerAdSize = adaptive(
            googleAdSize = GoogleAdSize.getLandscapeInlineAdaptiveBannerAdSize(context, width),
            descriptor = Descriptor.LandscapeInlineAdaptive(width = width),
        )

        /** Returns an inline adaptive size for portrait orientation. */
        @JvmStatic
        fun getPortraitInlineAdaptiveBannerAdSize(
            context: Context,
            width: Int,
        ): BannerAdSize = adaptive(
            googleAdSize = GoogleAdSize.getPortraitInlineAdaptiveBannerAdSize(context, width),
            descriptor = Descriptor.PortraitInlineAdaptive(width = width),
        )

        /** Returns a large anchored adaptive size for the device's current orientation. */
        @JvmStatic
        fun getLargeAnchoredAdaptiveBannerAdSize(
            context: Context,
            width: Int,
        ): BannerAdSize = adaptive(
            googleAdSize = GoogleAdSize.getLargeAnchoredAdaptiveBannerAdSize(context, width),
            descriptor = Descriptor.LargeAnchoredAdaptive(width = width),
        )

        /** Returns a large anchored adaptive size for landscape orientation. */
        @JvmStatic
        fun getLargeLandscapeAnchoredAdaptiveBannerAdSize(
            context: Context,
            width: Int,
        ): BannerAdSize = adaptive(
            googleAdSize = GoogleAdSize.getLargeLandscapeAnchoredAdaptiveBannerAdSize(
                context,
                width,
            ),
            descriptor = Descriptor.LargeLandscapeAnchoredAdaptive(width = width),
        )

        /** Returns a large anchored adaptive size for portrait orientation. */
        @JvmStatic
        fun getLargePortraitAnchoredAdaptiveBannerAdSize(
            context: Context,
            width: Int,
        ): BannerAdSize = adaptive(
            googleAdSize = GoogleAdSize.getLargePortraitAnchoredAdaptiveBannerAdSize(
                context,
                width,
            ),
            descriptor = Descriptor.LargePortraitAnchoredAdaptive(width = width),
        )

        private fun adaptive(
            googleAdSize: GoogleAdSize,
            descriptor: Descriptor,
        ): BannerAdSize = BannerAdSize(
            width = googleAdSize.width,
            height = googleAdSize.height,
            descriptor = descriptor,
        )
    }

    private sealed interface Descriptor {
        data object Banner : Descriptor
        data object Fluid : Descriptor
        data object FullBanner : Descriptor
        data object LargeBanner : Descriptor
        data object Leaderboard : Descriptor
        data object MediumRectangle : Descriptor
        data object WideSkyscraper : Descriptor
        data class Custom(val width: Int, val height: Int) : Descriptor
        data class InlineAdaptive(val width: Int, val maxHeight: Int) : Descriptor
        data class CurrentOrientationInlineAdaptive(val width: Int) : Descriptor
        data class LandscapeInlineAdaptive(val width: Int) : Descriptor
        data class PortraitInlineAdaptive(val width: Int) : Descriptor
        data class LargeAnchoredAdaptive(val width: Int) : Descriptor
        data class LargeLandscapeAnchoredAdaptive(val width: Int) : Descriptor
        data class LargePortraitAnchoredAdaptive(val width: Int) : Descriptor
        data class Legacy(val adSize: GoogleAdSize) : Descriptor
    }
}
