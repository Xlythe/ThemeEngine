package com.xlythe.engine.theme;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import androidx.annotation.AnyRes;
import androidx.annotation.BoolRes;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.FontRes;
import androidx.annotation.IntegerRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.UiThread;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@SuppressLint("DiscouragedApi")
public class Theme {
    public static final String COLOR = "color";
    public static final String FONT = "font";
    public static final String RAW = "raw";
    public static final String DRAWABLE = "drawable";
    public static final String STRING = "string";
    public static final String BOOLEAN = "bool";
    public static final String INTEGER = "integer";
    public static final String DIMEN = "dimen";
    public static final String STYLE = "style";

    static final String TAG = "Theme";

    private static final Map<String, Typeface> TYPEFACE_MAP = new HashMap<>();

    private static final Map<String, Typeface> defaultFontOverrides = new HashMap<>();
    private static Configuration fontConfiguration;
    private static long revision;
    private static final Set<Runnable> changeListeners = new HashSet<>();

    /** Changes when the selected themes, their installed resources or the default font change. */
    @UiThread
    public static long getRevision() { return revision; }

    /** Register on the main thread. Remove the listener when its owner is disposed. */
    @UiThread
    public static void addChangeListener(@NonNull Runnable listener) { changeListeners.add(listener); }

    @UiThread
    public static void removeChangeListener(@NonNull Runnable listener) { changeListeners.remove(listener); }

    private static void notifyThemeChanged() {
        revision++;
        for (Runnable listener : new ArrayList<>(changeListeners)) listener.run();
    }

    // A list of themes to apply to the app. If empty, there is no theme set. Most apps will only
    // specify a single theme, but some may specify more. In the later case, resources will be loaded
    // from each theme progressively until one is found.
    private static String[] sPackageNames = new String[0];

    // Allows the app to fake its package name, which allows it to apply themes for other packages.
    private static String sPackageOverride;

    private static final Map<String, BroadcastReceiver> sAppTrackingReceivers = new HashMap<>();

    /**
     * Allows you to proxy as another application
     *
     * @param packageOverride The package name of the app you're proxying
     */
    @UiThread
    public static void setPackageOverride(String packageOverride) {
        sPackageOverride = packageOverride;
    }

    @UiThread
    public static Context getThemeContext(Context context) {
        return getContext(context, getPackageName());
    }

    private static Context getContext(Context context, String packageName) {
        if (packageName == null || context.getPackageName().equals(packageName)) {
            return context;
        }

        try {
            return context.createPackageContext(packageName, 0);
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Failed to create a context", e);
        }
        return null;
    }

    /** Grabs the Resources from packageName */
    @UiThread
    public static Resources getResources(Context context) {
        return getResources(context, getPackageName());
    }

    private static Resources getResources(Context context, String packageName) {
        if (packageName == null || context.getPackageName().equals(packageName)) {
            return context.getResources();
        }

        registerReinstallReceiver(context, packageName);
        try {
            Resources resources = context.getPackageManager().getResourcesForApplication(packageName);
            Configuration hostConfig = context.getResources().getConfiguration();
            if (Build.VERSION.SDK_INT >= 17 && !hostConfig.equals(resources.getConfiguration())) {
                Context theme = context.createPackageContext(packageName, 0);
                return theme.createConfigurationContext(hostConfig).getResources();
            }
            return resources;
        } catch (NameNotFoundException e) {
            Log.v(
                    TAG,
                    "Failed to get "
                            + packageName
                            + "'s resources. Returning resources from the context instead.",
                    e);
            return context.getResources();
        }
    }

    // When a theme package is reinstalled, we need to clear our caches of any stale resources from
    // that app.
    private static void registerReinstallReceiver(Context context, final String packageName) {
        if (sAppTrackingReceivers.containsKey(packageName)) {
            // Already tracking this app. Ignore.
            return;
        }

        BroadcastReceiver broadcastReceiver =
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String updatedPackageName = intent.getData().getEncodedSchemeSpecificPart();
                        if (!packageName.equals(updatedPackageName)) {
                            // Ignored. Wrong app.
                            return;
                        }

                        try {
                            context.getApplicationContext().unregisterReceiver(this);
                        } catch (IllegalArgumentException e) {
                            // ignored
                        }
                        sAppTrackingReceivers.remove(packageName);
                        if (Arrays.asList(sPackageNames).contains(packageName)) {
                            TYPEFACE_MAP.clear();
                            defaultFontOverrides.clear();
                            notifyThemeChanged();
                        }
                    }
                };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_INSTALL);
        intentFilter.addDataScheme("package");

        context.getApplicationContext().registerReceiver(broadcastReceiver, intentFilter);
        sAppTrackingReceivers.put(packageName, broadcastReceiver);
        Log.d(TAG, String.format("Registered app listener for %s", packageName));
    }

    /** Gets id from theme apk */
    @UiThread
    @AnyRes
    public static int getId(Context context, String type, String name) {
        return getResources(context).getIdentifier(name, type, getPackageName());
    }

    /** Gets string from theme apk */
    @UiThread
    public static String getString(Context context, @StringRes int resId) {
        return getString(context, Theme.get(context, resId));
    }

    /** Gets string from theme apk */
    @UiThread
    public static String getString(Context context, Res res) {
        return getString(context, res.getName());
    }

    /** Gets string from theme apk */
    @UiThread
    public static String getString(Context context, String name) {
        for (String packageName : getPackageNames(context)) {
            Resources resources = getResources(context, packageName);
            int resId = resources.getIdentifier(name, STRING, packageName);
            if (resId == 0) {
                continue;
            }

            return resources.getString(resId);
        }

        throw new IllegalArgumentException("Resource " + name + " not found");
    }

    /** Gets boolean from theme apk */
    @UiThread
    public static boolean getBoolean(Context context, @BoolRes int resId) {
        return getBoolean(context, Theme.get(context, resId));
    }

    /** Gets boolean from theme apk */
    @UiThread
    public static boolean getBoolean(Context context, Res res) {
        return getBoolean(context, res.getName());
    }

    /** Gets boolean from theme apk */
    @UiThread
    public static boolean getBoolean(Context context, String name) {
        for (String packageName : getPackageNames(context)) {
            Resources resources = getResources(context, packageName);
            int resId = resources.getIdentifier(name, BOOLEAN, packageName);
            if (resId == 0) {
                continue;
            }

            return resources.getBoolean(resId);
        }

        throw new IllegalArgumentException("Resource " + name + " not found");
    }

    /** Gets integer from theme apk */
    @UiThread
    public static int getInt(Context context, @IntegerRes int resId) {
        return getInt(context, Theme.get(context, resId));
    }

    /** Gets integer from theme apk */
    @UiThread
    public static int getInt(Context context, Res res) {
        return getInt(context, res.getName());
    }

    /** Gets integer from theme apk */
    @UiThread
    public static int getInt(Context context, String name) {
        for (String packageName : getPackageNames(context)) {
            Resources resources = getResources(context, packageName);
            int resId = resources.getIdentifier(name, INTEGER, packageName);
            if (resId == 0) {
                continue;
            }

            return resources.getInteger(resId);
        }

        throw new IllegalArgumentException("Resource " + name + " not found");
    }

    /** Gets dimen from theme apk */
    @UiThread
    public static float getDimen(Context context, @DimenRes int resId) {
        return getDimen(context, Theme.get(context, resId));
    }

    /** Gets dimen from theme apk */
    @UiThread
    public static float getDimen(Context context, Res res) {
        return getDimen(context, res.getName());
    }

    /** Gets dimen from theme apk */
    @UiThread
    public static float getDimen(Context context, String name) {
        for (String packageName : getPackageNames(context)) {
            Resources resources = getResources(context, packageName);
            int resId = resources.getIdentifier(name, DIMEN, packageName);
            if (resId == 0) {
                continue;
            }

            return resources.getDimension(resId);
        }

        throw new IllegalArgumentException("Resource " + name + " not found");
    }

    /** Gets drawable from theme apk */
    @UiThread
    public static Drawable getDrawable(Context context, @DrawableRes int resId) {
        return getDrawable(context, Theme.get(context, resId));
    }

    /** Gets drawable from theme apk */
    @UiThread
    public static Drawable getDrawable(Context context, Res res) {
        return getDrawable(context, res.getName());
    }

    /** Gets drawable from theme apk */
    @UiThread
    public static Drawable getDrawable(Context context, String name) {
        for (String packageName : getPackageNames(context)) {
            Resources resources = getResources(context, packageName);
            int resId = resources.getIdentifier(name, DRAWABLE, packageName);
            if (resId == 0) {
                continue;
            }

            if (Build.VERSION.SDK_INT >= 21) {
                return resources.getDrawable(resId, context.getTheme());
            } else {
                return resources.getDrawable(resId);
            }
        }

        throw new IllegalArgumentException("Resource " + name + " not found");
    }

    /** Gets color from theme apk */
    @ColorInt
    @UiThread
    public static int getColor(Context context, @ColorRes int resId) {
        return getColor(context, Theme.get(context, resId));
    }

    /** Gets color from theme apk */
    @ColorInt
    @UiThread
    public static int getColor(Context context, Res res) {
        return getColor(context, res.getName());
    }

    /** Gets color from theme apk */
    @ColorInt
    @UiThread
    public static int getColor(Context context, String name) {
        for (String packageName : getPackageNames(context)) {
            Resources resources = getResources(context, packageName);
            int resId = resources.getIdentifier(name, COLOR, packageName);
            if (resId == 0) {
                continue;
            }

            return resources.getColor(resId);
        }

        throw new IllegalArgumentException("Resource " + name + " not found");
    }

    /** Gets color from theme apk */
    @UiThread
    public static ColorStateList getColorStateList(Context context, @ColorRes int resId) {
        return getColorStateList(context, Theme.get(context, resId));
    }

    /** Gets color from theme apk */
    @UiThread
    public static ColorStateList getColorStateList(Context context, Res res) {
        return getColorStateList(context, res.getName());
    }

    /** Gets color from theme apk */
    @UiThread
    public static ColorStateList getColorStateList(Context context, String name) {
        for (String packageName : getPackageNames(context)) {
            Resources resources = getResources(context, packageName);
            int resId = resources.getIdentifier(name, COLOR, packageName);
            if (resId == 0) {
                continue;
            }

            return resources.getColorStateList(resId);
        }

        throw new IllegalArgumentException("Resource " + name + " not found");
    }

    /** Gets android theme from theme apk. Can be 0 (no theme). */
    @UiThread
    public static int getTheme(Context context) {
        int id = getId(context, STRING, "app_theme");
        if (id == 0) return 0;

        String fieldName = getResources(context).getString(id);
        return context.getResources().getIdentifier(fieldName, STYLE, context.getPackageName());
    }

    /**
     * Gets android theme from theme apk. Can be 0 (no theme). This is for apps that want an actionbar
     * in their Settings but not in their main app.
     */
    @UiThread
    public static int getSettingsTheme(Context context) {
        int id = getId(context, STRING, "app_settings_theme");
        if (id == 0) return 0;

        String fieldName = getResources(context).getString(id);
        return context.getResources().getIdentifier(fieldName, STYLE, context.getPackageName());
    }

    /** Returns whether the theme is light or dark. WARNING: Assumes dark if no resource is found. */
    @UiThread
    public static boolean isLightTheme(Context context) {
        int id = getId(context, STRING, "app_theme");
        if (id != 0) {
            String fieldName = getResources(context).getString(id);
            return fieldName.toLowerCase(Locale.US).contains("light");
        } else {
            return false;
        }
    }

    @Nullable
    @UiThread
    public static String getPackageName() {
        return sPackageNames.length == 0 ? null : sPackageNames[0];
    }

    private static String[] getPackageNames(Context context) {
        String[] packageNames = new String[sPackageNames.length + 1];
        for (int i = 0; i < sPackageNames.length; i++) {
            packageNames[i] = sPackageNames[i];
        }
        packageNames[sPackageNames.length] = context.getPackageName();
        return packageNames;
    }

    @UiThread
    public static void setPackageName(@Nullable String packageName) {
        setPackageNames(packageName == null ? new String[0] : new String[] {packageName});
    }

    @UiThread
    public static void setPackageNames(String... packageNames) {
        if (!Arrays.equals(sPackageNames, packageNames)) {
            sPackageNames = packageNames.clone();
            TYPEFACE_MAP.clear();
            defaultFontOverrides.clear();
            notifyThemeChanged();
        }
    }

    @UiThread
    @Nullable
    public static Res get(Context context, @AnyRes int resId) {
        if (resId == 0) {
            return null;
        }
        return new Res(
                context.getResources().getResourceTypeName(resId),
                context.getResources().getResourceEntryName(resId));
    }

    @UiThread
    public static String getSoundPath(Context context, Res res) {
        for (String packageName : getPackageNames(context)) {
            Resources resources = getResources(context, packageName);
            int resId = resources.getIdentifier(res.getName(), res.getType(), packageName);
            if (resId == 0) {
                continue;
            }

            return "android.resource://" + packageName + "/" + resId;
        }

        throw new IllegalArgumentException("Resource " + res + " not found");
    }

    @UiThread
    public static int getSound(Context context, SoundPool soundPool, Res res) {
        for (String packageName : getPackageNames(context)) {
            Resources resources = getResources(context, packageName);
            int resId = resources.getIdentifier(res.getName(), res.getType(), packageName);
            if (resId == 0) {
                continue;
            }

            return soundPool.load(getContext(context, packageName), resId, 1);
        }

        throw new IllegalArgumentException("Resource " + res + " not found");
    }

    @UiThread
    public static long getDurationOfSound(Context context, Theme.Res res) {
        for (String packageName : getPackageNames(context)) {
            Resources resources = getResources(context, packageName);
            int millis = 0;
            MediaPlayer mp = new MediaPlayer();
            try {
                AssetFileDescriptor afd;
                int resId = resources.getIdentifier(res.getName(), res.getType(), packageName);
                if (resId == 0) {
                    continue;
                }

                afd = getContext(context, packageName).getResources().openRawResourceFd(resId);
                mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                afd.close();
                mp.prepare();
                millis = mp.getDuration();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mp.release();
            }
            return millis;
        }

        throw new IllegalArgumentException("Resource " + res + " not found");
    }

    private static void checkFontConfiguration(Context context) {
        Configuration current = context.getResources().getConfiguration();
        if (!current.equals(fontConfiguration)) {
            fontConfiguration = new Configuration(current);
            TYPEFACE_MAP.clear();
        }
    }

    @UiThread
    public static void setFont(Context context, Typeface typeface) {
        String host = context.getPackageName();
        if (defaultFontOverrides.containsKey(host) && defaultFontOverrides.get(host) == typeface) return;
        defaultFontOverrides.put(host, typeface);
        notifyThemeChanged();
    }

    @UiThread
    @Nullable
    public static Typeface getFont(Context context, @FontRes int resId) {
        return getFont(context, Theme.get(context, resId));
    }

    @UiThread
    @Nullable
    public static Typeface getFont(Context context, Res res) {
        return getFont(context, res.getName());
    }

    @UiThread
    @Nullable
    public static Typeface getFont(Context context) {
        return getFont(context, "font");
    }

    @UiThread
    @Nullable
    public static Typeface getFont(Context context, String name) {
        if ("font".equals(name) && defaultFontOverrides.containsKey(context.getPackageName())) {
            return defaultFontOverrides.get(context.getPackageName());
        }
        checkFontConfiguration(context);
        String key = context.getPackageName() + ":" + name;
        if (TYPEFACE_MAP.containsKey(key)) {
            return TYPEFACE_MAP.get(key);
        }

        if (Build.VERSION.SDK_INT >= 26) {
            for (String packageName : sPackageNames) {
                Resources resources = getResources(context, packageName);
                int resId = resources.getIdentifier(name, FONT, packageName);
                if (resId == 0) {
                    continue;
                }

                Typeface font = loadThemeFont(context, resources, resId);
                TYPEFACE_MAP.put(key, font);
                return font;
            }
        }

        String[] extensions = {".ttf", ".otf"};
        for (String packageName : sPackageNames) {
            registerReinstallReceiver(context, packageName);
            for (String s : extensions) {
                try {
                    // Use cursor loader to grab font
                    Uri uri = Uri.parse("content://" + packageName + ".FileProvider/" + name + s);
                    try (AssetFileDescriptor asset = context.getContentResolver().openAssetFileDescriptor(uri, "r")) {
                        if (asset == null) continue;
                        try (InputStream input = asset.createInputStream()) {
                            Typeface font = readFont(context, input);
                            TYPEFACE_MAP.put(key, font);
                            return font;
                        }
                    }
                } catch (Exception e) {
                    // Do nothing
                }
            }
        }

        if (Build.VERSION.SDK_INT >= 26) {
            Resources resources = context.getResources();
            int resId = resources.getIdentifier(name, FONT, context.getPackageName());
            if (resId != 0) {
                Typeface font = resources.getFont(resId);
                TYPEFACE_MAP.put(key, font);
                return font;
            }
        }

        AssetManager am = context.getResources().getAssets();
        for (String s : extensions) {
            try {
                // Try/catch for broken fonts
                Typeface font = Typeface.createFromAsset(am, name + s);
                TYPEFACE_MAP.put(key, font);
                return font;
            } catch (Exception e) {
                // Do nothing
            }
        }

        // No typeface was found.
        TYPEFACE_MAP.put(key, null);
        return null;
    }

    @androidx.annotation.RequiresApi(26)
    private static Typeface loadThemeFont(Context context, Resources resources, int id) {
        TypedValue value = new TypedValue();
        resources.getValue(id, value, true);
        if (value.string == null || value.string.toString().endsWith(".xml")) {
            return resources.getFont(id);
        }
        // Android can reuse a typeface by APK package and asset name after an in-place update.
        // Read the current binary once per resource-cache generation, including compressed fonts.
        try (InputStream input = resources.openRawResource(id)) {
            return readFont(context, input);
        } catch (IOException e) {
            Log.w(TAG, "Unable to read font " + value.string, e);
            return resources.getFont(id);
        }
    }

    private static Typeface readFont(Context context, InputStream input) throws IOException {
        File file = File.createTempFile("theme-font-", ".tmp", context.getCacheDir());
        try {
            try (FileOutputStream output = new FileOutputStream(file)) {
                byte[] buffer = new byte[8192];
                int count;
                while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
            }
            return Typeface.createFromFile(file);
        } finally {
            if (!file.delete()) Log.w(TAG, "Unable to remove temporary font");
        }
    }

    /** Returns a list of installed apps that are registered as themes */
    @UiThread
    public static List<App> getApps(Context context) {
        Set<App> apps = getAppsByActivity(context);
        apps.addAll(getAppsByMetadata(context));
        return new ArrayList<>(apps);
    }

    @UiThread
    private static Set<App> getAppsByActivity(Context context) {
        Set<App> apps = new HashSet<>();
        PackageManager manager = context.getPackageManager();

        Intent mainIntent;
        if (sPackageOverride != null) {
            mainIntent = new Intent(sPackageOverride + ".THEME", null);
        } else {
            mainIntent = new Intent(context.getPackageName() + ".THEME", null);
        }

        final List<ResolveInfo> infos;
        try {
            infos = manager.queryIntentActivities(mainIntent, 0);
        } catch (Exception e) {
            e.printStackTrace();
            return apps;
        }

        for (ResolveInfo info : infos) {
            apps.add(
                    new App(
                            info.loadLabel(manager).toString(), info.activityInfo.applicationInfo.packageName));
        }
        return apps;
    }

    @UiThread
    private static Set<App> getAppsByMetadata(Context context) {
        Set<App> apps = new HashSet<>();
        PackageManager manager = context.getPackageManager();
        String overridePackageName =
                String.format(
                        "%s.THEME", sPackageOverride == null ? context.getPackageName() : sPackageOverride);

        for (ApplicationInfo info : manager.getInstalledApplications(PackageManager.GET_META_DATA)) {
            Bundle bundle = info.metaData;
            if (bundle == null || !bundle.containsKey(overridePackageName)) {
                continue;
            }
            try {
                if ((bundle.getBoolean(overridePackageName, false))) {
                    apps.add(new App(info.loadLabel(manager).toString(), info.packageName));
                }
            } catch (ClassCastException e) {
                // ignore.
            }
        }

        return apps;
    }

    public static class Res {
        private final String type;
        private final String name;

        private Res(String type, String name) {
            this.type = type;
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        @NonNull
        @Override
        public String toString() {
            return String.format("Res{name=%s, type=%s}", name, type);
        }
    }
}