@file:OptIn(JetAdMobAlpha::class)

package com.jet.admob.example.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.jet.admob.AdMobAdsUtil
import com.jet.admob.AdMobNative
import com.jet.admob.NativeAdFormat
import com.jet.admob.annotations.JetAdMobAlpha
import com.jet.admob.example.JetAdMobAdsTheme
import com.jet.admob.example.LazyColumnScreen
import com.jet.admob.rememberAdMobNativeAdState


/**
 * Examples of Native ads
 * @author Miroslav Hýbler <br>
 * created on 08.01.2026
 */
@Composable
fun NativeAdsScreen() {
    val smallNativeAdState = rememberAdMobNativeAdState(
        adUnitId = AdMobAdsUtil.TestIds.NATIVE,
    )
    val mediumNativeAdState = rememberAdMobNativeAdState(
        adUnitId = AdMobAdsUtil.TestIds.NATIVE,
    )

    LazyColumnScreen(
        title = "Native ads"
    ) {

        item {
            AdMobNative(
                state = smallNativeAdState,
                adFormat = NativeAdFormat.Small,
            )
        }

        item {
            AdMobNative(
                modifier = Modifier.padding(horizontal = 20.dp),
                state = mediumNativeAdState,
                adFormat = NativeAdFormat.Medium,
            )
        }
    }
}


@Composable
@PreviewLightDark
private fun NativeAdsPreview() {
    JetAdMobAdsTheme() {
        NativeAdsScreen()
    }
}
