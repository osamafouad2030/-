package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = GoldenAureolin,
    secondary = LightEmerald,
    tertiary = IvoryWhite,
    background = DarkBackground,
    surface = SoftCardDark,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color(0xFFF5EFE0),
    onSurface = Color(0xFFF5EFE0)
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldGreen,
    secondary = GoldenAureolin,
    tertiary = DeepNavy,
    background = IvoryWhite,
    surface = SoftCardLight,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = DeepNavy,
    onSurface = DeepNavy
)

@Composable
fun MishkatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
