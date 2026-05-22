@file:OptIn(JetAdMobAlpha::class)

package com.jet.admob.example.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.google.android.gms.ads.AdSize
import com.jet.admob.AdMobAdsUtil
import com.jet.admob.AdMobBanner
import com.jet.admob.AdMobNative
import com.jet.admob.NativeAdColors
import com.jet.admob.NativeAdFormat
import com.jet.admob.annotations.JetAdMobAlpha
import com.jet.admob.example.JetAdMobAdsTheme
import com.jet.admob.example.LazyColumnScreen
import com.jet.admob.rememberAdMobBannerState
import com.jet.admob.rememberAdMobNativeAdState


/**
 * Example of hoisting ad state above lazy list items.
 *
 * @author Miroslav Hýbler <br>
 * created on 22.05.2026
 */
@Composable
fun AdsInContentExampleScreen() {
    val bannerAdState = rememberAdMobBannerState(
        adUnitId = AdMobAdsUtil.TestIds.FIXED_SIZE_BANNER,
        adSize = AdSize.BANNER,
    )
    val nativeAdState = rememberAdMobNativeAdState(
        adUnitId = AdMobAdsUtil.TestIds.NATIVE,
    )

    LazyColumnScreen(
        title = "Ads in Content",
    ) {
        items(
            count = 8,
            key = { index -> "intro-$index" },
        ) { index ->
            ContentText(text = sampleContent[index])
        }

        item(key = "banner-ad") {
            AdMobBanner(
                modifier = Modifier.padding(horizontal = 20.dp),
                state = bannerAdState,
                preOccupySpace = true,
            )
        }

        items(
            count = 8,
            key = { index -> "middle-$index" },
        ) { index ->
            ContentText(text = sampleContent[index + 8])
        }

        item(key = "native-ad") {
            AdMobNative(
                modifier = Modifier.padding(horizontal = 20.dp),
                state = nativeAdState,
                adFormat = NativeAdFormat.Medium,
                colors = NativeAdColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    buttonColor = MaterialTheme.colorScheme.primary,
                    buttonTextColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        }

        items(
            count = 8,
            key = { index -> "outro-$index" },
        ) { index ->
            ContentText(text = sampleContent[index + 16])
        }
    }
}


@Composable
private fun ContentText(
    text: String,
) {
    Text(
        modifier = Modifier.padding(horizontal = 20.dp),
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onBackground,
    )
}


private val sampleContent = listOf(
    "Content item 1: Morning light moved slowly across the skyline as the city started to wake.",
    "Content item 2: The first trains were already full, carrying coffee cups and quiet plans.",
    "Content item 3: By the river, runners traced the same path in opposite directions.",
    "Content item 4: A bakery opened its doors and let warm air spill onto the sidewalk.",
    "Content item 5: Office windows caught the sun one floor at a time.",
    "Content item 6: The day had the easy rhythm of a place that knew itself well.",
    "Content item 7: Small errands filled the gaps between bigger intentions.",
    "Content item 8: Street signs, bicycle bells, and conversations layered into a steady hum.",
    "Content item 9: Around noon, the pace changed and the parks filled with short pauses.",
    "Content item 10: People carried lunch outside and sat wherever there was a patch of shade.",
    "Content item 11: Clouds gathered at the edge of the afternoon but never quite arrived.",
    "Content item 12: The river kept its own schedule, bright in some places and dark in others.",
    "Content item 13: Shopfronts changed from practical to inviting as evening came closer.",
    "Content item 14: A musician tuned a guitar near the tram stop and drew a small crowd.",
    "Content item 15: The city seemed louder for a moment, then softer than before.",
    "Content item 16: Windows opened, lights came on, and dinner tables started to fill.",
    "Content item 17: Somewhere above the avenue, a balcony garden leaned into the air.",
    "Content item 18: The last commute of the day moved with less urgency.",
    "Content item 19: Cafes kept their doors open, holding onto the mild weather.",
    "Content item 20: Friends met at corners and decided where to go only after arriving.",
    "Content item 21: The sky turned blue, then silver, then a color without a name.",
    "Content item 22: Reflections stretched across the pavement after the streetlights came on.",
    "Content item 23: By night, the city had folded the day neatly into its noise.",
    "Content item 24: The final windows went dark long after the river disappeared from view.",
)


@Composable
@PreviewLightDark
private fun AdsInContentExampleScreenPreview() {
    JetAdMobAdsTheme {
        AdsInContentExampleScreen()
    }
}
