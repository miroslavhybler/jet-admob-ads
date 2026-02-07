package com.jet.admob

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.jet.admob.annotations.JetAdMobAlpha
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A state object that can be hoisted to control and observe rewarded ads.
 *
 * @param adUnitId The ad unit ID for the rewarded ad.
 * @param onUserEarnedReward The callback to be invoked when the user has earned a reward.
 * @since 1.0.0
 */
@JetAdMobAlpha
@Immutable
class AdMobRewardedAdState constructor(
    val adUnitId: String,
    val onUserEarnedReward: () -> Unit
) {
    private val mAdStatus = MutableStateFlow<AdStatus>(value = AdStatus.Loading)
    val adStatus = mAdStatus.asStateFlow()

    private var rewardedAd: RewardedAd? = null

    /**
     * Loads a new rewarded ad.
     *
     * @param context The context to use for loading the ad.
     */
    fun load(context: Context) {
        mAdStatus.value = AdStatus.Loading
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            adUnitId,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    rewardedAd = null
                    mAdStatus.value = AdStatus.Failed
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    mAdStatus.value = AdStatus.Loaded
                }
            }
        )
    }

    /**
     * Shows the rewarded ad.
     *
     * @param context The context to use for showing the ad.
     * @return True if the ad was shown, false otherwise.
     */
    fun show(context: Context): Boolean {
        val activity = context.findActivity() ?: run {
            Log.e(
                "REWARDED_AD",
                "Unable to show RewardedAd, the context.findActivity() returned null! " +
                        "Please report an issue https://github.com/miroslavhybler/jet-admob-ads/issues " +
                        "here and provide all the details that can be useful for debugging."
            )
            return false
        }
        val adToShow = rewardedAd ?: run {
            Log.w("REWARDED_AD", "A show() was called but the rewarded ad wasn't ready yet")
            return false
        }

        adToShow.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                load(context = context)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                rewardedAd = null
                Log.e(
                    "JetAdMobAds",
                    "REWARDED_AD onAdFailedToShowFullScreenContent: ${adError.message}"
                )
                mAdStatus.value = AdStatus.Failed
            }

            override fun onAdShowedFullScreenContent() {
                mAdStatus.value = AdStatus.Shown
            }
        }
        adToShow.show(activity) {
            onUserEarnedReward()
        }
        return true
    }
}

/**
 * Creates a [AdMobRewardedAdState] that is remembered across compositions.
 *
 * @param adUnitId The ad unit ID for the rewarded ad.
 * @param onUserEarnedReward The callback to be invoked when the user has earned a reward.
 * @return A [AdMobRewardedAdState] that can be used to control and observe rewarded ads.
 * @since 1.0.0
 */
@Composable
fun rememberAdMobRewardedAdState(
    adUnitId: String,
    onUserEarnedReward: () -> Unit
): AdMobRewardedAdState {
    val context = LocalContext.current
    val rewardedAdState = remember {
        AdMobRewardedAdState(
            adUnitId = adUnitId,
            onUserEarnedReward = onUserEarnedReward
        )
    }

    LaunchedEffect(key1 = adUnitId) {
        rewardedAdState.load(context = context)
    }

    return rewardedAdState
}