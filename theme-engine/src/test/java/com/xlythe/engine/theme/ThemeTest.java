package com.xlythe.engine.theme;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ProviderInfo;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowBinder;
import org.robolectric.shadows.ShadowPackageManager;

import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class ThemeTest {
    private final Context context = RuntimeEnvironment.getApplication();

    private void install(String name, Typeface font) {
        PackageInfo info = new PackageInfo();
        info.packageName = name;
        info.applicationInfo = new ApplicationInfo();
        info.applicationInfo.packageName = name;
        info.applicationInfo.nonLocalizedLabel = "Theme label";
        shadowOf(context.getPackageManager()).installPackage(info);
        Resources res = context.getResources();
        ShadowPackageManager.resources.put(name, new Resources(
                res.getAssets(), res.getDisplayMetrics(), res.getConfiguration()) {
            @Override public int getIdentifier(String key, String type, String pkg) {
                return font == null ? 0 : 1;
            }
            @Override public Typeface getFont(int id) { return font; }
        });
    }

    @Test public void fontFollowsFallbackAndItsReplacement() {
        install("theme.primary", null);
        install("theme.first", Typeface.SERIF);
        install("theme.second", Typeface.MONOSPACE);
        String[] themes = {"theme.primary", "theme.first"};
        Theme.setPackageNames(themes);
        assertSame(Typeface.SERIF, Theme.getFont(context, "headline"));
        themes[1] = "theme.second";
        Theme.setPackageNames(themes);
        assertSame(Typeface.MONOSPACE, Theme.getFont(context, "headline"));

        install("theme.second", Typeface.SANS_SERIF);
        context.sendBroadcast(new Intent(Intent.ACTION_PACKAGE_REPLACED, Uri.parse("package:theme.second")));
        shadowOf(Looper.getMainLooper()).idle();
        assertSame(Typeface.SANS_SERIF, Theme.getFont(context, "headline"));
    }

    @Test public void launcherIsOptionalForInstalledTheme() {
        install("theme.data", null);
        App app = App.getApp(context, "theme.data");
        assertEquals("Theme label", app.getName());
        assertNull(app.getIntent(context));
    }

    @Test public void externalContextOnlyLoadsResources() {
        Theme.setPackageName("theme.data");
        Context wrapper = new ContextWrapper(context) {
            @Override public Context createPackageContext(String name, int flags) {
                assertEquals("theme.data", name);
                assertEquals(0, flags);
                return context;
            }
        };
        assertSame(context, Theme.getThemeContext(wrapper));
    }

    @Test public void assetAndMetadataRequireASupportedHost() {
        String host = "example.host";
        int uid = 12345;
        shadowOf(context.getPackageManager()).setPackagesForUid(uid, host);
        ShadowBinder.setCallingUid(uid);
        ProviderInfo info = new ProviderInfo();
        info.authority = "theme.assets";
        FileProvider provider = new FileProvider();
        provider.attachInfo(context, info);
        Uri asset = Uri.parse("content://theme.assets/missing.ttf");
        assertThrows(SecurityException.class, () -> provider.query(asset, null, null, null, null));
        assertThrows(SecurityException.class, () -> provider.openAssetFile(asset, "r"));

        ApplicationInfo theme = context.getApplicationInfo();
        theme.metaData = new Bundle();
        theme.metaData.putBoolean(host + ".THEME", true);
        shadowOf(context.getPackageManager()).installPackage(packageWith(theme));
        assertEquals(1, provider.query(asset, null, null, null, null).getCount());
        assertThrows(java.io.FileNotFoundException.class, () -> provider.openAssetFile(asset, "r"));
    }

    private PackageInfo packageWith(ApplicationInfo app) {
        PackageInfo info = new PackageInfo();
        info.packageName = app.packageName;
        info.applicationInfo = app;
        return info;
    }
}
