package com.xlythe.engine.theme;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toolbar;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.core.view.ViewCompat;

/** Applies external theme resources to framework views, including application subclasses. */
public final class ThemedWidgets {
    private ThemedWidgets() {}

    /** Read the themed attributes from XML, the widget's default style, or the current theme. */
    @UiThread
    public static void apply(View view, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        Context context = view.getContext();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.theme, defStyleAttr, defStyleRes);
        try {
            Theme.Res background = Theme.get(context, WidgetAttribute.BACKGROUND.resourceId(a));
            if (background != null) {
                if (Theme.COLOR.equals(background.getType())) view.setBackgroundColor(Theme.getColor(context, background));
                else if (Theme.DRAWABLE.equals(background.getType())) ViewCompat.setBackground(view, Theme.getDrawable(context, background));
            }
            if (view instanceof TextView) applyText((TextView) view, a);
            if (view instanceof ProgressBar) {
                ProgressBar progress = (ProgressBar) view;
                int id = WidgetAttribute.PROGRESS_DRAWABLE.resourceId(a);
                if (id != 0) progress.setProgressDrawable(Theme.getDrawable(context, id));
                id = WidgetAttribute.INDETERMINATE_DRAWABLE.resourceId(a);
                if (id != 0) progress.setIndeterminateDrawable(Theme.getDrawable(context, id));
            }
            if (view instanceof SeekBar) {
                int id = WidgetAttribute.THUMB.resourceId(a);
                if (id != 0) ((SeekBar) view).setThumb(Theme.getDrawable(context, id));
            }
            if (view instanceof SearchView) {
                int id = WidgetAttribute.QUERY_HINT.resourceId(a);
                if (id != 0) ((SearchView) view).setQueryHint(Theme.getString(context, id));
            }
            if (view instanceof AutoCompleteTextView) {
                int id = WidgetAttribute.DROP_DOWN_BACKGROUND.resourceId(a);
                if (id != 0) ((AutoCompleteTextView) view).setDropDownBackgroundDrawable(Theme.getDrawable(context, id));
            }
            if (Build.VERSION.SDK_INT >= 21 && view instanceof Toolbar) {
                Toolbar toolbar = (Toolbar) view;
                int id = WidgetAttribute.TITLE_TEXT_COLOR.resourceId(a);
                if (id != 0) toolbar.setTitleTextColor(Theme.getColor(context, id));
                id = WidgetAttribute.SUBTITLE_TEXT_COLOR.resourceId(a);
                if (id != 0) toolbar.setSubtitleTextColor(Theme.getColor(context, id));
            }
        } finally {
            a.recycle();
        }
    }

    private static void applyText(TextView view, TypedArray a) {
        Context context = view.getContext();
        int id = WidgetAttribute.TEXT_COLOR.resourceId(a);
        if (id != 0) view.setTextColor(Theme.getColorStateList(context, id));
        id = WidgetAttribute.TEXT_COLOR_HINT.resourceId(a);
        if (id != 0) view.setHintTextColor(Theme.getColorStateList(context, id));
        id = WidgetAttribute.TEXT_COLOR_LINK.resourceId(a);
        if (id != 0) view.setLinkTextColor(Theme.getColorStateList(context, id));
        id = WidgetAttribute.TEXT_SIZE.resourceId(a);
        if (id != 0) view.setTextSize(TypedValue.COMPLEX_UNIT_PX, Theme.getDimen(context, id));
        id = WidgetAttribute.FONT.resourceId(a);
        String name = id != 0 && Theme.FONT.equals(context.getResources().getResourceTypeName(id))
                ? context.getResources().getResourceEntryName(id) : WidgetAttribute.FONT.string(a);
        Typeface font = name == null ? Theme.getFont(context) : Theme.getFont(context, name);
        if (font != null) view.setTypeface(font);
    }
}
