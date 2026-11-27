package com.xlythe.engine.theme.compose

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.FontRes
import androidx.annotation.UiThread
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.xlythe.engine.theme.Theme

/** Resolves host resource IDs through the selected APKs in fallback order. */
@UiThread
class ThemeResources internal constructor(private val context: Context, private val density: Density) {
    fun color(@ColorRes id: Int): Color = Color(Theme.getColor(context, id))
    fun dimension(@DimenRes id: Int): Dp = with(density) { Theme.getDimen(context, id).toDp() }
    fun textSize(@DimenRes id: Int): TextUnit = with(density) { Theme.getDimen(context, id).toSp() }
    fun fontFamily(@FontRes id: Int): FontFamily? = Theme.getFont(context, id)?.let(::FontFamily)
    fun fontFamily(name: String = "font"): FontFamily? = Theme.getFont(context, name)?.let(::FontFamily)
}

val LocalThemeResources = compositionLocalOf<ThemeResources> {
    error("Use LocalThemeResources inside ProvideThemeEngineTheme")
}

/**
 * Provides external theme resources to this subtree. The host maps its named resources onto
 * Material roles. Leaving a role unspecified preserves the surrounding Material theme.
 * Selection, package and configuration changes update readers while preserving remembered UI state.
 */
@Composable
fun ProvideThemeEngineTheme(
    colorScheme: ThemeResources.(ColorScheme) -> ColorScheme = { it },
    typography: ThemeResources.(Typography) -> Typography = { it },
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val configuration = Configuration(LocalConfiguration.current)
    val density = LocalDensity.current
    var revision by remember { mutableStateOf(Theme.getRevision()) }
    DisposableEffect(Unit) {
        val listener = Runnable { revision = Theme.getRevision() }
        Theme.addChangeListener(listener)
        listener.run()
        onDispose { Theme.removeChangeListener(listener) }
    }
    val resources = remember(context, configuration, density, revision) { ThemeResources(context, density) }
    CompositionLocalProvider(LocalThemeResources provides resources) {
        MaterialTheme(
            colorScheme = resources.colorScheme(MaterialTheme.colorScheme),
            typography = resources.typography(MaterialTheme.typography),
            shapes = MaterialTheme.shapes,
            content = content
        )
    }
}
