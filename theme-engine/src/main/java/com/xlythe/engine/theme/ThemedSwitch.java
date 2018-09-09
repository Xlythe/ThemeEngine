package com.xlythe.engine.theme;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;

import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.appcompat.widget.SwitchCompat;

public class ThemedSwitch extends SwitchCompat {
    public ThemedSwitch(Context context) {
        super(context);
        setup(context, null);
    }

    public ThemedSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup(context, attrs);
    }

    public ThemedSwitch(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setup(context, attrs);
    }

    private void setup(Context context, @Nullable AttributeSet attrs) {
        // Get font
        setDefaultFont();

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.theme);
            if (a != null) {
                // Get text color
                setTextColor(Theme.get(context, a.getResourceId(R.styleable.theme_textColor, 0)));

                // Get text hint color
                setHintTextColor(Theme.get(context, a.getResourceId(R.styleable.theme_textColorHint, 0)));

                // Get text link color
                setLinkTextColor(Theme.get(context, a.getResourceId(R.styleable.theme_textColorLink, 0)));

                // Get button
                setButtonDrawable(Theme.get(context, a.getResourceId(R.styleable.theme_button, 0)));

                // Get background
                setBackground(Theme.get(context, a.getResourceId(R.styleable.theme_themeBackground, 0)));

                // Get custom font. Note that in v26, /res/font was added so we need to play some games.
                if (Build.VERSION.SDK_INT >= 21) {
                    switch (a.getType(R.styleable.theme_themeFont)) {
                        case TypedValue.TYPE_ATTRIBUTE:
                            setFont(Theme.get(context, a.getResourceId(R.styleable.theme_themeFont, 0)));
                            break;
                        case TypedValue.TYPE_STRING:
                            setFont(a.getString(R.styleable.theme_themeFont));
                            break;
                    }
                }

                a.recycle();
            }
        }
    }

    @UiThread
    public void setDefaultFont() {
        Typeface t = Theme.getFont(getContext());
        if (t != null) {
            setTypeface(t);
        }
    }

    @UiThread
    public void setFont(@Nullable Theme.Res res) {
        if (res != null) {
            if (Theme.FONT.equals(res.getType())) {
                Typeface t = Theme.getFont(getContext(), res);
                if (t != null) {
                    setTypeface(t);
                }
            }
        }
    }

    @UiThread
    @Deprecated
    public void setFont(@Nullable String font) {
        if (font != null) {
            Typeface t = Theme.getFont(getContext(), font);
            if (t != null) {
                setTypeface(t);
            }
        }
    }

    @UiThread
    public void setTextColor(@Nullable Theme.Res res) {
        if (res != null) {
            if (Theme.COLOR.equals(res.getType())) {
                setTextColor(Theme.getColorStateList(getContext(), res.getName()));
            }
        }
    }

    @UiThread
    public void setHintTextColor(@Nullable Theme.Res res) {
        if (res != null) {
            if (Theme.COLOR.equals(res.getType())) {
                setHintTextColor(Theme.getColorStateList(getContext(), res.getName()));
            }
        }
    }

    @UiThread
    public void setLinkTextColor(@Nullable Theme.Res res) {
        if (res != null) {
            if (Theme.COLOR.equals(res.getType())) {
                setLinkTextColor(Theme.getColorStateList(getContext(), res.getName()));
            }
        }
    }

    @UiThread
    public void setButtonDrawable(@Nullable Theme.Res res) {
        if (res != null) {
            if (Theme.DRAWABLE.equals(res.getType())) {
                setButtonDrawable(Theme.getDrawable(getContext(), res.getName()));
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
