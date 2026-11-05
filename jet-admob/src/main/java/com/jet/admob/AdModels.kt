package com.jet.admob

import com.google.android.gms.ads.AdError as GoogleAdError
import com.google.android.gms.ads.AdRequest as GoogleAdRequest


/**
 * SDK-independent targeting options shared by ad request types.
 *
 * @since 1.1.0
 */
data class AdRequestConfig(
    val keywords: Set<String> = emptySet(),
    val contentUrl: String? = null,
    val neighboringContentUrls: Set<String> = emptySet(),
)


/**
 * SDK-independent details about an ad loading or presentation failure.
 *
 * @since 1.1.0
 */
data class AdErrorInfo(
    val code: Int,
    val domain: String,
    val message: String,
    val cause: AdErrorInfo? = null,
)


/**
 * SDK-independent listener for common ad lifecycle events.
 *
 * @since 1.1.0
 */
open class AdEventListener {
    open fun onAdClicked() = Unit
    open fun onAdClosed() = Unit
    open fun onAdFailedToLoad(error: AdErrorInfo) = Unit
    open fun onAdImpression() = Unit
    open fun onAdLoaded() = Unit
    open fun onAdOpened() = Unit
}


internal fun AdRequestConfig.toAdRequest(): GoogleAdRequest {
    val builder = GoogleAdRequest.Builder()
    keywords.forEach(builder::addKeyword)
    contentUrl?.let(builder::setContentUrl)
    if (neighboringContentUrls.isNotEmpty()) {
        builder.setNeighboringContentUrls(neighboringContentUrls.toList())
    }
    return builder.build()
}


internal fun GoogleAdError.toAdErrorInfo(): AdErrorInfo = AdErrorInfo(
    code = code,
    domain = domain,
    message = message,
    cause = cause?.toAdErrorInfo(),
)
