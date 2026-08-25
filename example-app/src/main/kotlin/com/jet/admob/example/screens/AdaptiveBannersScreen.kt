@file:OptIn(JetAdMobAlpha::class)

package com.jet.admob.example.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.jet.admob.AdMobAdsUtil
import com.jet.admob.AdMobBannerState
import com.jet.admob.annotations.JetAdMobAlpha
import com.jet.admob.example.JetAdMobAdsTheme
import com.jet.admob.example.LazyColumnScreen
import com.jet.admob.rememberAdMobBannerState


/**
 * @author Miroslav Hýbler <br>
 * created on 07.01.2026
 */
@Composable
fun AdaptiveBannersScreen() {
    val context = LocalContext.current
    val density = LocalDensity.current
    val screenWidth = LocalResources.current.displayMetrics.widthPixels
    val screenWidthDp = (screenWidth / density.density).toInt()

    val landscapeAnchoredAdSize = remember(context, screenWidthDp) {
        AdMobBannerState.getLargeLandscapeAnchoredAdaptiveBannerAdSize(context, screenWidthDp)
    }

    val portraitAnchoredAdSize = remember(context, screenWidthDp) {
        AdMobBannerState.getLargePortraitAnchoredAdaptiveBannerAdSize(context, screenWidthDp)
    }

    val inlineAdaptiveAdSize = remember(screenWidthDp) {
        AdMobBannerState.getInlineAdaptiveBannerAdSize(screenWidthDp, 128)
    }

    val landscapeAnchoredAdState = rememberAdMobBannerState(
        adUnitId = AdMobAdsUtil.TestIds.ADAPTIVE_BANNER,
        adSize = landscapeAnchoredAdSize,
    )
    val portraitAnchoredAdState = rememberAdMobBannerState(
        adUnitId = AdMobAdsUtil.TestIds.ADAPTIVE_BANNER,
        adSize = portraitAnchoredAdSize,
    )
    val inlineAdaptiveAdState = rememberAdMobBannerState(
        adUnitId = AdMobAdsUtil.TestIds.ADAPTIVE_BANNER,
        adSize = inlineAdaptiveAdSize,
    )

    LazyColumnScreen(
        title = "Adaptive Banners",
    ) {
        bannerItem(
            adState = landscapeAnchoredAdState,
            label = "getLargeLandscapeAnchoredAdaptiveBannerAdSize(): $landscapeAnchoredAdSize",
        )
        bannerItem(
            adState = portraitAnchoredAdState,
            label = "getLargePortraitAnchoredAdaptiveBannerAdSize(): $portraitAnchoredAdSize",
        )
        bannerItem(
            adState = inlineAdaptiveAdState,
            label = "getInlineAdaptiveBannerAdSize(): $inlineAdaptiveAdSize",
        )
    }
}

@Composable
@PreviewLightDark
private fun AdaptiveBannersScreenPreview() {
    JetAdMobAdsTheme() {
        AdaptiveBannersScreen()
    }
}
