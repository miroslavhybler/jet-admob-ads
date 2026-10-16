# jet-admob-ads

A simple, fast way to use the Google Mobile Ads Next-Gen SDK in Jetpack Compose applications.

> [!WARNING]
> This library is currently in an alpha stage and is highly experimental. While it's safe and policy-compliant, please use it with caution and report any issues you encounter.

This library provides a set of composables that make it easy to display banner and native ads with a high degree of customization, allowing you to seamlessly integrate them into your app's design while adhering to AdMob's policies.

## Prerequisites

Before using this library, you should have a basic understanding of how to work with Google AdMob in Android applications. Please review the official documentation:

- [GMA Next-Gen SDK for Android](https://developers.google.com/admob/android/next-gen/quick-start)
- [Banner Ads](https://developers.google.com/admob/android/next-gen/banner)
- [Native Ads](https://developers.google.com/admob/android/next-gen/native)

Your app must use Android API 24 or newer and compile against Android API 35 or newer.

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
        implementation("com.github.miroslavhybler:jet-admob-ads:2.0.0")
    }
    ```

## Initialization

The Next-Gen SDK must be initialized once, on a worker thread, before any ad state is created.
The application ID is supplied programmatically instead of through the legacy manifest metadata:

```kotlin
var adsReady by mutableStateOf(false)

lifecycleScope.launch(Dispatchers.IO) {
    MobileAds.initialize(
        applicationContext,
        InitializationConfig.Builder("YOUR_ADMOB_APP_ID").build(),
    )

    withContext(Dispatchers.Main) {
        adsReady = true
    }
}
```

Only create `rememberAdMob…State` objects after `adsReady` becomes `true`. If your app uses
mediation, wait for the `MobileAds.initialize` completion callback so adapters are ready too.
Configure consent and request-level privacy options before initialization because the SDK or a
mediation adapter can preload ads during initialization.

If you use the User Messaging Platform SDK, keep the AdMob application ID metadata in your
manifest as required by UMP. Mediation adapters can also pull the legacy SDK transitively; exclude
the `play-services-ads` and `play-services-ads-lite` modules from those integrations.

## Usage

### Banner Ad

Create a banner state with `rememberAdMobBannerState`, then pass it to `AdMobBanner`:

```kotlin
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize

val bannerState = rememberAdMobBannerState(
    adUnitId = "YOUR_AD_UNIT_ID",
    adSize = AdSize.BANNER,
)

AdMobBanner(
    state = bannerState,
    preOccupySpace = true,
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
    adSize = AdMobBannerState.BANNER,
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

Both `rememberAdMobBannerState` and `rememberAdMobNativeAdState` also accept optional
`loadAdRequest` and `adListener` parameters for custom targeting and event observation. Banner
customization uses `BannerAdRequest`, native customization uses `NativeAdRequest`, and events are
reported through `AdMobAdListener`.

For more advanced usage and customization options, please refer to the source code and the example app included in this project.
