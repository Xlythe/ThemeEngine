package com.xlythe.engine.theme;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ListView;

public class ThemedListView extends ListView {
    public ThemedListView(Context context) {
        super(context);
        setup(context, null);
    }

    public ThemedListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup(context, attrs);
    }

    public ThemedListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setup(context, attrs);
    }

    @TargetApi(21)
    public ThemedListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setup(context, attrs);
    }

    private void setup(Context context, AttributeSet attrs) {
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

    public void setDivider(@Nullable Theme.Res res) {
        if (res != null) {
            if (Theme.DRAWABLE.equals(res.getType())) {
                try {
                    setDivider(Theme.getDrawable(getContext(), res.getName()));
                } catch (Exception e) {
                    // Not supported on some phones...
                    e.printStackTrace();
                }
            }
        }
    }

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
}
