package com.xlythe.engine.theme;

import android.app.Activity;
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
import android.view.ContextThemeWrapper;
import android.view.View;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
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
            @Override public void getValue(int id, android.util.TypedValue value, boolean resolveRefs) {
                value.string = null;
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
        long revision = Theme.getRevision();
        for (String action : new String[]{Intent.ACTION_PACKAGE_REMOVED, Intent.ACTION_PACKAGE_ADDED}) {
            context.sendBroadcast(new Intent(action, Uri.parse("package:theme.second"))
                    .putExtra(Intent.EXTRA_REPLACING, true));
            shadowOf(Looper.getMainLooper()).idle();
            assertEquals(revision, Theme.getRevision());
        }
        context.sendBroadcast(new Intent(Intent.ACTION_PACKAGE_REPLACED, Uri.parse("package:theme.second")));
        shadowOf(Looper.getMainLooper()).idle();
        assertEquals(revision + 1, Theme.getRevision());
        assertSame(Typeface.SANS_SERIF, Theme.getFont(context, "headline"));
    }

    @Test
    public void replacingBinaryFontAtSameResourcePathReadsNewBytes() {
        String pkg = "theme.typeface";
        install(pkg, null);
        String[] fixture = {"narrow"};
        java.util.List<String> opened = new java.util.ArrayList<>();
        Resources host = context.getResources();
        ShadowPackageManager.resources.put(pkg, new Resources(
                host.getAssets(), host.getDisplayMetrics(), host.getConfiguration()) {
            @Override public int getIdentifier(String name, String type, String owner) { return 1; }
            @Override public void getValue(int id, android.util.TypedValue value, boolean resolveRefs) {
                value.string = "res/font/body.ttf";
            }
            @Override public java.io.InputStream openRawResource(int id) {
                opened.add(fixture[0]);
                return host.openRawResource(host.getIdentifier("theme_test_" + fixture[0], "font", context.getPackageName()));
            }
        });
        Theme.setPackageName(pkg);
        Typeface first = Theme.getFont(context, "body");
        assertNotNull(first);
        assertSame(first, Theme.getFont(context, "body"));
        fixture[0] = "wide";
        context.sendBroadcast(new Intent(Intent.ACTION_PACKAGE_REPLACED, Uri.parse("package:" + pkg)));
        shadowOf(Looper.getMainLooper()).idle();
        assertNotNull(Theme.getFont(context, "body"));
        assertEquals(java.util.Arrays.asList("narrow", "wide"), opened);
    }

    @Test public void bundledXmlFontFamilyIsParsedAgainAfterReplacement() {
        String pkg = "theme.family";
        install(pkg, null);
        Resources host = context.getResources();
        int family = host.getIdentifier("theme_test_family", "font", context.getPackageName());
        int[] reads = {0};
        ShadowPackageManager.resources.put(pkg, new Resources(
                host.getAssets(), host.getDisplayMetrics(), host.getConfiguration()) {
            @Override public int getIdentifier(String name, String type, String owner) { return family; }
            @Override public android.content.res.XmlResourceParser getXml(int id) {
                reads[0]++;
                return host.getXml(id);
            }
        });
        Theme.setPackageName(pkg);
        assertNotNull(Theme.getFont(context, "body"));
        assertNotNull(Theme.getFont(context, "body"));
        assertEquals(1, reads[0]);
        context.sendBroadcast(new Intent(Intent.ACTION_PACKAGE_REPLACED, Uri.parse("package:" + pkg)));
        shadowOf(Looper.getMainLooper()).idle();
        assertNotNull(Theme.getFont(context, "body"));
        assertEquals(2, reads[0]);
    }

    @Test public void listenersObserveChangesOnceAndCanUnsubscribe() {
        int[] calls = {0};
        Runnable listener = () -> calls[0]++;
        Theme.addChangeListener(listener);
        long before = Theme.getRevision();
        Theme.setPackageNames("theme.primary", "theme.first");
        Theme.setPackageNames("theme.primary", "theme.first");
        Theme.setFont(context, Typeface.SERIF);
        Theme.setFont(context, Typeface.SERIF);
        assertEquals(before + 2, Theme.getRevision());
        assertEquals(2, calls[0]);
        Theme.removeChangeListener(listener);
        Theme.setPackageName(null);
        assertEquals(2, calls[0]);
    }

    public static class CountingActivity extends Activity {
        int recreations;
        @Override public void recreate() { recreations++; }
    }

    private void replace(String pkg) {
        context.sendBroadcast(new Intent(Intent.ACTION_PACKAGE_REPLACED, Uri.parse("package:" + pkg)));
        shadowOf(Looper.getMainLooper()).idle();
    }

    @Test @Config(sdk = {21, 28, 33}) public void wrappedWidgetContextsAutomaticallyRefreshOneHost() {
        String pkg = "theme.active";
        Theme.setPackageName(pkg);
        ActivityController<CountingActivity> screen =
                Robolectric.buildActivity(CountingActivity.class).create();
        CountingActivity activity = screen.get();
        Context wrapped = new ContextThemeWrapper(activity, androidx.appcompat.R.style.Theme_AppCompat);
        activity.setContentView(new ThemedView(wrapped));
        new ThemedTextView(wrapped);
        screen.start().resume().visible();
        Theme.getResources(wrapped);
        replace("theme.unrelated");
        assertEquals(0, activity.recreations);
        replace(pkg);
        assertEquals(1, activity.recreations);
        screen.pause().stop().destroy();
    }

    @Test @Config(sdk = {21, 28, 33}) public void stoppedCustomWidgetHostCoalescesChanges() {
        String pkg = "theme.stopped";
        Theme.setPackageName(pkg);
        ActivityController<CountingActivity> screen =
                Robolectric.buildActivity(CountingActivity.class).setup();
        CountingActivity activity = screen.get();
        View custom = new View(activity);
        ThemedWidgets.apply(custom, null, 0, 0);
        screen.pause().stop();
        Theme.getResources(activity);
        replace(pkg);
        Theme.getResources(activity);
        replace(pkg);
        assertEquals(0, activity.recreations);
        screen.restart().start().resume();
        shadowOf(Looper.getMainLooper()).idle();
        assertEquals(1, activity.recreations);
        screen.pause().stop().destroy();
    }

    @Test public void directResourceReadsDoNotRecreateActivity() {
        String pkg = "theme.compose";
        Theme.setPackageName(pkg);
        ActivityController<CountingActivity> screen =
                Robolectric.buildActivity(CountingActivity.class).setup();
        Theme.getResources(screen.get());
        new ThemedView(context);
        replace(pkg);
        assertEquals(0, screen.get().recreations);
        screen.pause().stop().destroy();
    }

    @Test public void destroyedWidgetHostSkipsPendingRefresh() {
        String pkg = "theme.destroyed";
        Theme.setPackageName(pkg);
        ActivityController<CountingActivity> screen = Robolectric.buildActivity(CountingActivity.class).setup();
        CountingActivity activity = screen.get();
        new ThemedView(activity);
        Theme.getResources(activity);
        screen.pause().stop();
        replace(pkg);
        screen.destroy();
        Theme.getResources(context);
        replace(pkg);
        assertEquals(0, activity.recreations);
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
