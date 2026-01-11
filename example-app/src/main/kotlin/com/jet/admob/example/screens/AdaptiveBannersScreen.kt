package com.jet.admob.example.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.google.android.gms.ads.AdSize
import com.jet.admob.AdMobAdsUtil
import com.jet.admob.example.LazyColumnScreen
import com.jet.admob.example.screens.bannerItem
import com.jet.admob.example.theme.JetAdMobAdsTheme


/**
 * TODO
 * @author Miroslav Hýbler<br>
 * created on 07.01.2026
 */
@Composable
fun AdaptiveBannersScreen() {
    val context = LocalContext.current
    val dst = LocalResources.current.displayMetrics.density
    val screenWidth = LocalResources.current.displayMetrics.widthPixels

    val adSize = remember {
        AdSize.getPortraitAnchoredAdaptiveBannerAdSize(
            context,
            (screenWidth / dst).toInt()
        )
    }

    LazyColumnScreen(
        title = "Adaptive Banners",
    ) {
        
        bannerItem(
            adUnitId = AdMobAdsUtil.TestIds.FIXED_SIZE_BANNER,
            adSize = adSize,
            label = "AdSize.getPortraitAnchoredAdaptiveBannerAdSize $adSize",
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