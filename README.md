ThemeEngine
===========

A library for grabbing images, fonts, and colors from another apk.

Where to Download
-----------------
```groovy
dependencies {
  implementation 'com.xlythe:theme-engine:1.1'
}
```

How to Use
-----------------
The main app must make the following call before setContentView. This is only needed in the very first Activity.
```java
Theme.setPackageName(MY_THEME_PACKAGE_NAME);
```

Then you can either automatically grab the images and colors via ThemedView in xml or programmaically set them in java.
```xml
<com.xlythe.engine.theme.ThemedTextView xmlns:theme="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    theme:textColor="@color/text_color" />
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
Then add in any drawables and colors you want to replace. Just name them the same as they are in the main app. For custom fonts, add a font file called "font" to /assets.
