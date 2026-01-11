package com.jet.admob.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.jet.admob.example.screens.AdaptiveBannersScreen
import com.jet.admob.example.theme.JetAdMobAdsTheme
import com.jet.admob.example.screens.BannersScreen
import com.jet.admob.example.screens.NativeAdsScreen

/**
 * @author Miroslav Hýbler <br>
 * created on 06.01.2026
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JetAdMobAdsTheme {
                MainNavDisplay()
            }
        }
    }
}
