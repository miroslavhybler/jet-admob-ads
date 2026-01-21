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
        implementation("com.github.miroslavhybler:jet-admob-ads:1.1.0-alpha01")
    }
    ```

## Usage

### Banner Ad

To display a simple banner ad, use the `AdMobBanner` composable:

```kotlin
AdMobBanner(adUnitId = "YOUR_AD_UNIT_ID")
```

### Native Ad

To display a native ad, use the `AdMobNative` composable. You can choose from different formats and customize the colors and shapes to match your app's theme.

```kotlin
AdMobNative(
    adUnitId = "YOUR_AD_UNIT_ID",
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

For more advanced usage and customization options, please refer to the source code and the example app included in this project.
