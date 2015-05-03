package com.gonimah.subwayalerts.widgets;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class StyledTextView extends TextView {
    public StyledTextView(Context context) {
        super(context);
        Typeface fontVerano = Typeface.createFromAsset(context.getAssets(), "fonts/Verano/Verano.otf");
        setTypeface(fontVerano);
    }

    public StyledTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Typeface fontVerano = Typeface.createFromAsset(context.getAssets(), "fonts/Verano/Verano.otf");
        setTypeface(fontVerano);
    }

    public StyledTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Typeface fontVerano = Typeface.createFromAsset(context.getAssets(), "fonts/Verano/Verano.otf");
        setTypeface(fontVerano);
    }

    @TargetApi(21)
    public StyledTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        Typeface fontVerano = Typeface.createFromAsset(context.getAssets(), "fonts/Verano/Verano.otf");
        setTypeface(fontVerano);
    }
}
