package com.xlythe.engine.theme;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.AnyRes;
import android.support.annotation.BoolRes;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.FontRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.UiThread;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class Theme {
    public static final String COLOR = "color";
    public static final String FONT = "font";
    public static final String RAW = "raw";
    public static final String DRAWABLE = "drawable";
    public static final String STRING = "string";
    public static final String BOOLEAN = "bool";
    public static final String DIMEN = "dimen";

    static final String TAG = "Theme";

    private static final Map<String, Typeface> TYPEFACE_MAP = new HashMap<>();
    private static final LruCache<String, Drawable> DRAWABLE_MAP = new LruCache<>(100);
    private static final LruCache<String, Integer> COLOR_MAP = new LruCache<>(100);
    private static final LruCache<String, ColorStateList> COLOR_STATE_LIST_MAP = new LruCache<>(100);
    private static String PACKAGE_NAME;
    private static String PACKAGE_OVERRIDE;

    private static void clearCacheForPackage(Context context, String packageName) {
        String prefix = getKey(context, packageName) + "_";
        remove(TYPEFACE_MAP, prefix);
        remove(DRAWABLE_MAP, prefix);
        remove(COLOR_MAP, prefix);
        remove(COLOR_STATE_LIST_MAP, prefix);
    }

    private static void remove(Map<String, ?> cache, String prefix) {
        Set<String> keysToRemove = new HashSet<>();
        for (String key : cache.keySet()) {
            if (key.startsWith(prefix)) {
                keysToRemove.add(key);
            }
        }
        for (String key : keysToRemove) {
            cache.remove(key);
        }
    }

    private static void remove(LruCache<String, ?> cache, String prefix) {
        for (String key : cache.snapshot().keySet()) {
            if (key.startsWith(prefix)) {
                cache.remove(key);
            }
        }
    }

    /**
     * Allows you to proxy as another application
     *
     * @param packageOverride The package name of the app you're proxying
     */
    @UiThread
    public static void setPackageOverride(String packageOverride) {
        PACKAGE_OVERRIDE = packageOverride;
    }

    @UiThread
    static String getPackageOverride() {
        return PACKAGE_OVERRIDE;
    }

    public static Context getThemeContext(Context context) {
        try {
            return context.createPackageContext(getPackageName(), Context.CONTEXT_INCLUDE_CODE + Context.CONTEXT_IGNORE_SECURITY);
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Failed to create a context", e);
        }
        return null;
    }

    /**
     * Grabs the Resources from packageName
     */
    public static Resources getResources(Context context) {
        try {
            Resources resources = context.getPackageManager().getResourcesForApplication(getPackageName());
            registerReinstallReceiver(context, getPackageName());
            return resources;
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Failed to get " + getPackageName() + "'s resources. Returning resources from the context instead.", e);
            return context.getResources();
        }
    }

    // When a theme package is reinstalled, we need to clear our caches of any stale resources from that app.
    private static void registerReinstallReceiver(Context context, final String packageName) {
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String updatedPackageName = intent.getData().getEncodedSchemeSpecificPart();
                if (!packageName.equals(updatedPackageName)) {
                    // Ignored. Wrong app.
                    return;
                }

                clearCacheForPackage(context, packageName);
                context.getApplicationContext().unregisterReceiver(this);
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_INSTALL);
        intentFilter.addDataScheme("package");

        context.getApplicationContext().registerReceiver(broadcastReceiver, intentFilter);
    }

    /**
     * Gets id from theme apk
     */
    @AnyRes
    public static int getId(Context context, String type, String name) {
        return getResources(context).getIdentifier(name, type, getPackageName());
    }

    /**
     * Gets string from theme apk
     */
    public static String getString(Context context, @StringRes int resId) {
        return getString(context, Theme.get(context, resId));
    }

    /**
     * Gets string from theme apk
     */
    public static String getString(Context context, Res res) {
        return getString(context, res.getName());
    }

    /**
     * Gets string from theme apk
     */
    public static String getString(Context context, String name) {
        int id = getId(context, STRING, name);
        if (id == 0) return null;
        return getResources(context).getString(id);
    }

    /**
     * Gets boolean from theme apk
     */
    public static Boolean getBoolean(Context context, @BoolRes int resId) {
        return getBoolean(context, Theme.get(context, resId));
    }

    /**
     * Gets boolean from theme apk
     */
    public static Boolean getBoolean(Context context, Res res) {
        return getBoolean(context, res.getName());
    }

    /**
     * Gets boolean from theme apk
     */
    public static Boolean getBoolean(Context context, String name) {
        int id = getId(context, BOOLEAN, name);
        if (id == 0) {
            id = context.getResources().getIdentifier(name, BOOLEAN, context.getPackageName());
            if (id != 0) {
                return context.getResources().getBoolean(id);
            } else return null;
        }
        return getResources(context).getBoolean(id);
    }

    /**
     * Gets dimen from theme apk
     */
    public static Float getDimen(Context context, @DimenRes int resId) {
        return getDimen(context, Theme.get(context, resId));
    }

    /**
     * Gets dimen from theme apk
     */
    public static Float getDimen(Context context, Res res) {
        return getDimen(context, res.getName());
    }

    /**
     * Gets dimen from theme apk
     */
    public static Float getDimen(Context context, String name) {
        int id = getId(context, DIMEN, name);
        if (id == 0) {
            id = context.getResources().getIdentifier(name, DIMEN, context.getPackageName());
            if (id != 0) {
                return context.getResources().getDimension(id);
            } else return null;
        }
        return getResources(context).getDimension(id);
    }

    /**
     * Gets drawable from theme apk
     */
    public static Drawable getDrawable(Context context, @DrawableRes int resId) {
        return getDrawable(context, Theme.get(context, resId));
    }

    /**
     * Gets drawable from theme apk
     */
    public static Drawable getDrawable(Context context, Res res) {
        return getDrawable(context, res.getName());
    }

    /**
     * Gets drawable from theme apk
     */
    public static Drawable getDrawable(Context context, String name) {
        String key = getKey(context) + "_" + name;
        if (DRAWABLE_MAP.get(key) != null) {
            return DRAWABLE_MAP.get(key).getConstantState().newDrawable();
        }
        int id = getId(context, DRAWABLE, name);
        if (id == 0) {
            id = context.getResources().getIdentifier(name, DRAWABLE, context.getPackageName());
            if (id != 0) {
                DRAWABLE_MAP.put(key, context.getResources().getDrawable(id));
                return DRAWABLE_MAP.get(key);
            } else return null;
        }
        DRAWABLE_MAP.put(key, getResources(context).getDrawable(id));
        return DRAWABLE_MAP.get(key);
    }

    /**
     * Gets color from theme apk
     */
    public static int getColor(Context context, @ColorRes int resId) {
        return getColor(context, Theme.get(context, resId));
    }

    /**
     * Gets color from theme apk
     */
    public static int getColor(Context context, Res res) {
        return getColor(context, res.getName());
    }

    /**
     * Gets color from theme apk
     */
    public static int getColor(Context context, String name) {
        String key = getKey(context) + "_" + name;
        if (COLOR_MAP.get(key) != null) {
            return COLOR_MAP.get(key);
        }
        int id = getId(context, COLOR, name);
        if (id == 0) {
            id = context.getResources().getIdentifier(name, COLOR, context.getPackageName());
            COLOR_MAP.put(key, context.getResources().getColor(id));
            return COLOR_MAP.get(key);
        }
        COLOR_MAP.put(key, getResources(context).getColor(id));
        return COLOR_MAP.get(key);
    }

    /**
     * Gets color from theme apk
     */
    public static ColorStateList getColorStateList(Context context, @ColorRes int resId) {
        return getColorStateList(context, Theme.get(context, resId));
    }

    /**
     * Gets color from theme apk
     */
    public static ColorStateList getColorStateList(Context context, Res res) {
        return getColorStateList(context, res.getName());
    }

    /**
     * Gets color from theme apk
     */
    public static ColorStateList getColorStateList(Context context, String name) {
        String key = getKey(context) + "_" + name;
        if (COLOR_STATE_LIST_MAP.get(key) != null) {
            return COLOR_STATE_LIST_MAP.get(key);
        }
        int id = getId(context, COLOR, name);
        if (id == 0) {
            id = context.getResources().getIdentifier(name, COLOR, context.getPackageName());
            COLOR_STATE_LIST_MAP.put(key, context.getResources().getColorStateList(id));
            return COLOR_STATE_LIST_MAP.get(key);
        }
        COLOR_STATE_LIST_MAP.put(key, getResources(context).getColorStateList(id));
        return COLOR_STATE_LIST_MAP.get(key);
    }

    /**
     * Gets android theme from theme apk. Can be 0 (no theme).
     */
    public static int getTheme(Context context) {
        int id = getId(context, "string", "app_theme");
        if (id == 0) return 0;

        String fieldName = getResources(context).getString(id).replace(".", "_");
        try {
            Field field = android.R.style.class.getField(fieldName);
            return field.getInt(null);
        } catch (RuntimeException e) {
            Log.e(TAG, "Ignoring runtime exception.", e);
        } catch (NoSuchFieldException e) {
            Log.e(TAG, "Reflection failed", e);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Reflection failed", e);
        }
        return 0;
    }

    /**
     * Gets android theme from theme apk. Can be 0 (no theme). This is for apps that want an actionbar in their Settings but not in their main app.
     */
    public static int getSettingsTheme(Context context) {
        int id = getId(context, "string", "app_settings_theme");
        if (id == 0) return 0;

        String fieldName = getResources(context).getString(id).replace(".", "_");
        try {
            Field field = android.R.style.class.getField(fieldName);
            return field.getInt(null);
        } catch (RuntimeException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Returns whether the theme is light or dark. WARNING: Assumes dark if no resource is found.
     */
    public static boolean isLightTheme(Context context) {
        int id = getId(context, "string", "app_theme");
        if (id != 0) {
            String fieldName = getResources(context).getString(id).replace(".", "_");
            return fieldName.toLowerCase(Locale.US).contains("light");
        } else {
            return false;
        }
    }

    public static String getPackageName() {
        return PACKAGE_NAME;
    }

    public static void setPackageName(String packageName) {
        PACKAGE_NAME = packageName;
    }

    @Nullable
    public static Res get(Context context, @AnyRes int resId) {
        if (resId == 0) {
            return null;
        }
        return new Res(context.getResources().getResourceTypeName(resId), context.getResources().getResourceEntryName(resId));
    }

    public static String getSoundPath(Context context, Res res) {
        int id = getId(context, res.getType(), res.getName());
        if (id == 0) {
            id = context.getResources().getIdentifier(res.getName(), res.getType(), context.getPackageName());
            return "android.resource://" + context.getPackageName() + "/" + id;
        }
        return "android.resource://" + getPackageName() + "/" + id;
    }

    public static int getSound(Context context, SoundPool soundPool, Res res) {
        int id = getId(context, res.getType(), res.getName());
        if (id == 0) {
            id = context.getResources().getIdentifier(res.getName(), res.getType(), context.getPackageName());
            return soundPool.load(context, id, 1);
        }
        return soundPool.load(getThemeContext(context), id, 1);
    }

    public static long getDurationOfSound(Context context, Theme.Res res) {
        int millis = 0;
        MediaPlayer mp = new MediaPlayer();
        try {
            AssetFileDescriptor afd;
            int id = getId(context, res.getType(), res.getName());
            if (id == 0) {
                id = context.getResources().getIdentifier(res.getName(), res.getType(), context.getPackageName());
                afd = context.getResources().openRawResourceFd(id);
            } else {
                afd = getThemeContext(context).getResources().openRawResourceFd(id);
            }
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

    public static void setFont(Context context, Typeface typeface) {
        TYPEFACE_MAP.put(getKey(context) + "_" + "font", typeface);
    }

    @Nullable
    public static Typeface getFont(Context context, @FontRes int resId) {
        return getFont(context, Theme.get(context, resId));
    }

    @Nullable
    public static Typeface getFont(Context context, Res res) {
        return getFont(context, res.getName());
    }

    @Nullable
    public static Typeface getFont(Context context) {
        return getFont(context, "font");
    }

    @Nullable
    public static Typeface getFont(Context context, String name) {
        String key = getKey(context) + "_" + name;
        if (TYPEFACE_MAP.containsKey(key)) {
            return TYPEFACE_MAP.get(key);
        }

        if (Build.VERSION.SDK_INT >= 26) {
            int id = getId(context, FONT, name);
            if (id != 0) {
                TYPEFACE_MAP.put(key, getResources(context).getFont(id));
                return TYPEFACE_MAP.get(key);
            }
        }

        String[] extensions = {".ttf", ".otf"};
        for (String s : extensions) {
            try {
                // Use cursor loader to grab font
                Uri uri = Uri.parse("content://" + getPackageName() + ".FileProvider/" + name + s);
                AssetFileDescriptor a = context.getContentResolver().openAssetFileDescriptor(uri, null);
                FileInputStream in = new FileInputStream(a.getFileDescriptor());
                in.skip(a.getStartOffset());
                File file = new File(context.getCacheDir(), name + s);
                file.delete();
                file.createNewFile();
                FileOutputStream fOutput = new FileOutputStream(file);
                byte[] dataBuffer = new byte[1024];
                int readLength = 0;
                while ((readLength = in.read(dataBuffer)) != -1) {
                    fOutput.write(dataBuffer, 0, readLength);
                }
                in.close();
                fOutput.close();

                // Try/catch for broken fonts
                Typeface t = Typeface.createFromFile(file);
                TYPEFACE_MAP.put(key, t);
                return TYPEFACE_MAP.get(key);
            } catch (Exception e) {
                // Do nothing
            }
        }

        if (Build.VERSION.SDK_INT >= 26) {
            int id = context.getResources().getIdentifier(name, COLOR, context.getPackageName());
            if (id != 0) {
                TYPEFACE_MAP.put(key, context.getResources().getFont(id));
                return TYPEFACE_MAP.get(key);
            }
        }

        AssetManager am = context.getResources().getAssets();
        for (String s : extensions) {
            try {
                // Try/catch for broken fonts
                Typeface t = Typeface.createFromAsset(am, name + s);
                TYPEFACE_MAP.put(key, t);
                return TYPEFACE_MAP.get(key);
            } catch (Exception e) {
                // Do nothing
            }
        }

        // No typeface was found.
        TYPEFACE_MAP.put(key, null);
        return TYPEFACE_MAP.get(key);
    }

    /**
     * Returns a list of installed apps that are registered as themes
     */
    public static List<App> getApps(Context context) {
        List<App> apps = new LinkedList<>();
        PackageManager manager = context.getPackageManager();

        Intent mainIntent;
        if (PACKAGE_OVERRIDE != null) {
            mainIntent = new Intent(PACKAGE_OVERRIDE + ".THEME", null);
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
            apps.add(new App(info.loadLabel(manager).toString(), info.activityInfo.applicationInfo.packageName));
        }
        return apps;
    }

    private static String getKey(Context context) {
        return getKey(context, getPackageName());
    }

    private static String getKey(Context context, String packageName) {
        return context.getPackageName() + "_" + packageName;
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

        @Override
        public String toString() {
            return String.format("Res{name=%s, type=%s}", name, type);
        }
    }
}
