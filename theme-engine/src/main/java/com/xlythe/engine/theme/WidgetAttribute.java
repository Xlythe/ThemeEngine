package com.xlythe.engine.theme;

import android.content.res.TypedArray;

/** The two public XML names for each externally themed widget property. */
enum WidgetAttribute {
    BUTTON(R.styleable.theme_themedButton, R.styleable.theme_button),
    BUTTON_TINT(R.styleable.theme_themedButtonTint, R.styleable.theme_buttonTint),
    TEXT_SIZE(R.styleable.theme_themedTextSize, R.styleable.theme_textSize),
    TEXT_COLOR(R.styleable.theme_themedTextColor, R.styleable.theme_textColor),
    TEXT_COLOR_HINT(R.styleable.theme_themedTextColorHint, R.styleable.theme_textColorHint),
    TEXT_COLOR_LINK(R.styleable.theme_themedTextColorLink, R.styleable.theme_textColorLink),
    SRC(R.styleable.theme_themedSrc, R.styleable.theme_src),
    DIVIDER(R.styleable.theme_themedDivider, R.styleable.theme_divider),
    BACKGROUND(R.styleable.theme_themedBackground, R.styleable.theme_background),
    PROGRESS_DRAWABLE(R.styleable.theme_themedProgressDrawable, R.styleable.theme_progressDrawable),
    POPUP_BACKGROUND(R.styleable.theme_themedPopupBackground, R.styleable.theme_popupBackground),
    FONT(R.styleable.theme_themedFont, R.styleable.theme_font),
    FONT_FAMILY(R.styleable.theme_themedFontFamily, R.styleable.theme_fontFamily),
    TEXT_STYLE(R.styleable.theme_themedTextStyle, R.styleable.theme_textStyle),
    TINT(R.styleable.theme_themedTint, R.styleable.theme_tint),
    THUMB(R.styleable.theme_themedThumb, R.styleable.theme_thumb),
    INDETERMINATE_DRAWABLE(R.styleable.theme_themedIndeterminateDrawable, R.styleable.theme_indeterminateDrawable),
    QUERY_HINT(R.styleable.theme_themedQueryHint, R.styleable.theme_queryHint),
    DROP_DOWN_BACKGROUND(R.styleable.theme_themedDropDownBackground, R.styleable.theme_dropDownBackground),
    TITLE_TEXT_COLOR(R.styleable.theme_themedTitleTextColor, R.styleable.theme_titleTextColor),
    SUBTITLE_TEXT_COLOR(R.styleable.theme_themedSubtitleTextColor, R.styleable.theme_subtitleTextColor);

    private final int prefixed;
    private final int shorthand;

    WidgetAttribute(int prefixed, int shorthand) {
        this.prefixed = prefixed;
        this.shorthand = shorthand;
    }

    private int index(TypedArray attributes) {
        return attributes.hasValue(prefixed) ? prefixed : shorthand;
    }

    int resourceId(TypedArray attributes) {
        return attributes.getResourceId(index(attributes), 0);
    }

    String string(TypedArray attributes) {
        return attributes.getString(index(attributes));
    }
}
