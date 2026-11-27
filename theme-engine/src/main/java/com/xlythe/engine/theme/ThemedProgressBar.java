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
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.theme, defStyleAttr, defStyleRes);
        try {
            // Get background
            setBackground(Theme.get(context, a.getResourceId(R.styleable.theme_themedBackground, 0)));

            // Get progress drawable
            setProgressDrawable(
                    Theme.get(context, a.getResourceId(R.styleable.theme_themedProgressDrawable, 0)));
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
        WidgetStyle.setDimension(this, res, R.id.theme_pending_width);
    }

    @UiThread
    public void setHeight(@Nullable Theme.Res res) {
        WidgetStyle.setDimension(this, res, R.id.theme_pending_height);
    }

    @Override
    public void setLayoutParams(android.view.ViewGroup.LayoutParams params) {
        WidgetStyle.applyPendingDimensions(this, params);
        super.setLayoutParams(params);
    }
}
