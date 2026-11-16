package com.neyvan.desinstalador

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.neyvan.desinstalador.ui.AppSplashScreen
import com.neyvan.desinstalador.ui.UninstallerRoute
import com.neyvan.desinstalador.ui.theme.DesinstaladorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Hardening de Segurança: Proteção contra Tapjacking / Overlay Attacks
        window.decorView.filterTouchesWhenObscured = true

        enableEdgeToEdge()
        setContent {
            DesinstaladorTheme {
                var showSplash by remember { mutableStateOf(true) }

                Crossfade(
                    targetState = showSplash,
                    animationSpec = tween(durationMillis = 400),
                    label = "SplashCrossfade"
                ) { isSplash ->
                    if (isSplash) {
                        AppSplashScreen(
                            onAnimationFinished = { showSplash = false }
                        )
                    } else {
                        UninstallerRoute()
                    }
                }
            }
        }
    }
}
