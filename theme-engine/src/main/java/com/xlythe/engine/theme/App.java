package com.xlythe.engine.theme;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;

public class App implements Serializable {
    private static final long serialVersionUID = -7796311962836649402L;
    private final String name;
    private final String packageName;

    public App(String name, String packageName) {
        this.name = name;
        this.packageName = packageName;
    }

    public static boolean doesPackageExists(Context context, String targetPackage) {
        try {
            context.getPackageManager().getApplicationInfo(targetPackage, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static App getApp(Context context, String packageName) {
        PackageManager manager = context.getPackageManager();
        Intent launch = manager.getLaunchIntentForPackage(packageName);
        ResolveInfo launcher = launch == null ? null : manager.resolveActivity(launch, 0);
        if (launcher != null) return new App(launcher.loadLabel(manager).toString(), packageName);
        try {
            ApplicationInfo info = manager.getApplicationInfo(packageName, 0);
            return new App(info.loadLabel(manager).toString(), packageName);
        } catch (PackageManager.NameNotFoundException missing) {
            return new App(packageName, packageName);
        }
    }

    public String getName() {
        return name;
    }

    /** Returns null when the package has no launcher activity or is no longer installed. */
    @Nullable
    public Intent getIntent(Context context) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent == null) return null;
        int flags =
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                        | Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED;
        intent.setFlags(flags);
        return intent;
    }

    public String getPackageName() {
        return packageName;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        }

        if (other instanceof App) {
            App that = (App) other;
            return this.toString().equals(that.toString());
        }

        return false;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("App<%s, %s>", name, packageName);
    }
}