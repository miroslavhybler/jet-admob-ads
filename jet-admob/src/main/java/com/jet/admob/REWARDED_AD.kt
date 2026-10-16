@file:Suppress("RedundantVisibilityModifier", "OPT_IN_USAGE")

package com.jet.admob

import android.content.Context
import android.os.Looper
import android.util.Log
import androidx.annotation.MainThread
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.rewarded.OnUserEarnedRewardListener
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardItem
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAd
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAdEventCallback
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
public class AdMobRewardedAdState internal constructor(
    val adUnitId: String,
    val onUserEarnedReward: () -> Unit,
) {
    private val mAdStatus = MutableStateFlow<AdStatus>(value = AdStatus.Loading)
    val adStatus = mAdStatus.asStateFlow()

    private var rewardedAd: RewardedAd? = null
    private var presentingAd: RewardedAd? = null
    private var isLoading: Boolean = false
    private var isDestroyed: Boolean = false

    /**
     * Loads a new rewarded ad.
     *
     * @param context The context to use for loading the ad. Retained for source compatibility.
     */
    @Suppress("UNUSED_PARAMETER")
    fun load(
        context: Context,
        adRequest: AdRequest = AdRequest.Builder(adUnitId).build(),
    ) {
        runOnMainThread {
            loadOnMainThread(adRequest = adRequest)
        }
    }

    private fun loadOnMainThread(adRequest: AdRequest) {
        if (isDestroyed || isLoading || rewardedAd != null || presentingAd != null) return

        isLoading = true
        mAdStatus.value = AdStatus.Loading
        RewardedAd.load(
            adRequest,
            object : AdLoadCallback<RewardedAd> {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    runOnMainThread {
                        isLoading = false
                        if (isDestroyed) return@runOnMainThread

                        rewardedAd?.destroy()
                        rewardedAd = null
                        mAdStatus.value = AdStatus.Failed
                    }
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    runOnMainThread {
                        isLoading = false
                        if (isDestroyed) {
                            ad.destroy()
                            return@runOnMainThread
                        }

                        rewardedAd?.destroy()
                        rewardedAd = ad
                        mAdStatus.value = AdStatus.Loaded
                    }
                }
            },
        )
    }

    /**
     * Shows the rewarded ad.
     *
     * @param context The context to use for showing the ad.
     * @return True if the ad was ready and scheduled to be shown, false otherwise.
     */
    @MainThread
    fun show(context: Context): Boolean {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            Log.e("REWARDED_AD", "RewardedAd.show() must be called on the main thread")
            return false
        }
        if (isDestroyed) return false

        val activity = context.findActivity() ?: run {
            Log.e(
                "REWARDED_AD",
                "Unable to show RewardedAd, the context.findActivity() returned null! " +
                        "Please report an issue https://github.com/miroslavhybler/jet-admob-ads/issues " +
                        "here and provide all the details that can be useful for debugging."
            )
            return false
        }
        val ad = rewardedAd ?: run {
            Log.w("REWARDED_AD", "A show() was called but the rewarded ad wasn't ready yet")
            return false
        }

        rewardedAd = null
        presentingAd = ad
        ad.adEventCallback = object : RewardedAdEventCallback {
            override fun onAdDismissedFullScreenContent() {
                runOnMainThread {
                    releasePresentingAd(ad)
                    if (!isDestroyed) {
                        load(context = context)
                    }
                }
            }

            override fun onAdFailedToShowFullScreenContent(
                fullScreenContentError: FullScreenContentError,
            ) {
                runOnMainThread {
                    releasePresentingAd(ad)
                    if (isDestroyed) return@runOnMainThread

                    Log.e(
                        "JetAdMobAds",
                        "REWARDED_AD onAdFailedToShowFullScreenContent: ${fullScreenContentError.message}"
                    )
                    mAdStatus.value = AdStatus.Failed
                }
            }

            override fun onAdShowedFullScreenContent() {
                runOnMainThread {
                    if (!isDestroyed) {
                        mAdStatus.value = AdStatus.Shown
                    }
                }
            }
        }
        ad.show(
            activity,
            object : OnUserEarnedRewardListener {
                override fun onUserEarnedReward(reward: RewardItem) {
                    runOnMainThread {
                        onUserEarnedReward()
                    }
                }
            },
        )
        return true
    }

    private fun releasePresentingAd(ad: RewardedAd) {
        if (presentingAd === ad) {
            presentingAd = null
            ad.destroy()
        }
    }

    internal fun destroy() {
        runOnMainThread {
            if (isDestroyed) return@runOnMainThread

            isDestroyed = true
            isLoading = false
            rewardedAd?.destroy()
            presentingAd?.destroy()
            rewardedAd = null
            presentingAd = null
        }
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
public fun rememberAdMobRewardedAdState(
    adUnitId: String,
    onUserEarnedReward: () -> Unit,
): AdMobRewardedAdState {
    val context = LocalContext.current
    val currentOnUserEarnedReward = rememberUpdatedState(onUserEarnedReward)
    val rewardedAdState = remember(adUnitId) {
        AdMobRewardedAdState(
            adUnitId = adUnitId,
            onUserEarnedReward = {
                currentOnUserEarnedReward.value()
            },
        )
    }

    LaunchedEffect(key1 = rewardedAdState) {
        rewardedAdState.load(context = context)
    }

    DisposableEffect(key1 = rewardedAdState) {
        onDispose {
            rewardedAdState.destroy()
        }
    }

    return rewardedAdState
}
