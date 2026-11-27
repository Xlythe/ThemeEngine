ThemeEngine
===========

A library for grabbing images, fonts, and colors from another apk.

Where to Download
-----------------
```groovy
dependencies {
  implementation 'com.xlythe:theme-engine:3.0.1'
}
```

### Jetpack Compose

For a Compose app on API 21 or newer, add the Compose artifact. It brings in the matching core library.

```groovy
dependencies {
  implementation 'com.xlythe:theme-engine-compose:3.0.1'
}
```

Wrap the screen once and map your host resource IDs to Material 3 roles. ThemeEngine resolves each ID by name through the configured theme APKs, then the host. Other roles inherit the surrounding Material theme.

```kotlin
ProvideThemeEngineTheme(
    colorScheme = { base -> base.copy(primary = color(R.color.brand)) },
    typography = { base ->
        base.copy(bodyLarge = base.bodyLarge.copy(fontFamily = fontFamily("body") ?: base.bodyLarge.fontFamily))
    }
) {
    val inset = LocalThemeResources.current.dimension(R.dimen.content_inset)
    Text("Now playing", Modifier.padding(inset), color = MaterialTheme.colorScheme.primary)
}
```

Use the existing `Theme.setPackageNames(...)` API on the main thread to change the selection. The provider updates when a selected package changes, when the configuration changes, or when the app sets a new default font. The screen keeps its remembered state. Data-only themes do not need Compose dependencies.

How to Use
-----------------
As of Android 11 (API 30), the main app must add this to their AndroidManifest.
```xml
<manifest package="com.example.myapp">
    <queries>
        <intent>
            <action android:name="com.example.myapp.THEME" /> <!-- Change com.example.myapp to your main app's package name -->
        </intent>
    </queries>
</manifest>
```

Save the selected theme package name in host preferences and restore it before inflating views or composing the first screen on each process start, including after a host APK update.
```java
Theme.setPackageName(MY_THEME_PACKAGE_NAME);
```

When a selected theme APK is updated, installed or removed, ThemeEngine automatically recreates Activities that inflated a themed widget or used `ThemedWidgets.apply`. Stopped Activities refresh on their next start. Compose-only screens recompose in place. For custom rendering that reads resources directly, register a callback with `Theme.addChangeListener` and remove it when the owner is destroyed.

Then you can either automatically grab the images and colors via ThemedView in xml or programmaically set them in java.
```xml
<com.xlythe.engine.theme.ThemedTextView xmlns:theme="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    theme:themedTextColor="@color/text_color" />
```
or
```java
TextView tv = new TextView(this);
tv.setTextColor(Theme.getColor(this, R.color.text_color));
```

The library also includes a ThemePreference. This preference will query for all installed themes and let the user select one. You can then set Theme.setPackageName via the PreferenceManager. A good default is your own package name.


As for the theme apk, it's job is simple. It requires no java code. For starters, add the following intent filter to any activity in the Activity Manifest.
```xml
<intent-filter>
    <action android:name="com.example.myapp.THEME" /> <!-- Change com.example.myapp to your main app's package name -->
</intent-filter>
```
Then add in any drawables and colors you want to replace. Just name them the same as they are in the main app. For custom fonts, add `font.ttf` or `font.otf` to the theme’s assets. The core library registers its asset provider automatically at `${applicationId}.FileProvider`. External callers can read assets only when the theme declares support for their package.

### XML attribute names

Every widget attribute in the `res-auto` namespace accepts both spellings. For example, `app:button` and `app:themedButton` select the same themed drawable. The same rule applies in styles and to `ThemedWidgets.apply`. When both spellings resolve to a value, the prefixed one wins.

Use resource references with the short names, such as `app:textColor="@color/title"` and `app:font="@font/body"`. AndroidX declares `app:font` as a reference. For an asset font name, use `app:font="@string/body_font_name"` with a string whose value is `body`, or use `app:themedFont="body"` directly.

### Framework widgets

Custom framework views can call `ThemedWidgets.apply(this, attrs, defStyleAttr, defStyleRes)` after construction. It applies common background properties and the themed properties supported by the widget family.
