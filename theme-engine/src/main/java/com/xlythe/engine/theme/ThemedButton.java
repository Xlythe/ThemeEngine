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
    private final WidgetStyle.Dimensions dimensions = new WidgetStyle.Dimensions();

  public ThemedButton(Context context) {
    super(context);
    setup(getContext(), null, androidx.appcompat.R.attr.buttonStyle, 0);
  }

  public ThemedButton(Context context, AttributeSet attrs) {
    super(context, attrs);
    setup(getContext(), attrs, androidx.appcompat.R.attr.buttonStyle, 0);
  }

  public ThemedButton(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    setup(context, attrs, defStyleAttr, 0);
  }

  @TargetApi(21)
  public ThemedButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(WidgetStyle.withFallback(context, defStyleAttr, defStyleRes), attrs, defStyleAttr);
    setup(getContext(), attrs, defStyleAttr, defStyleRes);
  }

  private void setup(
          Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    // Get font
    setDefaultFont();

    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.theme, defStyleAttr, defStyleRes);
    try {
      // Get text color
      setTextColor(Theme.get(context, WidgetAttribute.TEXT_COLOR.resourceId(a)));

      // Get text hint color
      setHintTextColor(
              Theme.get(context, WidgetAttribute.TEXT_COLOR_HINT.resourceId(a)));

      // Get text link color
      setLinkTextColor(
              Theme.get(context, WidgetAttribute.TEXT_COLOR_LINK.resourceId(a)));

      // Get text size
      setTextSize(Theme.get(context, WidgetAttribute.TEXT_SIZE.resourceId(a)));

      // Get background
      setBackground(Theme.get(context, WidgetAttribute.BACKGROUND.resourceId(a)));

      // Get custom font
      setupFont(context, a);
    } finally {
      a.recycle();
    }
  }

  private void setupFont(Context context, TypedArray a) {
    int id = WidgetAttribute.FONT.resourceId(a);
    if (id != 0 && !Theme.STRING.equals(context.getResources().getResourceTypeName(id))) {
      setFont(Theme.get(context, id));
      return;
    }
    String font = WidgetAttribute.FONT.string(a);
    if (font != null) {
      setFont(font);
      return;
    }

    // If no font was specified, look up the text style.
    setTextStyle(
            Theme.get(context, WidgetAttribute.FONT_FAMILY.resourceId(a)),
            Theme.get(context, WidgetAttribute.TEXT_STYLE.resourceId(a)));
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
