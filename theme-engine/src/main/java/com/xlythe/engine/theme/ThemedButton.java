package com.xlythe.engine.theme;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;

import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.appcompat.widget.AppCompatButton;

public class ThemedButton extends AppCompatButton {
  public ThemedButton(Context context) {
    super(context);
    setup(context, null, 0, 0);
  }

  public ThemedButton(Context context, AttributeSet attrs) {
    super(context, attrs);
    setup(context, attrs, 0, 0);
  }

  public ThemedButton(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    setup(context, attrs, defStyleAttr, 0);
  }

  @TargetApi(21)
  public ThemedButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr);
    setup(context, attrs, defStyleAttr, defStyleRes);
  }

  private void setup(
          Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    // Get font
    setDefaultFont();

    if (attrs != null) {
      TypedArray a =
              context.obtainStyledAttributes(attrs, R.styleable.theme, defStyleAttr, defStyleRes);
      if (a != null) {
        // Get text color
        setTextColor(Theme.get(context, a.getResourceId(R.styleable.theme_themedTextColor, 0)));

        // Get text hint color
        setHintTextColor(
                Theme.get(context, a.getResourceId(R.styleable.theme_themedTextColorHint, 0)));

        // Get text link color
        setLinkTextColor(
                Theme.get(context, a.getResourceId(R.styleable.theme_themedTextColorLink, 0)));

        // Get text size
        setTextSize(Theme.get(context, a.getResourceId(R.styleable.theme_themedTextSize, 0)));

        // Get background
        setBackground(Theme.get(context, a.getResourceId(R.styleable.theme_themedBackground, 0)));

        // Get custom font
        setupFont(context, a);

        a.recycle();
      }
    }
  }

  private void setupFont(Context context, TypedArray a) {
    if (Build.VERSION.SDK_INT < 21) {
      return;
    }

    // In v26, /res/font was added. Older versions may set a string while newer versions may
    // explicitly reference a font.
    switch (a.getType(R.styleable.theme_themedFont)) {
      case TypedValue.TYPE_ATTRIBUTE:
        setFont(Theme.get(context, a.getResourceId(R.styleable.theme_themedFont, 0)));
        return;
      case TypedValue.TYPE_STRING:
        setFont(a.getString(R.styleable.theme_themedFont));
        return;
    }

    // If no font was specified, look up the text style.
    setTextStyle(
            Theme.get(context, a.getResourceId(R.styleable.theme_themedFontFamily, 0)),
            Theme.get(context, a.getResourceId(R.styleable.theme_themedTextStyle, 0)));
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
  public void setFont(@Nullable String font) {
    if (font != null) {
      Typeface t = Theme.getFont(getContext(), font);
      if (t != null) {
        setTypeface(t);
      }
    }
  }

  private void setTextStyle(@Nullable Theme.Res fontFamilyRes, @Nullable Theme.Res textStyleRes) {
    if (fontFamilyRes == null) {
      return;
    }

    String fontFamily = Theme.getString(getContext(), fontFamilyRes);
    int textStyle =
            textStyleRes == null ? Typeface.NORMAL : Theme.getInt(getContext(), textStyleRes);
    Typeface t = Typeface.create(fontFamily, textStyle);
    if (t != null) {
      setTypeface(t);
    }
  }

  @UiThread
  public void setTextColor(@Nullable Theme.Res res) {
    if (res != null) {
      if (Theme.COLOR.equals(res.getType())) {
        setTextColor(Theme.getColorStateList(getContext(), res));
      }
    }
  }

  @UiThread
  public void setHintTextColor(@Nullable Theme.Res res) {
    if (res != null) {
      if (Theme.COLOR.equals(res.getType())) {
        setHintTextColor(Theme.getColorStateList(getContext(), res));
      }
    }
  }

  @UiThread
  public void setLinkTextColor(@Nullable Theme.Res res) {
    if (res != null) {
      if (Theme.COLOR.equals(res.getType())) {
        setLinkTextColor(Theme.getColorStateList(getContext(), res));
      }
    }
  }

  @UiThread
  public void setTextSize(@Nullable Theme.Res res) {
    if (res != null) {
      if (Theme.DIMEN.equals(res.getType())) {
        setTextSize(TypedValue.COMPLEX_UNIT_PX, Theme.getDimen(getContext(), res));
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