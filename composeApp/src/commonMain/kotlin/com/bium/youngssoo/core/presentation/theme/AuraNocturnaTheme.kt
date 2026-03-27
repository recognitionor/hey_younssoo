package com.bium.youngssoo.core.presentation.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val AuraPrimary = Color(0xFF89ACFF)
val AuraOnPrimary = Color(0xFF002B6A)
val AuraPrimaryContainer = Color(0xFF739EFF)
val AuraOnPrimaryContainer = Color(0xFF002053)
val AuraSecondary = Color(0xFFA68CFF)
val AuraOnSecondary = Color(0xFF25006B)
val AuraSecondaryContainer = Color(0xFF591ADC)
val AuraOnSecondaryContainer = Color(0xFFE4DAFF)
val AuraTertiary = Color(0xFFFFE792)
val AuraOnTertiary = Color(0xFF655400)
val AuraTertiaryContainer = Color(0xFFFFD709)
val AuraOnTertiaryContainer = Color(0xFF5B4B00)
val AuraError = Color(0xFFFF6E84)
val AuraOnError = Color(0xFF490013)
val AuraErrorContainer = Color(0xFFA70138)
val AuraOnErrorContainer = Color(0xFFFFB2B9)
val AuraBackground = Color(0xFF0D0D15)
val AuraOnBackground = Color(0xFFEFECF8)
val AuraSurface = Color(0xFF0D0D15)
val AuraOnSurface = Color(0xFFEFECF8)
val AuraSurfaceVariant = Color(0xFF252530)
val AuraOnSurfaceVariant = Color(0xFFACAAB5)
val AuraOutline = Color(0xFF76747F)
val AuraOutlineVariant = Color(0xFF484750)

val AuraNocturnaColorScheme = darkColorScheme(
    primary = AuraPrimary,
    onPrimary = AuraOnPrimary,
    primaryContainer = AuraPrimaryContainer,
    onPrimaryContainer = AuraOnPrimaryContainer,
    secondary = AuraSecondary,
    onSecondary = AuraOnSecondary,
    secondaryContainer = AuraSecondaryContainer,
    onSecondaryContainer = AuraOnSecondaryContainer,
    tertiary = AuraTertiary,
    onTertiary = AuraOnTertiary,
    tertiaryContainer = AuraTertiaryContainer,
    onTertiaryContainer = AuraOnTertiaryContainer,
    error = AuraError,
    onError = AuraOnError,
    errorContainer = AuraErrorContainer,
    onErrorContainer = AuraOnErrorContainer,
    background = AuraBackground,
    onBackground = AuraOnBackground,
    surface = AuraSurface,
    onSurface = AuraOnSurface,
    surfaceVariant = AuraSurfaceVariant,
    onSurfaceVariant = AuraOnSurfaceVariant,
    outline = AuraOutline,
    outlineVariant = AuraOutlineVariant
)

@Composable
fun AuraNocturnaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AuraNocturnaColorScheme,
        content = content
    )
}
