package com.xlythe.engine.theme;

import android.content.Context;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Nullable;

final class WidgetStyle {
    private WidgetStyle() {}

    // AppCompat constructors accept defStyleAttr but have no defStyleRes overload.
    static Context withFallback(Context context, int attr, int fallback) {
        if (fallback == 0 || resolveStyle(context, attr) != 0) return context;
        return new ContextThemeWrapper(context, fallback);
    }

    static Context withDefaultStyle(Context context, int attr, int fallback) {
        int style = resolveStyle(context, attr);
        if (style == 0) style = fallback;
        return style == 0 ? context : new ContextThemeWrapper(context, style);
    }

    private static int resolveStyle(Context context, int attr) {
        TypedValue value = new TypedValue();
        return attr != 0 && context.getTheme().resolveAttribute(attr, value, true) ? value.resourceId : 0;
    }

    static void setDimension(View view, @Nullable Theme.Res res, int axis) {
        if (res == null || !Theme.DIMEN.equals(res.getType())) return;
        int pixels = (int) Theme.getDimen(view.getContext(), res);
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params == null) {
            view.setTag(axis, pixels);
            return;
        }
        if (axis == R.id.theme_pending_width) params.width = pixels;
        else params.height = pixels;
        view.requestLayout();
    }

    static void applyPendingDimensions(View view, ViewGroup.LayoutParams params) {
        if (params == null) return;
        Integer width = (Integer) view.getTag(R.id.theme_pending_width);
        Integer height = (Integer) view.getTag(R.id.theme_pending_height);
        if (width != null) {
            params.width = width;
            view.setTag(R.id.theme_pending_width, null);
        }
        if (height != null) {
            params.height = height;
            view.setTag(R.id.theme_pending_height, null);
        }
    }
}
