package com.xlythe.engine.theme;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

public class ThemedRadioGroup extends RadioGroup {
    public ThemedRadioGroup(Context context) {
        super(context);
        setup(context, null);
    }

    public ThemedRadioGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup(context, attrs);
    }

    private void setup(Context context, @Nullable AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.theme);
            if (a != null) {
                // Get divider
                setDivider(Theme.get(context, a.getResourceId(R.styleable.theme_themeDivider, 0)));

                // Get background
                setBackground(Theme.get(context, a.getResourceId(R.styleable.theme_themeBackground, 0)));

                a.recycle();
            }
        }
    }

    @UiThread
    @SuppressLint("NewApi")
    public void setDivider(@Nullable Theme.Res res) {
        if (res != null) {
            if (Theme.DRAWABLE.equals(res.getType())) {
                if (Build.VERSION.SDK_INT >= 11) {
                    setDividerDrawable(Theme.getDrawable(getContext(), res.getName()));
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
                setBackgroundColor(Theme.getColor(getContext(), res.getName()));
            } else if (Theme.DRAWABLE.equals(res.getType())) {
                if (Build.VERSION.SDK_INT < 16) {
                    setBackgroundDrawable(Theme.getDrawable(getContext(), res.getName()));
                } else {
                    setBackground(Theme.getDrawable(getContext(), res.getName()));
                }
            }
        }
    }

    @UiThread
    public void setWidth(@Nullable Theme.Res res) {
        if (res != null) {
            if (Theme.DIMEN.equals(res.getType())) {
                getLayoutParams().width = Theme.getDimen(getContext(), res).intValue();
                requestLayout();
            }
        }
    }

    @UiThread
    public void setHeight(@Nullable Theme.Res res) {
        if (res != null) {
            if (Theme.DIMEN.equals(res.getType())) {
                getLayoutParams().height = Theme.getDimen(getContext(), res).intValue();
                requestLayout();
            }
        }
    }
}
