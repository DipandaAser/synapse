package com.aserdipanda.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Synapse's Material 3 theme.
 *
 * On Android 12 and later the colors follow the wallpaper (dynamic color) unless
 * [dynamicColor] is false. Everywhere else the teal brand scheme applies.
 * The system bars are set up by `enableEdgeToEdge()` in the activity.
 */
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
        darkTheme -> SynapseDarkColors
        else -> SynapseLightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SynapseTypography,
        content = content
    )
}
