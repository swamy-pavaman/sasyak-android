package com.kapilagro.sasyak.presentation.common.theme


// Light and Dark Color Schemes
import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = AgroPrimary,
    onPrimary = White, // Use the defined White
    primaryContainer = AgroLight,
    onPrimaryContainer = AgroDark,
    secondary = AgroSecondary,
    onSecondary = White,
    secondaryContainer = AgroMuted,
    onSecondaryContainer = AgroDark,
    tertiary = AgroAccent,
    onTertiary = White, // Updated for better contrast on Sky Blue
    background = Background,
    onBackground = Foreground,
    surface = Card,
    onSurface = Foreground,
    surfaceVariant = AgroMuted,
    onSurfaceVariant = AgroMutedForeground,
    outline = Border,
    error = Error,
    errorContainer = Color(0xFFFFDAD6), // Use Compose Color
    onError = White,
    onErrorContainer = Color(0xFF410002)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF81C784), // Lighter Green for Dark Mode
    onPrimary = White,
    primaryContainer = AgroDark,
    onPrimaryContainer = Color(0xFF81C784),
    secondary = AgroSecondary,
    onSecondary = White,
    secondaryContainer = Color(0xFF5A4037), // Darker Soil Brown
    onSecondaryContainer = Color(0xFFE5C9B8),
    tertiary = Color(0xFF29B6F6), // Lighter Sky Blue for Dark Mode
    onTertiary = White,
    background = BackgroundDark,
    onBackground = Color(0xFFFAFAFA), // Off-White
    surface = SurfaceDark,
    onSurface = Color(0xFFFAFAFA),
    surfaceVariant = Color(0xFF424242),
    onSurfaceVariant = Color(0xFFBFBFBF),
    outline = Color(0xFF707070),
    error = Error,
    errorContainer = Color(0xFF93000A),
    onError = White,
    onErrorContainer = Color(0xFFFFDAD6)
)

@Composable
fun SasyakTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
