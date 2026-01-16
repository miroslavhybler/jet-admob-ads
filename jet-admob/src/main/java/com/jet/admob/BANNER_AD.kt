package com.jet.admob

import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.jet.admob.annotations.JetAdMobAlpha


/**
 * A composable that displays a banner ad from AdMob.
 *
 * **Important:** Modifying or obscuring the ad creative is a violation of AdMob policies.
 * Do not apply rounded corners or other clip shapes to this composable, as it can
 * obscure the ad and/or the required AdChoices icon, leading to suspension of your
 * AdMob account.
 *
 * More info about Banner ads: [Banner ad overview](https://developers.google.com/admob/android/banner)
 * [AdSize docs](https://developers.google.com/admob/android/reference/com/google/android/gms/ads/AdSize)
 *
 * @param modifier The modifier to be applied to the ad.
 * @param adUnitId The ad unit ID for this banner ad. See [Test ads](https://developers.google.com/admob/android/test-ads) for test ad unit IDs.
 * @param adSize The size of the banner ad. See [AdSize](https://developers.google.com/android/reference/com/google/android/gms/ads/AdSize) for more information.
 * @param loadAdRequest A lambda that returns an [AdRequest]. Defaults to a new [AdRequest.Builder().build()].
 * @param adListener An [AdListener] for observing ad events.
 * @param preOccupySpace If true, the composable will pre-occupy the space of the ad size before the ad is loaded. This can prevent layout shifts.
 *
 * @author Miroslav Hýbler <br>
 * created on 06.01.2026
 * @since 1.0.0
 */
@JetAdMobAlpha
@Composable
fun AdMobBanner(
    modifier: Modifier = Modifier,
    adUnitId: String,
    adSize: AdSize,
    loadAdRequest: () -> AdRequest = { AdRequest.Builder().build() },
    adListener: AdListener? = null,
    preOccupySpace: Boolean = false,
) {
    val isInspection = LocalInspectionMode.current

    val adjustedModifier = if (preOccupySpace) {
        modifier.size(
            width = adSize.width.dp,
            height = adSize.height.dp
        )
    } else {
        modifier.wrapContentSize()
    }

    if (isInspection) {
        AdMobBannerInspection(
            modifier = modifier.size(
                width = adSize.width.dp,
                height = adSize.height.dp
            ),
            adSize = adSize,
        )
    } else {
        AndroidView(
            modifier = adjustedModifier,
            factory = { context ->
                AdView(context).apply {
                    this.id = View.generateViewId()
                    this.adUnitId = adUnitId
                    this.setAdSize(adSize)

                    if (adListener != null) {
                        this.adListener = adListener
                    }

                    this.loadAd(loadAdRequest())
                }
            }
        )
    }
}

@Composable
private fun AdMobBannerInspection(
    modifier: Modifier,
    adSize: AdSize,
) {
    val contentColor = LocalContentColor.current
    Column(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "AdMob Banner Ad Preview\nsize: ${adSize}",
            color = contentColor,
            textAlign = TextAlign.Center,
        )
    }
}


@Composable
@PreviewLightDark
private fun AdMobBannerPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.spacedBy(space = 24.dp)
    ) {
        AdMobBanner(
            modifier = Modifier,
            adUnitId = "ca-app-pub-3940256099942544/6300978111", // Test ID
            adSize = AdSize.LARGE_BANNER
        )
    }
}
