package com.jet.admob

/**
 * Represents the status of an ad.
 * @since 1.0.0
 */
sealed interface AdStatus {

    /**
     * The ad is currently loading.
     * @since 1.0.0
     */
    object Loading : AdStatus

    /**
     * The ad has been successfully loaded.
     * @since 1.0.0
     */
    object Loaded : AdStatus

    /**
     * The ad has been successfully shown to the user.
     * @since 1.0.0
     */
    object Shown : AdStatus

    /**
     * The ad failed to load or show.
     * @since 1.0.0
     */
    object Failed : AdStatus
}
