package com.jet.admob.example.screens

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.google.android.gms.ads.AdSize
import com.jet.admob.AdMobAdsUtil
import com.jet.admob.example.LazyColumnScreen
import com.jet.admob.example.theme.JetAdMobAdsTheme


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

    val landscapeAnchoredAdSize = remember {
        AdSize.getLandscapeAnchoredAdaptiveBannerAdSize(context, screenWidthDp)
    }

    val portraitAnchoredAdSize = remember {
        AdSize.getPortraitAnchoredAdaptiveBannerAdSize(context, screenWidthDp)
    }

    val inlineAdaptiveAdSize = remember {
        AdSize.getInlineAdaptiveBannerAdSize(screenWidthDp, 128)
    }

    LazyColumnScreen(
        title = "Adaptive Banners",
    ) {
        //This is just for this example simplification, try not to use ads in lazyColumn
        bannerItem(
            adSize = landscapeAnchoredAdSize,
            label = "getLandscapeAnchoredAdaptiveBannerAdSize()",
        )
        bannerItem(
            adSize = portraitAnchoredAdSize,
            label = "portraitAnchoredAdSize()",
        )
        bannerItem(
            adSize = inlineAdaptiveAdSize,
            label = "inlineAdaptiveAdSize()",
        )
    }
}

private fun LazyListScope.bannerItem(
    adSize: AdSize,
    label: String,
) {

    bannerItem(
        adUnitId = AdMobAdsUtil.TestIds.ADAPTIVE_BANNER,
        adSize = adSize,
        label = "$label: $adSize"
    )
}

@Composable
@PreviewLightDark
private fun AdaptiveBannersScreenPreview() {
    JetAdMobAdsTheme() {
        AdaptiveBannersScreen()
    }
}