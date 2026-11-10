package com.aserdipanda.core.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Synapse brand colors: Material 3 tonal palettes generated from seed #0B8A80.
// Teal is the signal and amber (tertiary) is the incoming SMS. On Android 12 and later the
// app uses dynamic color by default, so these schemes are the fallback (see SynapseAppTheme).
//
// The design lists every role except the tone 10 "on fixed" colors, the light surfaceDim
// (neutral tone 87) and the dark surfaceBright (neutral tone 24). Those are derived from
// the same palettes so that no role falls back to Material's baseline purple.

// Fixed roles keep the same value in light and dark themes.
private val PrimaryFixed = Color(0xFF97F3E7)
private val PrimaryFixedDim = Color(0xFF7BD6CB)
private val OnPrimaryFixed = Color(0xFF00201D)
private val OnPrimaryFixedVariant = Color(0xFF00504A)
private val SecondaryFixed = Color(0xFFCCE8E3)
private val SecondaryFixedDim = Color(0xFFB1CCC7)
private val OnSecondaryFixed = Color(0xFF061F1D)
private val OnSecondaryFixedVariant = Color(0xFF324B48)
private val TertiaryFixed = Color(0xFFFFDDB8)
private val TertiaryFixedDim = Color(0xFFFFB960)
private val OnTertiaryFixed = Color(0xFF291800)
private val OnTertiaryFixedVariant = Color(0xFF653E00)

internal val SynapseLightColors = lightColorScheme(
    primary = Color(0xFF006A62),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF97F3E7),
    onPrimaryContainer = Color(0xFF00504A),
    inversePrimary = Color(0xFF7BD6CB),
    secondary = Color(0xFF4A6360),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCCE8E3),
    onSecondaryContainer = Color(0xFF324B48),
    tertiary = Color(0xFF865300),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDDB8),
    onTertiaryContainer = Color(0xFF653E00),
    background = Color(0xFFF7FAF8),
    onBackground = Color(0xFF191C1C),
    surface = Color(0xFFF7FAF8),
    onSurface = Color(0xFF191C1C),
    surfaceVariant = Color(0xFFDAE5E2),
    onSurfaceVariant = Color(0xFF3F4947),
    inverseSurface = Color(0xFF2D3130),
    inverseOnSurface = Color(0xFFEFF1F0),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A),
    outline = Color(0xFF6F7977),
    outlineVariant = Color(0xFFBEC9C6),
    surfaceBright = Color(0xFFF7FAF8),
    surfaceDim = Color(0xFFD8DBD9),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF2F4F2),
    surfaceContainer = Color(0xFFECEEED),
    surfaceContainerHigh = Color(0xFFE6E9E7),
    surfaceContainerHighest = Color(0xFFE0E3E1),
    primaryFixed = PrimaryFixed,
    primaryFixedDim = PrimaryFixedDim,
    onPrimaryFixed = OnPrimaryFixed,
    onPrimaryFixedVariant = OnPrimaryFixedVariant,
    secondaryFixed = SecondaryFixed,
    secondaryFixedDim = SecondaryFixedDim,
    onSecondaryFixed = OnSecondaryFixed,
    onSecondaryFixedVariant = OnSecondaryFixedVariant,
    tertiaryFixed = TertiaryFixed,
    tertiaryFixedDim = TertiaryFixedDim,
    onTertiaryFixed = OnTertiaryFixed,
    onTertiaryFixedVariant = OnTertiaryFixedVariant,
)

internal val SynapseDarkColors = darkColorScheme(
    primary = Color(0xFF7BD6CB),
    onPrimary = Color(0xFF003733),
    primaryContainer = Color(0xFF00504A),
    onPrimaryContainer = Color(0xFF97F3E7),
    inversePrimary = Color(0xFF006A62),
    secondary = Color(0xFFB1CCC7),
    onSecondary = Color(0xFF1C3532),
    secondaryContainer = Color(0xFF324B48),
    onSecondaryContainer = Color(0xFFCCE8E3),
    tertiary = Color(0xFFFFB960),
    onTertiary = Color(0xFF472A00),
    tertiaryContainer = Color(0xFF653E00),
    onTertiaryContainer = Color(0xFFFFDDB8),
    background = Color(0xFF101413),
    onBackground = Color(0xFFE0E3E1),
    surface = Color(0xFF101413),
    onSurface = Color(0xFFE0E3E1),
    surfaceVariant = Color(0xFF3F4947),
    onSurfaceVariant = Color(0xFFBEC9C6),
    inverseSurface = Color(0xFFE0E3E1),
    inverseOnSurface = Color(0xFF2D3130),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(0xFF899391),
    outlineVariant = Color(0xFF3F4947),
    surfaceBright = Color(0xFF373A3A),
    surfaceDim = Color(0xFF101413),
    surfaceContainerLowest = Color(0xFF0B0F0E),
    surfaceContainerLow = Color(0xFF191C1C),
    surfaceContainer = Color(0xFF1D2020),
    surfaceContainerHigh = Color(0xFF272B2A),
    surfaceContainerHighest = Color(0xFF323535),
    primaryFixed = PrimaryFixed,
    primaryFixedDim = PrimaryFixedDim,
    onPrimaryFixed = OnPrimaryFixed,
    onPrimaryFixedVariant = OnPrimaryFixedVariant,
    secondaryFixed = SecondaryFixed,
    secondaryFixedDim = SecondaryFixedDim,
    onSecondaryFixed = OnSecondaryFixed,
    onSecondaryFixedVariant = OnSecondaryFixedVariant,
    tertiaryFixed = TertiaryFixed,
    tertiaryFixedDim = TertiaryFixedDim,
    onTertiaryFixed = OnTertiaryFixed,
    onTertiaryFixedVariant = OnTertiaryFixedVariant,
)
