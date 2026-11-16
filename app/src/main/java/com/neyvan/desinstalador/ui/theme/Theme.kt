package com.neyvan.desinstalador.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Paleta Oled / Dark Material 3 equilibrada
val Ink = Color(0xFF0F1117)             // Fundo profundo otimizado para OLED
val Panel = Color(0xFF1A1F2C)           // Superfície de cards e containers
val PanelElevated = Color(0xFF22293A)   // Superfície com elevação sutil / barra inferior
val Amber = Color(0xFFF5A623)           // Âmbar dourado vibrante e legível
val AmberContainer = Color(0xFF33220A)  // Fundo sutil para chips/destaques
val Cream = Color(0xFFF3EFEA)           // Texto primário de alto contraste (não estourado)
val Muted = Color(0xFFA6ABB8)           // Texto secundário legível em telas AMOLED
val OutlineBorder = Color(0xFF2A3245)   // Bordas e divisores sutis
val Danger = Color(0xFFFF5252)          // Cor de alerta / exclusão

private val ColorScheme = darkColorScheme(
    primary = Amber,
    onPrimary = Color(0xFF1C1304),
    primaryContainer = AmberContainer,
    onPrimaryContainer = Amber,
    secondary = Panel,
    onSecondary = Cream,
    background = Ink,
    onBackground = Cream,
    surface = Ink,
    onSurface = Cream,
    surfaceVariant = Panel,
    onSurfaceVariant = Muted,
    outline = OutlineBorder,
    error = Danger,
    onError = Color.White
)

@Composable
fun DesinstaladorTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ColorScheme,
        typography = DesinstaladorTypography,
        content = content
    )
}
