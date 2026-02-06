package com.jet.admob.example.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.jet.admob.AdMobAdsUtil
import com.jet.admob.AdMobNative
import com.jet.admob.NativeAdColors
import com.jet.admob.NativeAdFormat

/**
 * @author Miroslav Hýbler <br>
 * created on 11.01.2026
 */
@Composable
fun NativeAdFullScreenScreen() {
    // A full-screen ad should not be placed inside a scrollable container like LazyColumn.
    // It should occupy the entire screen space available to it.
    AdMobNative(
        modifier = Modifier.fillMaxSize(),
        adUnitId = AdMobAdsUtil.TestIds.NATIVE, //NATIVE_VIDEO can be used too but it often returns Ad Error 3 (no fill)
        adFormat = NativeAdFormat.FullScreen,
        colors = NativeAdColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            buttonColor = MaterialTheme.colorScheme.primary,
            buttonTextColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}
