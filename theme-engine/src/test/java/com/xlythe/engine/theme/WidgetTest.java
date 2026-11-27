package com.xlythe.engine.theme;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.ContextThemeWrapper;
import android.widget.LinearLayout;
import android.widget.Spinner;
import androidx.core.widget.ImageViewCompat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {21, 28, 33})
public class WidgetTest {
    private final Context context = new ContextThemeWrapper(RuntimeEnvironment.getApplication(),
            androidx.appcompat.R.style.Theme_AppCompat);

    private int id(String name, String type) {
        int id = context.getResources().getIdentifier(name, type, context.getPackageName());
        assertNotEquals(name, 0, id);
        return id;
    }

    @Test public void stylesApplyWithoutXmlAndPreservePlatformDefaults() {
        int style = id("WidgetTestDefaults", "style");
        ThemedTextView text = new ThemedTextView(context, null, 0, style);
        assertEquals(37f, text.getTextSize(), 0.01f);
        ThemedTextView nativeStyle = new ThemedTextView(context, null, 0, id("NativeWidgetTestStyle", "style"));
        assertEquals(47f, nativeStyle.getTextSize(), 0.01f);
        assertEquals("Find albums", new ThemedSearchView(context, null, 0, style).getQueryHint());
        assertEquals(37f, new ThemedAutoCompleteTextView(context, null, 0, style).getTextSize(), 0.01f);
        assertNotNull(new ThemedSeekBar(context));
        assertNotNull(new ThemedRatingBar(context));
        assertNotNull(new ThemedToolbar(context));
    }

    @Test public void xmlInflationAppliesWidgetSpecificResources() {
        LinearLayout layout = (LinearLayout) android.view.LayoutInflater.from(context)
                .inflate(id("widget_test_controls", "layout"), null);
        assertEquals(13, ((ThemedSeekBar) layout.getChildAt(0)).getThumb().getIntrinsicWidth());
        assertEquals(7, ((ThemedRatingBar) layout.getChildAt(1)).getProgressDrawable().getIntrinsicHeight());
        assertEquals("Find albums", ((ThemedSearchView) layout.getChildAt(2)).getQueryHint());
    }

    @Test public void shortNamesInflateAndPrefixedButtonWinsWhenBothAreSet() {
        LinearLayout layout = (LinearLayout) android.view.LayoutInflater.from(context)
                .inflate(id("widget_test_aliases", "layout"), null);
        for (int i = 0; i < 2; i++) {
            ThemedCheckBox box = (ThemedCheckBox) layout.getChildAt(i);
            assertEquals(13, androidx.core.widget.CompoundButtonCompat.getButtonDrawable(box).getIntrinsicWidth());
        }
        assertEquals(13, ((ThemedSeekBar) layout.getChildAt(2)).getThumb().getIntrinsicWidth());
        assertEquals("Find albums", ((ThemedSearchView) layout.getChildAt(3)).getQueryHint());
        ThemedCheckBox platform = (ThemedCheckBox) layout.getChildAt(4);
        assertEquals(context.getResources().getDrawable(android.R.drawable.btn_star).getIntrinsicWidth(),
                androidx.core.widget.CompoundButtonCompat.getButtonDrawable(platform).getIntrinsicWidth());
    }

    @Test public void everyAttributeAcceptsBothNamesAndPrefersThePrefixedValue() {
        for (WidgetAttribute attribute : WidgetAttribute.values()) {
            StringBuilder suffix = new StringBuilder();
            for (String part : attribute.name().toLowerCase(java.util.Locale.ROOT).split("_")) {
                suffix.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
            }
            int prefixed = id("themed" + suffix, "attr");
            int shortName = id(Character.toLowerCase(suffix.charAt(0)) + suffix.substring(1), "attr");
            int first = android.R.drawable.btn_star;
            int second = id("widget_test_drawable", "drawable");
            for (int mask = 1; mask <= 3; mask++) {
                org.robolectric.android.AttributeSetBuilder builder = org.robolectric.Robolectric.buildAttributeSet();
                if ((mask & 1) != 0) builder.addAttribute(shortName, "@android:drawable/btn_star");
                if ((mask & 2) != 0) builder.addAttribute(prefixed, "@drawable/widget_test_drawable");
                android.content.res.TypedArray values = context.obtainStyledAttributes(builder.build(), R.styleable.theme);
                try {
                    assertEquals(attribute.name(), mask == 1 ? first : second, attribute.resourceId(values));
                } finally {
                    values.recycle();
                }
            }
        }
    }

    @Test public void dimensionsApplyToParentParametersOnlyOnce() {
        ThemedView view = new ThemedView(context);
        Theme.Res size = Theme.get(context, id("widget_test_width", "dimen"));
        view.setWidth(size);
        view.setHeight(size);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(1, 2, 3);
        new LinearLayout(context).addView(view, params);
        assertSame(params, view.getLayoutParams());
        assertEquals(37, params.width);
        assertEquals(37, params.height);
        params.width = 9;
        view.setLayoutParams(params);
        assertEquals(9, params.width);
    }

    @Test public void tintRetainsPressedStateAndSpinnerRetainsModeAndPopupTheme() {
        int style = id("WidgetTestDefaults", "style");
        ThemedImageView image = new ThemedImageView(context, null, 0, style);
        ColorStateList tint = ImageViewCompat.getImageTintList(image);
        assertNotNull(tint);
        assertEquals(Color.BLUE, tint.getDefaultColor());
        assertEquals(Color.RED, tint.getColorForState(new int[]{android.R.attr.state_pressed}, 0));
        ThemedSpinner spinner = new ThemedSpinner(context, null, 0, style, Spinner.MODE_DROPDOWN, context.getTheme());
        assertNotNull(spinner.getPopupContext());
        assertNotNull(new ThemedSpinner(context, Spinner.MODE_DIALOG));
    }
}
