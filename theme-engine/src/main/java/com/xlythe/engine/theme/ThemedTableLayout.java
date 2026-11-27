package com.xlythe.engine.theme;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.TableLayout;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

public class ThemedTableLayout extends TableLayout {
    public ThemedTableLayout(Context context) {
        super(context);
        setup(context, null, 0, 0);
    }

    public ThemedTableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup(context, attrs, 0, 0);
    }

    public ThemedTableLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(WidgetStyle.withDefaultStyle(context, defStyleAttr, 0), attrs);
        setup(getContext(), attrs, defStyleAttr, 0);
    }

    @TargetApi(21)
    public ThemedTableLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(WidgetStyle.withDefaultStyle(context, defStyleAttr, defStyleRes), attrs);
        setup(getContext(), attrs, defStyleAttr, defStyleRes);
    }

    private void setup(
            Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.theme, defStyleAttr, defStyleRes);
        try {
            // Get divider
            setDivider(Theme.get(context, a.getResourceId(R.styleable.theme_themedDivider, 0)));

            // Get background
            setBackground(Theme.get(context, a.getResourceId(R.styleable.theme_themedBackground, 0)));
        } finally {
            a.recycle();
        }
    }

    @UiThread
    @SuppressLint("NewApi")
    public void setDivider(@Nullable Theme.Res res) {
        if (res != null) {
            if (Theme.DRAWABLE.equals(res.getType())) {
                if (Build.VERSION.SDK_INT >= 11) {
                    setDividerDrawable(Theme.getDrawable(getContext(), res));
                }
            }
        }
    }

    @UiThread
    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    public void setBackground(@Nullable Theme.Res res) {
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
