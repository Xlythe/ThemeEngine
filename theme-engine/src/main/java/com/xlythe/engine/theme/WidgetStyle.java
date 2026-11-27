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

    /** Holds a size chosen before the parent has supplied its own LayoutParams. */
    static final class Dimensions {
        private Integer pendingWidth;
        private Integer pendingHeight;

        void setWidth(View view, @Nullable Theme.Res res) {
            if (res == null || !Theme.DIMEN.equals(res.getType())) return;
            int pixels = (int) Theme.getDimen(view.getContext(), res);
            ViewGroup.LayoutParams params = view.getLayoutParams();
            if (params == null) pendingWidth = pixels;
            else {
                params.width = pixels;
                view.requestLayout();
            }
        }

        void setHeight(View view, @Nullable Theme.Res res) {
            if (res == null || !Theme.DIMEN.equals(res.getType())) return;
            int pixels = (int) Theme.getDimen(view.getContext(), res);
            ViewGroup.LayoutParams params = view.getLayoutParams();
            if (params == null) pendingHeight = pixels;
            else {
                params.height = pixels;
                view.requestLayout();
            }
        }

        void applyTo(ViewGroup.LayoutParams params) {
            if (params == null) return;
            if (pendingWidth != null) params.width = pendingWidth;
            if (pendingHeight != null) params.height = pendingHeight;
            pendingWidth = null;
            pendingHeight = null;
        }
    }
}
