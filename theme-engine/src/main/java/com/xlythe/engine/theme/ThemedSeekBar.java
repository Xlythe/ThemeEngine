package com.xlythe.engine.theme;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.SeekBar;

// Preserve the public framework constructors, including defStyleRes.
@android.annotation.SuppressLint("AppCompatCustomView")
public class ThemedSeekBar extends SeekBar {
    public ThemedSeekBar(Context context) {
        super(context);
        ThemedWidgets.apply(this, null, android.R.attr.seekBarStyle, 0);
    }

    public ThemedSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        ThemedWidgets.apply(this, attrs, android.R.attr.seekBarStyle, 0);
    }

    public ThemedSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ThemedWidgets.apply(this, attrs, defStyleAttr, 0);
    }

    @TargetApi(21)
    public ThemedSeekBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        ThemedWidgets.apply(this, attrs, defStyleAttr, defStyleRes);
    }
}
