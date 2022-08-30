package com.xlythe.engine.theme;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ScrollView;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

public class ThemedScrollView extends ScrollView {
    public ThemedScrollView(Context context) {
        super(context);
        setup(context, null, 0, 0);
    }

    public ThemedScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup(context, attrs, 0, 0);
    }

    public ThemedScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(21)
    public ThemedScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setup(context, attrs, defStyleAttr, defStyleRes);
    }

    private void setup(
            Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        if (attrs != null) {
            TypedArray a =
                    context.obtainStyledAttributes(attrs, R.styleable.theme, defStyleAttr, defStyleRes);
            if (a != null) {
                // Get background
                setBackground(Theme.get(context, a.getResourceId(R.styleable.theme_themedBackground, 0)));

                try {
                    if ("none".equals(Theme.getString(getContext(), "requiresFadingEdge"))) {
                        setVerticalFadingEdgeEnabled(false);
                    }
                } catch (IllegalArgumentException e) {
                    // ignored
                }

                a.recycle();
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
        if (res != null) {
            if (Theme.DIMEN.equals(res.getType())) {
                getLayoutParams().width = (int) Theme.getDimen(getContext(), res);
                requestLayout();
            }
        }
    }

    @UiThread
    public void setHeight(@Nullable Theme.Res res) {
        if (res != null) {
            if (Theme.DIMEN.equals(res.getType())) {
                getLayoutParams().height = (int) Theme.getDimen(getContext(), res);
                requestLayout();
            }
        }
    }
}