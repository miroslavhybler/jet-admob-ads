@file:OptIn(ExperimentalMaterial3Api::class, JetAdMobAlpha::class)

package com.jet.admob.example.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.jet.admob.AdMobAdsUtil
import com.jet.admob.AdMobBanner
import com.jet.admob.AdMobBannerState
import com.jet.admob.BannerAdSize
import com.jet.admob.annotations.JetAdMobAlpha
import com.jet.admob.example.JetAdMobAdsTheme
import com.jet.admob.example.LazyColumnScreen
import com.jet.admob.rememberAdMobBannerState


/**
 * Examples of Banners with fixed size using defined [BannerAdSize]
 * @author Miroslav Hýbler <br>
 * created on 07.01.2026
 */
@Composable
fun BannersScreen() {
    val bannerState = rememberAdMobBannerState(
        adUnitId = AdMobAdsUtil.TestIds.FIXED_SIZE_BANNER,
        adSize = BannerAdSize.BANNER,
    )
    val fullBannerState = rememberAdMobBannerState(
        adUnitId = AdMobAdsUtil.TestIds.FIXED_SIZE_BANNER,
        adSize = BannerAdSize.FULL_BANNER,
    )
    val largeBannerState = rememberAdMobBannerState(
        adUnitId = AdMobAdsUtil.TestIds.FIXED_SIZE_BANNER,
        adSize = BannerAdSize.LARGE_BANNER,
    )
    val leaderboardState = rememberAdMobBannerState(
        adUnitId = AdMobAdsUtil.TestIds.FIXED_SIZE_BANNER,
        adSize = BannerAdSize.LEADERBOARD,
    )
    val mediumRectangleState = rememberAdMobBannerState(
        adUnitId = AdMobAdsUtil.TestIds.FIXED_SIZE_BANNER,
        adSize = BannerAdSize.MEDIUM_RECTANGLE,
    )
    val wideSkyscraperState = rememberAdMobBannerState(
        adUnitId = AdMobAdsUtil.TestIds.ADAPTIVE_BANNER,
        adSize = BannerAdSize.WIDE_SKYSCRAPER,
    )

    LazyColumnScreen(
        title = "Basic Banners",
    ) {
        bannerItem(
            adState = bannerState,
            label = "BannerAdSize.BANNER",
        )

        bannerItem(
            adState = fullBannerState,
            label = "BannerAdSize.FULL_BANNER",
            preOccupySpace = true,
        )
        bannerItem(
            adState = largeBannerState,
            label = "BannerAdSize.LARGE_BANNER",
        )

        bannerItem(
            adState = leaderboardState,
            label = "BannerAdSize.LEADERBOARD",
        )

        bannerItem(
            adState = mediumRectangleState,
            label = "BannerAdSize.MEDIUM_RECTANGLE",
        )
        bannerItem(
            adState = wideSkyscraperState,
            label = "BannerAdSize.WIDE_SKYSCRAPER",
            preOccupySpace = true,
        )
    }
}


fun LazyListScope.bannerItem(
    adState: AdMobBannerState,
    label: String,
    preOccupySpace: Boolean = false,
) {
    item(key = label) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        ) {
            Text(text = label)

            AdMobBanner(
                state = adState,
                preOccupySpace = preOccupySpace,
            )
        }
    }
}


@Composable
@PreviewLightDark
private fun BannersScreenPreview() {
    JetAdMobAdsTheme() {
        BannersScreen()
    }
}
