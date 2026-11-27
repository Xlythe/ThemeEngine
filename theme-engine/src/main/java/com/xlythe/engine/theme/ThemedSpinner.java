package com.xlythe.engine.theme;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.appcompat.widget.AppCompatSpinner;

public class ThemedSpinner extends AppCompatSpinner {
    private final WidgetStyle.Dimensions dimensions = new WidgetStyle.Dimensions();

    public ThemedSpinner(Context context) {
        super(context);
        setup(getContext(), null, androidx.appcompat.R.attr.spinnerStyle, 0);
    }

    public ThemedSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup(getContext(), attrs, androidx.appcompat.R.attr.spinnerStyle, 0);
    }

    @SuppressLint("NewApi")
    public ThemedSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup(context, attrs, defStyleAttr, 0);
    }

    public ThemedSpinner(Context context, int mode) {
        this(context, null, androidx.appcompat.R.attr.spinnerStyle, mode);
    }

    public ThemedSpinner(Context context, AttributeSet attrs, int defStyleAttr, int mode) {
        this(context, attrs, defStyleAttr, 0, mode);
    }

    public ThemedSpinner(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes, int mode) {
        this(context, attrs, defStyleAttr, defStyleRes, mode, null);
    }

    public ThemedSpinner(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes,
            int mode, @Nullable android.content.res.Resources.Theme popupTheme) {
        super(WidgetStyle.withFallback(context, defStyleAttr, defStyleRes), attrs, defStyleAttr, mode, popupTheme);
        setup(getContext(), attrs, defStyleAttr, defStyleRes);
    }

    private void setup(
            Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.theme, defStyleAttr, defStyleRes);
        try {
            // Get background
            setBackground(Theme.get(context, a.getResourceId(R.styleable.theme_themedBackground, 0)));

            // Get popup background
            setPopupBackground(
                    Theme.get(context, a.getResourceId(R.styleable.theme_themedPopupBackground, 0)));
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
    @TargetApi(16)
    public void setPopupBackground(Theme.Res res) {
        if (res != null) {
            if (Build.VERSION.SDK_INT >= 16) {
                setPopupBackgroundDrawable(Theme.getDrawable(getContext(), res));
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
