@file:OptIn(ExperimentalMaterial3Api::class)

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
import com.google.android.gms.ads.AdSize
import com.jet.admob.AdMobAdsUtil
import com.jet.admob.AdMobBanner
import com.jet.admob.example.LazyColumnScreen
import com.jet.admob.example.theme.JetAdMobAdsTheme


/**
 * Examples of Banners with fixed size using defined [AdSize]
 * @author Miroslav Hýbler <br>
 * created on 07.01.2026
 */
@Composable
fun BannersScreen() {

    LazyColumnScreen(
        title = "Basic Banners",
    ) {
        //This is just for this example simplification, try not to use ads in lazyColumn
        bannerItem(
            adUnitId = AdMobAdsUtil.TestIds.FIXED_SIZE_BANNER,
            adSize = AdSize.BANNER,
            label = "AdSize.BANNER",
        )

        bannerItem(
            adUnitId = AdMobAdsUtil.TestIds.FIXED_SIZE_BANNER,
            adSize = AdSize.FULL_BANNER,
            label = "AdSize.FULL_BANNER",
            preOccupySpace = true,
            )
        bannerItem(
            adUnitId = AdMobAdsUtil.TestIds.FIXED_SIZE_BANNER,
            adSize = AdSize.LARGE_BANNER,
            label = "AdSize.LARGE_BANNER",

            )

        bannerItem(
            adUnitId = AdMobAdsUtil.TestIds.FIXED_SIZE_BANNER,
            adSize = AdSize.LEADERBOARD,
            label = "AdSize.LEADERBOARD",
        )

        bannerItem(
            adUnitId = AdMobAdsUtil.TestIds.FIXED_SIZE_BANNER,
            adSize = AdSize.MEDIUM_RECTANGLE,
            label = "AdSize.MEDIUM_RECTANGLE",
        )
        bannerItem(
            adUnitId = AdMobAdsUtil.TestIds.ADAPTIVE_BANNER,
            adSize = AdSize.WIDE_SKYSCRAPER,
            label = "AdSize.WIDE_SKYSCRAPER",
            preOccupySpace = true,
        )
    }
}


fun LazyListScope.bannerItem(
    adUnitId: String,
    adSize: AdSize,
    label: String,
    preOccupySpace: Boolean = false,
) {
    item() {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        ) {
            Text(text = label)

            AdMobBanner(
                adUnitId = adUnitId,
                adSize = adSize,
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