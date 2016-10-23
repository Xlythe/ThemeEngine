package com.xlythe.engine.theme;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.Spinner;

public class ThemedSpinner extends Spinner {
    public ThemedSpinner(Context context) {
        super(context);
        setup(context, null);
    }

    public ThemedSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup(context, attrs);
    }

    @SuppressLint("NewApi")
    public ThemedSpinner(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setup(context, attrs);
    }

    private void setup(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.theme);
            if (a != null) {
                // Get background
                setBackground(Theme.get(context, a.getResourceId(R.styleable.theme_themeBackground, 0)));

                // Get popup background
                setPopupBackground(Theme.get(context, a.getResourceId(R.styleable.theme_popupBackground, 0)));

                a.recycle();
            }
        }
    }

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    public void setBackground(Theme.Res res) {
        if (res != null) {
            if (Theme.COLOR.equals(res.getType())) {
                setBackgroundColor(Theme.getColor(getContext(), res.getName()));
            } else if (Theme.DRAWABLE.equals(res.getType())) {
                if (android.os.Build.VERSION.SDK_INT < 16) {
                    setBackgroundDrawable(Theme.getDrawable(getContext(), res.getName()));
                } else {
                    setBackground(Theme.getDrawable(getContext(), res.getName()));
                }
            }
        }
    }

    @TargetApi(16)
    public void setPopupBackground(Theme.Res res) {
        if (res != null) {
            if (android.os.Build.VERSION.SDK_INT >= 16) {
                setPopupBackgroundDrawable(Theme.getDrawable(getContext(), res.getName()));
            }
        }
    }
}
