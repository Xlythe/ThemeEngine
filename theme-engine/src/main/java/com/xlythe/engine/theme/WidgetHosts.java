package com.xlythe.engine.theme;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import androidx.core.app.ActivityCompat;
import java.util.ArrayList;
import java.util.WeakHashMap;

/** Main-thread lifecycle tracking for Activities that inflate themed widgets. */
final class WidgetHosts implements Application.ActivityLifecycleCallbacks {
    private static final WidgetHosts INSTANCE = new WidgetHosts();
    private final WeakHashMap<Activity, Host> hosts = new WeakHashMap<>();

    private static final class Host {
        Boolean started;
        boolean pending;
        boolean recreating;
    }

    static void track(Context context) {
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                INSTANCE.trackActivity((Activity) context);
                return;
            }
            Context base = ((ContextWrapper) context).getBaseContext();
            if (base == context) return;
            context = base;
        }
    }

    private void trackActivity(Activity activity) {
        if (hosts.containsKey(activity) || activity.isFinishing()
                || (Build.VERSION.SDK_INT >= 17 && activity.isDestroyed())) return;
        if (hosts.isEmpty()) activity.getApplication().registerActivityLifecycleCallbacks(this);
        hosts.put(activity, new Host());
    }

    static void onPackageChanged() {
        for (Activity activity : new ArrayList<>(INSTANCE.hosts.keySet())) {
            Host host = INSTANCE.hosts.get(activity);
            if (host != null) {
                host.pending = true;
                refresh(activity, host);
            }
        }
    }

    private static void refresh(Activity activity, Host host) {
        if (!host.pending || host.recreating || activity.isFinishing()) return;
        // A widget can first be added after onStart. Until we receive a lifecycle event,
        // use the visibility of the existing window to identify an already running Activity.
        View decor = activity.getWindow().peekDecorView();
        boolean started = host.started != null ? host.started
                : decor != null && decor.isShown() && decor.getWindowVisibility() == View.VISIBLE;
        if (started) {
            host.recreating = true;
            ActivityCompat.recreate(activity);
        }
    }

    @Override public void onActivityStarted(Activity activity) {
        Host host = hosts.get(activity);
        if (host != null) {
            host.started = true;
            refresh(activity, host);
        }
    }

    @Override public void onActivityStopped(Activity activity) {
        Host host = hosts.get(activity);
        if (host != null) host.started = false;
    }

    @Override public void onActivityDestroyed(Activity activity) {
        if (hosts.remove(activity) != null && hosts.isEmpty()) {
            activity.getApplication().unregisterActivityLifecycleCallbacks(this);
        }
    }

    @Override public void onActivityCreated(Activity activity, Bundle state) {}
    @Override public void onActivityResumed(Activity activity) { onActivityStarted(activity); }
    @Override public void onActivityPaused(Activity activity) {}
    @Override public void onActivitySaveInstanceState(Activity activity, Bundle state) {}
}
