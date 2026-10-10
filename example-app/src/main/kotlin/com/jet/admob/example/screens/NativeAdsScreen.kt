@file:OptIn(JetAdMobAlpha::class)

package com.jet.admob.example.screens

import android.widget.ImageView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.jet.admob.AdMobAdsUtil
import com.jet.admob.AdMobNative
import com.jet.admob.AdMobNativeAdState
import com.jet.admob.AdMobNativeLayout
import com.jet.admob.NativeAdActionButton
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
    val customNativeAdState = rememberAdMobNativeAdState(
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

        item {
            CustomNativeAd(state = customNativeAdState)
        }
    }
}


@Composable
private fun CustomNativeAd(
    state: AdMobNativeAdState,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(space = 8.dp),
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 20.dp),
            text = "Custom Compose layout",
            style = MaterialTheme.typography.titleMedium,
        )

        AdMobNativeLayout(
            state = state,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        ) {
            Column(
                modifier = Modifier
                    .clip(shape = RoundedCornerShape(size = 24.dp))
                    .background(color = MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(all = 16.dp),
                verticalArrangement = Arrangement.spacedBy(space = 12.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(space = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        modifier = Modifier
                            .size(size = 52.dp)
                            .clip(shape = RoundedCornerShape(size = 12.dp)),
                    ) { drawable ->
                        val bitmap = remember(drawable) {
                            drawable.toBitmap().asImageBitmap()
                        }
                        Image(
                            bitmap = bitmap,
                            contentDescription = "Advertiser icon",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    }

                    Column(
                        modifier = Modifier.weight(weight = 1f),
                        verticalArrangement = Arrangement.spacedBy(space = 4.dp),
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            AdAttribution(
                                shape = RoundedCornerShape(size = 4.dp),
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            )
                            Advertiser { advertiser ->
                                Text(
                                    text = advertiser,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        Headline { headline ->
                            Text(
                                text = headline,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }

                Media(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(ratio = 16f / 9f)
                        .clip(shape = RoundedCornerShape(size = 16.dp)),
                    scaleType = ImageView.ScaleType.CENTER_CROP,
                )

                Body { body ->
                    Text(
                        text = body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                CallToAction(modifier = Modifier.fillMaxWidth()) { label ->
                    NativeAdActionButton(
                        text = label,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(size = 12.dp),
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        contentPadding = PaddingValues(
                            horizontal = 16.dp,
                            vertical = 12.dp,
                        ),
                    )
                }
            }
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
