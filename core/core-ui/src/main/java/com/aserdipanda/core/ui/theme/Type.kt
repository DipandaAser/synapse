package com.aserdipanda.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.DeviceFontFamilyName
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.aserdipanda.synapse.core.ui.R

// Fonts are downloaded through Google Play services, so the APK ships none. Until a font is
// available, and on devices without Play services, text falls back to the system font, and
// mono text to the system's monospace font.
private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private val robotoFlex = GoogleFont("Roboto Flex")
private val robotoMono = GoogleFont("Roboto Mono")

/** The app's text font. */
val RobotoFlex = FontFamily(
    Font(googleFont = robotoFlex, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = robotoFlex, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = robotoFlex, fontProvider = provider, weight = FontWeight.Bold)
)

/** For URLs, JSON and rule values. */
val RobotoMono = FontFamily(
    Font(googleFont = robotoMono, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = robotoMono, fontProvider = provider, weight = FontWeight.Medium),
    // Used while Roboto Mono downloads, or if it can't: values must stay monospaced.
    Font(familyName = DeviceFontFamilyName("monospace"), weight = FontWeight.Normal),
    Font(familyName = DeviceFontFamilyName("monospace"), weight = FontWeight.Medium)
)

private val baseline = Typography()

/** The Material 3 type scale, set in Roboto Flex. */
val SynapseTypography = Typography(
    displayLarge = baseline.displayLarge.copy(fontFamily = RobotoFlex),
    displayMedium = baseline.displayMedium.copy(fontFamily = RobotoFlex),
    displaySmall = baseline.displaySmall.copy(fontFamily = RobotoFlex),
    headlineLarge = baseline.headlineLarge.copy(fontFamily = RobotoFlex),
    headlineMedium = baseline.headlineMedium.copy(fontFamily = RobotoFlex),
    headlineSmall = baseline.headlineSmall.copy(fontFamily = RobotoFlex),
    titleLarge = baseline.titleLarge.copy(fontFamily = RobotoFlex),
    titleMedium = baseline.titleMedium.copy(fontFamily = RobotoFlex),
    titleSmall = baseline.titleSmall.copy(fontFamily = RobotoFlex),
    bodyLarge = baseline.bodyLarge.copy(fontFamily = RobotoFlex),
    bodyMedium = baseline.bodyMedium.copy(fontFamily = RobotoFlex),
    bodySmall = baseline.bodySmall.copy(fontFamily = RobotoFlex),
    labelLarge = baseline.labelLarge.copy(fontFamily = RobotoFlex),
    labelMedium = baseline.labelMedium.copy(fontFamily = RobotoFlex),
    labelSmall = baseline.labelSmall.copy(fontFamily = RobotoFlex)
)

/** Roboto Mono 13/20 for URLs, JSON and rule values: `MaterialTheme.typography.mono`. */
val Typography.mono: TextStyle
    get() = bodyMedium.copy(
        fontFamily = RobotoMono,
        fontSize = 13.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    )
