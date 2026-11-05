# jet-admob-ads

A simple, fast way to use Google AdMob ads in your Jetpack Compose applications.

> [!WARNING]
> This library is currently in an alpha stage and is highly experimental. While it's safe and policy-compliant, please use it with caution and report any issues you encounter.

This library provides a set of composables that make it easy to display banner and native ads with a high degree of customization, allowing you to seamlessly integrate them into your app's design while adhering to AdMob's policies.

## Prerequisites

Before using this library, you should have a basic understanding of how to work with Google AdMob in Android applications. Please review the official documentation:

- [Google Mobile Ads SDK for Android](https://developers.google.com/admob/android/quick-start)
- [Banner Ads](https://developers.google.com/admob/android/banner)
- [Native Ads](https://developers.google.com/admob/android/native/start)

## Installation

To add this library to your project, follow these steps:

1.  Add the JitPack repository to your `settings.gradle.kts` file:

    ```kotlin
    dependencyResolutionManagement {
        repositories {
            maven(url = "https://jitpack.io")
        }
    }
    ```

2.  Add the dependency to your app's `build.gradle.kts` file:

    ```kotlin
    dependencies {
        implementation("com.github.miroslavhybler:jet-admob-ads:1.1.0-beta02")
    }
    ```

## Usage

### Banner Ad

Create a banner state with `rememberAdMobBannerState`, using the library-owned `BannerAdSize`, then
pass it to `AdMobBanner`:

```kotlin
val bannerState = rememberAdMobBannerState(
    adUnitId = "YOUR_AD_UNIT_ID",
    adSize = BannerAdSize.BANNER,
)

AdMobBanner(
    state = bannerState,
    preOccupySpace = true,
)
```

`BannerAdSize` also provides fixed, custom, inline adaptive, and large anchored adaptive banner
sizes. It does not expose the underlying Google Mobile Ads SDK type, allowing the implementation to
move to the Next-Gen SDK without changing banner size usage.

Common targeting and lifecycle callbacks are also SDK-independent:

```kotlin
val bannerState = rememberAdMobBannerState(
    adUnitId = "YOUR_AD_UNIT_ID",
    adSize = BannerAdSize.BANNER,
    requestConfig = AdRequestConfig(
        keywords = setOf("travel", "outdoors"),
        contentUrl = "https://example.com/article",
    ),
    eventListener = object : AdEventListener() {
        override fun onAdFailedToLoad(error: AdErrorInfo) {
            Log.w("Ads", error.message)
        }
    },
)
```

### Native Ad

Create a native ad state with `rememberAdMobNativeAdState`, then pass it to `AdMobNative`.
You can choose from different formats and customize the colors and shapes to match your app's theme.

```kotlin
val nativeAdState = rememberAdMobNativeAdState(
    adUnitId = "YOUR_AD_UNIT_ID",
)

AdMobNative(
    state = nativeAdState,
    adFormat = NativeAdFormat.Medium,
    shape = RoundedCornerShape(12.dp),
    colors = NativeAdColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        buttonColor = MaterialTheme.colorScheme.primary,
        buttonTextColor = MaterialTheme.colorScheme.onPrimary
    )
)
```

### Ads in Lazy Lists

When placing ads inside `LazyColumn` or `LazyRow`, hoist the ad state outside the lazy item.
This keeps the loaded ad alive when Compose temporarily disposes off-screen list items.

```kotlin
val bannerState = rememberAdMobBannerState(
    adUnitId = "YOUR_BANNER_AD_UNIT_ID",
    adSize = BannerAdSize.BANNER,
)
val nativeAdState = rememberAdMobNativeAdState(
    adUnitId = "YOUR_NATIVE_AD_UNIT_ID",
)

LazyColumn {
    items(contentItems) { item ->
        Text(text = item.title)
    }

    item(key = "banner-ad") {
        AdMobBanner(
            state = bannerState,
            preOccupySpace = true,
        )
    }

    items(moreContentItems) { item ->
        Text(text = item.title)
    }

    item(key = "native-ad") {
        AdMobNative(
            state = nativeAdState,
            adFormat = NativeAdFormat.Medium,
        )
    }
}
```

The recommended APIs use `BannerAdSize`, `AdRequestConfig`, `AdEventListener`, and `AdErrorInfo`,
so application code does not need to import Google Mobile Ads SDK classes. Overloads accepting
legacy `AdSize`, `AdRequest`, and `AdListener` types remain available for migration but are
deprecated and will be removed in 2.0.0.

For more advanced usage and customization options, refer to the source code and example app.
