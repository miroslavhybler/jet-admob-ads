package com.jet.admob.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

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
