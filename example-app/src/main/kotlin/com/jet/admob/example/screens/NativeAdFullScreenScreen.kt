package com.jet.admob.example.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.jet.admob.AdMobAdsUtil
import com.jet.admob.AdMobNative
import com.jet.admob.NativeAdColors
import com.jet.admob.NativeAdFormat
import com.jet.admob.example.LazyColumnScreen


/**
 * @author Miroslav Hýbler <br>
 * created on 11.01.2026
 */
@Composable
fun NativeAdFullScreenScreen() {
    LazyColumnScreen(
        title = "Native fullscreen ad"
    ) {
        item {
            AdMobNative(
                modifier = Modifier.fillMaxSize(),
                adUnitId = AdMobAdsUtil.TestIds.NATIVE_VIDEO,
                adFormat = NativeAdFormat.FullScreen,
                colors = NativeAdColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    buttonColor = MaterialTheme.colorScheme.primary,
                    buttonTextColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}