package com.xlythe.engine.theme;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.SearchView;

public class ThemedSearchView extends SearchView {
    public ThemedSearchView(Context context) {
        super(context);
        ThemedWidgets.apply(this, null, android.os.Build.VERSION.SDK_INT >= 21 ? android.R.attr.searchViewStyle : 0, 0);
    }

    public ThemedSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        ThemedWidgets.apply(this, attrs, android.os.Build.VERSION.SDK_INT >= 21 ? android.R.attr.searchViewStyle : 0, 0);
    }

    @TargetApi(21)
    public ThemedSearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ThemedWidgets.apply(this, attrs, defStyleAttr, 0);
    }

    @TargetApi(21)
    public ThemedSearchView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        ThemedWidgets.apply(this, attrs, defStyleAttr, defStyleRes);
    }
}
