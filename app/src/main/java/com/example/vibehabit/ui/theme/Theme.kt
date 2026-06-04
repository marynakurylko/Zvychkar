package com.example.vibehabit.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = NeonPurple, // Головний акцент
    background = BackgroundDark, // Глобальний фон
    surface = SurfaceDark, // Фон для карток (HabitCard)
    surfaceVariant = SurfaceVariantDark, // Фон для полів вводу
    onPrimary = TextPrimary, // Текст на акцентних кнопках
    onBackground = TextPrimary, // Головний текст
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary // Текст-плейсхолдер
)

private val LightColorScheme = lightColorScheme(
    primary = NeonPurple,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = Color(0xFFE0E0E0),
    onPrimary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
    onSurfaceVariant = Color.DarkGray
)

@Composable
fun HabitTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // ВАЖЛИВО: Вимикаємо динамічні кольори, щоб система не перебивала наш неон!
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Фарбуємо системний статус-бар (там, де годинник і батарея) під наш глибокий фон
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Можна буде потім підключити сюди футуристичний шрифт!
        content = content
    )
}