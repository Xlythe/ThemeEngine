package com.xlythe.engine.theme;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.EditText;

public class ThemedEditText extends EditText {
    public ThemedEditText(Context context) {
        super(context);
        setup(context, null);
    }

    public ThemedEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup(context, attrs);
    }

    public ThemedEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setup(context, attrs);
    }

    @TargetApi(21)
    public ThemedEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setup(context, attrs);
    }

    private void setup(Context context, AttributeSet attrs) {
        // Get font
        setDefaultFont();

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.theme);
            if (a != null) {
                // Get text color
                setTextColor(Theme.get(context, a.getResourceId(R.styleable.theme_textColor, 0)));

                // Get text hint color
                setHintTextColor(Theme.get(context, a.getResourceId(R.styleable.theme_textColorHint, 0)));

                // Get background
                setBackground(Theme.get(context, a.getResourceId(R.styleable.theme_themeBackground, 0)));

                // Get custom font
                setFont(a.getString(R.styleable.theme_font));

                a.recycle();
            }
        }
    }

    public void setDefaultFont() {
        Typeface t = Theme.getFont(getContext());
        if (t != null) {
            setTypeface(t);
        }
    }

    public void setFont(@Nullable String font) {
        if (font != null) {
            Typeface t = Theme.getFont(getContext(), font);
            if (t != null) {
                setTypeface(t);
            }
        }
    }

    public void setTextColor(@Nullable Theme.Res res) {
        if (res != null) {
            if (Theme.COLOR.equals(res.getType())) {
                setTextColor(Theme.getColorStateList(getContext(), res.getName()));
            }
        }
    }

    public void setHintTextColor(@Nullable Theme.Res res) {
        if (res != null) {
            if (Theme.COLOR.equals(res.getType())) {
                setHintTextColor(Theme.getColorStateList(getContext(), res.getName()));
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
                if (android.os.Build.VERSION.SDK_INT < 16) {
                    setBackgroundDrawable(Theme.getDrawable(getContext(), res.getName()));
                } else {
                    setBackground(Theme.getDrawable(getContext(), res.getName()));
                }
            }
        }
    }
}
