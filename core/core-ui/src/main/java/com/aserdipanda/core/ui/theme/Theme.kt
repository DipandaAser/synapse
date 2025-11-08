// In ui/theme/Theme.kt
package com.aserdipanda.core.ui.theme

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat // Make sure this import is present

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

@Composable
fun SynapseAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
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
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // --- CRUCIAL LINE FOR STATUS BAR ICONS ---
            // This line tells the system whether to use light or dark icons/text in the status bar.
            // true = dark icons (for a light status bar background)
            // false = light icons (for a dark status bar background)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme

            // OPTIONAL: If you want a specific status bar color *other than* the default system one,
            // you can uncomment and set it here. For a simple app, often the system default
            // (transparent/translucent, handled by WindowCompat.setDecorFitsSystemWindows) is fine.
            // If you uncomment, make sure this color contrasts with the icons set above.
            // window.statusBarColor = colorScheme.primary.toArgb() // This line caused the warning.

            // Ensure content is drawn behind the system bars
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}