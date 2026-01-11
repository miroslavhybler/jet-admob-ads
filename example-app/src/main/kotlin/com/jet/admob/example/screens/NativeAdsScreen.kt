package com.jet.admob.example.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.jet.admob.AdMobAdsUtil
import com.jet.admob.AdMobNative
import com.jet.admob.NativeAdColors
import com.jet.admob.NativeAdFormat
import com.jet.admob.example.LazyColumnScreen
import com.jet.admob.example.theme.JetAdMobAdsTheme


/**
 * @author Miroslav Hýbler<br>
 * created on 08.01.2026
 */
@Composable
fun NativeAdsScreen() {
    LazyColumnScreen(
        title = "Native ads"
    ) {


        item {
            AdMobNative(
                adUnitId = AdMobAdsUtil.TestIds.NATIVE,
                adFormat = NativeAdFormat.Small,
                colors = NativeAdColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    buttonColor = MaterialTheme.colorScheme.primary,
                    buttonTextColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }

        item {
            AdMobNative(
                adUnitId = AdMobAdsUtil.TestIds.NATIVE,
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