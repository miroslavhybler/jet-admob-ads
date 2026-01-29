package com.jet.admob.example.screens

import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.jet.admob.AdMobAdsUtil
import com.jet.admob.AdStatus
import com.jet.admob.example.ColumnScreen
import com.jet.admob.rememberAdMobRewardedAdState

/**
 * @author Miroslav Hýbler <br>
 * created on 27.01.2026
 */
@Composable
fun RewardedScreen() {
    val context = LocalContext.current

    val rewardedAdState = rememberAdMobRewardedAdState(
        adUnitId = AdMobAdsUtil.TestIds.REWARDED_ADS,
        onUserEarnedReward = {
            Toast.makeText(
                context,
                "User earned reward",
                Toast.LENGTH_SHORT
            ).show()
        }
    )

    val adStatus by rewardedAdState.adStatus.collectAsState()

    ColumnScreen(
        title = "Rewarded Ad Example"
    ) {
        Button(
            onClick = { rewardedAdState.show(context=context) },
            enabled = adStatus == AdStatus.Loaded
        ) {
            Text(text = "Show Rewarded Ad")
        }

        Text(
            modifier = Modifier.padding(horizontal = 20.dp),
            text = when (adStatus) {
                is AdStatus.Failed -> "Ad failed to load"
                is AdStatus.Loaded -> "Ad is loaded"
                is AdStatus.Loading -> "Ad is loading"
                is AdStatus.Shown -> "Ad is shown"
            }
        )

    }
}
