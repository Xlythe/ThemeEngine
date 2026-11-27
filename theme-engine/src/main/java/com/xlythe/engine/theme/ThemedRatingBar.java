package com.xlythe.engine.theme;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.RatingBar;

// Preserve the public framework constructors, including defStyleRes.
@android.annotation.SuppressLint("AppCompatCustomView")
public class ThemedRatingBar extends RatingBar {
    public ThemedRatingBar(Context context) {
        super(context);
        ThemedWidgets.apply(this, null, android.R.attr.ratingBarStyle, 0);
    }

    public ThemedRatingBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        ThemedWidgets.apply(this, attrs, android.R.attr.ratingBarStyle, 0);
    }

    public ThemedRatingBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ThemedWidgets.apply(this, attrs, defStyleAttr, 0);
    }

    @TargetApi(21)
    public ThemedRatingBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        ThemedWidgets.apply(this, attrs, defStyleAttr, defStyleRes);
    }
}
