package com.xlythe.engine.theme;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.Toolbar;

@TargetApi(21)
public class ThemedToolbar extends Toolbar {
    public ThemedToolbar(Context context) {
        super(context);
        ThemedWidgets.apply(this, null, android.R.attr.toolbarStyle, 0);
    }

    public ThemedToolbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        ThemedWidgets.apply(this, attrs, android.R.attr.toolbarStyle, 0);
    }

    public ThemedToolbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ThemedWidgets.apply(this, attrs, defStyleAttr, 0);
    }

    @TargetApi(21)
    public ThemedToolbar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        ThemedWidgets.apply(this, attrs, defStyleAttr, defStyleRes);
    }
}
