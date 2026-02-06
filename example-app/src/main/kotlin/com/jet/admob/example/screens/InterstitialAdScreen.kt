package com.jet.admob.example.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.jet.admob.AdMobAdsUtil
import com.jet.admob.AdPresentationState
import com.jet.admob.example.ColumnScreen
import com.jet.admob.rememberAdMobInterstitialAdState


/**
 * @author Miroslav Hýbler <br>
 * created on 24.01.2026
 */
@Composable
fun InterstitialAdScreen() {
    val context = LocalContext.current
    val adState = rememberAdMobInterstitialAdState(
        adUnitId = AdMobAdsUtil.TestIds.INTERSTITIAL,
    )

    LaunchedEffect(key1 = adState.presentationState) {
        if (adState.presentationState is AdPresentationState.Dismissed) {
            // Handle ad dismissal, e.g., navigate back or to the next screen
            Toast.makeText(
                context,
                "Ad dismissed. Preloading next ad.",
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    ColumnScreen(
        title = "Interstitial Ad Example"
    ) {
        Button(
            modifier = Modifier.padding(horizontal = 20.dp),
            onClick = { adState.show() },
            enabled = adState.isAdLoaded
        ) {
            Text(if (adState.isAdLoaded) "Show Interstitial Ad" else "Loading Ad...")
        }

        Text(
            modifier = Modifier.padding(horizontal = 20.dp),
            text = when (val state = adState.presentationState) {
                AdPresentationState.Idle -> "Ad is idle."
                AdPresentationState.Dismissed -> "Ad was dismissed."
                is AdPresentationState.FailedToShow -> "Ad failed to show: ${state.error.message}"
                AdPresentationState.Showed -> "Ad is being shown."
            }
        )

        adState.adError?.let {
            Text(
                modifier = Modifier.padding(horizontal = 20.dp),
                text = "Ad failed to load: ${it.message}",
            )
        }
    }
}