package com.xlythe.engine.theme.compose

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Typeface
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.unit.dp
import com.xlythe.engine.theme.Theme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowPackageManager

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ThemeEngineThemeTest {
    @get:Rule val ui = createAndroidComposeRule<ComponentActivity>()

    // Give each installed theme its own resource table without installing APKs on the test JVM.
    private fun install(context: Context, pkg: String, color: Int?, font: Typeface?) {
        val info = PackageInfo().apply {
            packageName = pkg
            applicationInfo = ApplicationInfo().apply { packageName = pkg }
        }
        shadowOf(context.packageManager).installPackage(info)
        val res = context.resources
        ShadowPackageManager.resources[pkg] = object : Resources(res.assets, res.displayMetrics, res.configuration) {
            override fun getIdentifier(name: String, type: String, defPackage: String) = when {
                name == "test_brand" && type == "color" && color != null -> 1
                name == "body" && type == "font" && font != null -> 2
                else -> 0
            }
            override fun getColor(id: Int) = requireNotNull(color)
            override fun getValue(id: Int, outValue: android.util.TypedValue, resolveRefs: Boolean) {
                outValue.string = null
            }
            override fun getFont(id: Int) = requireNotNull(font)
        }
    }

    @Test fun configurationChangesRefreshDimensionsInPlace() {
        val activity = ui.activity
        var night by mutableStateOf(false)
        ui.setContent {
            val configuration = Configuration(activity.resources.configuration).apply {
                uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or
                    if (night) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO
            }
            val context = remember(night) { activity.createConfigurationContext(configuration) }
            CompositionLocalProvider(LocalContext provides context, LocalConfiguration provides configuration) {
                ProvideThemeEngineTheme {
                    var clicks by remember { mutableStateOf(0) }
                    val id = context.resources.getIdentifier("test_inset", "dimen", context.packageName)
                    val inset = LocalThemeResources.current.dimension(id)
                    Text("Inset ${inset.value.toInt()} clicks $clicks", Modifier.clickable { clicks++ })
                }
            }
        }
        ui.onNodeWithText("Inset 12 clicks 0").performClick()
        ui.runOnIdle { night = true }
        ui.onNodeWithText("Inset 24 clicks 1").assertExists()
    }

    @Test fun switchingFallbackAndReplacingPackageUpdateLiveMaterialContent() {
        val context = ui.activity
        val brand = context.resources.getIdentifier("test_brand", "color", context.packageName)
        val inset = context.resources.getIdentifier("test_inset", "dimen", context.packageName)
        val red = android.graphics.Color.RED
        val blue = android.graphics.Color.BLUE
        ui.runOnUiThread {
            install(context, "theme.primary", null, null)
            install(context, "theme.first", red, Typeface.SERIF)
            install(context, "theme.second", blue, Typeface.MONOSPACE)
            Theme.setPackageNames("theme.primary", "theme.first")
        }

        var renderedColor = Color.Transparent
        var renderedFont: Any? = null
        var renderedInset = 0.dp
        ui.setContent {
            ProvideThemeEngineTheme(
                colorScheme = { it.copy(primary = color(brand)) },
                typography = { it.copy(bodyLarge = it.bodyLarge.copy(fontFamily = fontFamily("body"))) }
            ) {
                var count by remember { mutableStateOf(0) }
                renderedColor = MaterialTheme.colorScheme.primary
                renderedFont = LocalFontFamilyResolver.current.resolve(MaterialTheme.typography.bodyLarge.fontFamily).value
                renderedInset = LocalThemeResources.current.dimension(inset)
                Text("Count $count", Modifier.clickable { count++ }, color = renderedColor)
            }
        }

        ui.onNodeWithText("Count 0").performClick()
        val firstFont = ui.runOnIdle {
            assertEquals(Color(red), renderedColor)
            assertEquals(12.dp, renderedInset)
            assertEquals(Typeface.SERIF, renderedFont)
            renderedFont
        }
        ui.runOnIdle { Theme.setPackageNames("theme.primary", "theme.second") }
        ui.onNodeWithText("Count 1").assertExists()
        ui.runOnIdle {
            assertEquals(Color(blue), renderedColor)
            assertEquals(Typeface.MONOSPACE, renderedFont)
            install(context, "theme.second", red, Typeface.SERIF)
            context.sendBroadcast(Intent(Intent.ACTION_PACKAGE_REPLACED, Uri.parse("package:theme.second")))
        }
        ui.waitForIdle()
        ui.runOnIdle {
            assertEquals(Color(red), renderedColor)
            assertEquals(firstFont, renderedFont)
        }
        ui.onNodeWithText("Count 1").assertExists()
    }
}
