package com.jet.admob.example

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.jet.admob.example.screens.AdaptiveBannersScreen
import com.jet.admob.example.screens.BannersScreen
import com.jet.admob.example.screens.HomeScreen
import com.jet.admob.example.screens.NativeAdFullScreenScreen
import com.jet.admob.example.screens.NativeAdsScreen
import kotlinx.serialization.Serializable


/**
 * @author Miroslav Hýbler <br>
 * created on 07.01.2026
 */
@Composable
fun MainNavDisplay() {
    val backStack = rememberNavBackStack(Route.Home)
    NavDisplay(
        backStack = backStack,
        entryProvider = entryProvider(
            builder = {
                entry<Route.Home> {
                    HomeScreen(
                        onNavigate = { route ->
                            backStack += route
                        }
                    )
                }

                entry<Route.Banners> {
                    BannersScreen()
                }

                entry<Route.AdaptiveBanners> {
                    AdaptiveBannersScreen()
                }

                entry<Route.NativeAds> {
                    NativeAdsScreen()
                }
                entry<Route.NativeAdFullScreen> {
                    NativeAdFullScreenScreen()
                }
            }
        )
    )
}


@Serializable
sealed class Route : NavKey {

    @Serializable
    object Home : Route()

    @Serializable
    object Banners : Route()

    @Serializable
    object AdaptiveBanners : Route()

    @Serializable
    object NativeAds : Route()

    @Serializable
    object NativeAdFullScreen : Route()

}