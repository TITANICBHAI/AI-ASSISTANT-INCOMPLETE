package com.aiassistant.ui.settings;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;
import androidx.preference.PreferenceViewHolder;
import androidx.preference.SeekBarPreference;

public class CustomSeekBarPreference extends SeekBarPreference {
    private TextView valueTextView;

    public CustomSeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public CustomSeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomSeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomSeekBarPreference(Context context) {
        super(context);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        // The code to update the text view with the current value would go here if needed
    }
}
