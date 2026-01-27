package com.jet.admob.example.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.jet.admob.example.LazyColumnScreen
import com.jet.admob.example.Route
import com.jet.admob.example.theme.JetAdMobAdsTheme


/**
 * @author Miroslav Hýbler <br>
 * created on 07.01.2026
 */
@Composable
fun HomeScreen(
    onNavigate: (Route) -> Unit,
) {
    LazyColumnScreen(
        title = "Home"
    ) {
        buttonItem(
            text = "Fixed size Banners",
            onClick = {
                onNavigate(Route.Banners)
            },
        )

        buttonItem(
            text = "Adaptive size Banners",
            onClick = {
                onNavigate(Route.AdaptiveBanners)
            },
        )

        buttonItem(
            text = "Native Ads",
            onClick = {
                onNavigate(Route.NativeAds)
            },
        )

        buttonItem(
            text = "Native fullscreen Ad",
            onClick = {
                onNavigate(Route.NativeAdFullScreen)
            },
        )

        buttonItem(
            text = "Rewarded Ad",
            onClick = {
                onNavigate(Route.Rewarded)
            },
        )
    }
}


private fun LazyListScope.buttonItem(
    onClick: () -> Unit,
    text: String,
) {
    item {
        Button(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .height(height = 64.dp),
            onClick = onClick,
        ) {
            Text(text = text)
        }
    }
}


@Composable
@PreviewLightDark
private fun HomeScreenPreview() {
    JetAdMobAdsTheme() {
        HomeScreen(
            onNavigate = {},
        )
    }
}