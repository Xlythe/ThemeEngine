package com.xlythe.engine.theme;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

// Preserve the public framework constructors, including defStyleRes.
@android.annotation.SuppressLint("AppCompatCustomView")
public class ThemedAutoCompleteTextView extends AutoCompleteTextView {
    public ThemedAutoCompleteTextView(Context context) {
        super(context);
        ThemedWidgets.apply(this, null, android.R.attr.autoCompleteTextViewStyle, 0);
    }

    public ThemedAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        ThemedWidgets.apply(this, attrs, android.R.attr.autoCompleteTextViewStyle, 0);
    }

    public ThemedAutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ThemedWidgets.apply(this, attrs, defStyleAttr, 0);
    }

    @TargetApi(21)
    public ThemedAutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        ThemedWidgets.apply(this, attrs, defStyleAttr, defStyleRes);
    }

    @TargetApi(24)
    public ThemedAutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes, android.content.res.Resources.Theme popupTheme) {
        super(context, attrs, defStyleAttr, defStyleRes, popupTheme);
        ThemedWidgets.apply(this, attrs, defStyleAttr, defStyleRes);
    }
}
