package com.xlythe.engine.theme;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ProgressBar;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

public class ThemedProgressBar extends ProgressBar {
    private final WidgetStyle.Dimensions dimensions = new WidgetStyle.Dimensions();

    public ThemedProgressBar(Context context) {
        super(context);
        setup(getContext(), null, android.R.attr.progressBarStyle, 0);
    }

    public ThemedProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup(getContext(), attrs, android.R.attr.progressBarStyle, 0);
    }

    public ThemedProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(21)
    public ThemedProgressBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setup(context, attrs, defStyleAttr, defStyleRes);
    }

    private void setup(
            Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray a = WidgetStyle.obtainAttributes(context, attrs, defStyleAttr, defStyleRes);
        try {
            // Get background
            setBackground(Theme.get(context, WidgetAttribute.BACKGROUND.resourceId(a)));

            // Get progress drawable
            setProgressDrawable(
                    Theme.get(context, WidgetAttribute.PROGRESS_DRAWABLE.resourceId(a)));
        } finally {
            a.recycle();
        }
    }

    @UiThread
    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    public void setBackground(Theme.Res res) {
        if (res != null) {
            if (Theme.COLOR.equals(res.getType())) {
                setBackgroundColor(Theme.getColor(getContext(), res));
            } else if (Theme.DRAWABLE.equals(res.getType())) {
                if (Build.VERSION.SDK_INT < 16) {
                    setBackgroundDrawable(Theme.getDrawable(getContext(), res));
                } else {
                    setBackground(Theme.getDrawable(getContext(), res));
                }
            }
        }
    }

    @UiThread
    public void setProgressDrawable(Theme.Res res) {
        if (res != null) {
            if (Theme.DRAWABLE.equals(res.getType())) {
                setProgressDrawable(Theme.getDrawable(getContext(), res));
            }
        }
    }

    @UiThread
    public void setWidth(@Nullable Theme.Res res) {
        dimensions.setWidth(this, res);
    }

    @UiThread
    public void setHeight(@Nullable Theme.Res res) {
        dimensions.setHeight(this, res);
    }

    @Override
    public void setLayoutParams(android.view.ViewGroup.LayoutParams params) {
        if (dimensions != null) dimensions.applyTo(params);
        super.setLayoutParams(params);
    }
}
