package com.xlythe.engine.theme;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.GridLayout;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

public class ThemedGridLayout extends GridLayout {
    private final WidgetStyle.Dimensions dimensions = new WidgetStyle.Dimensions();

    public ThemedGridLayout(Context context) {
        super(context);
        setup(context, null, 0, 0);
    }

    public ThemedGridLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup(context, attrs, 0, 0);
    }

    public ThemedGridLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(21)
    public ThemedGridLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setup(context, attrs, defStyleAttr, defStyleRes);
    }

    private void setup(
            Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray a = WidgetStyle.obtainAttributes(context, attrs, defStyleAttr, defStyleRes);
        try {
            // Get background
            setBackground(Theme.get(context, WidgetAttribute.BACKGROUND.resourceId(a)));
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
