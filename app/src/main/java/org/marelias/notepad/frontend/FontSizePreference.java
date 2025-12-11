package org.marelias.notepad.frontend;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.TextView;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import org.marelias.notepad.R;

public class FontSizePreference extends Preference {
    private static final int MIN_SIZE = 1;
    private static final int MAX_SIZE = 36;
    private static final int DEFAULT_SIZE = 18;

    private TextView tvFontSize;
    private Button btnDecrease;
    private Button btnIncrease;
    private int currentSize;

    public FontSizePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.preference_font_size);
        setDefaultValue(DEFAULT_SIZE);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        tvFontSize = (TextView) holder.findViewById(R.id.tv_font_size);
        btnDecrease = (Button) holder.findViewById(R.id.btn_decrease);
        btnIncrease = (Button) holder.findViewById(R.id.btn_increase);

        // Load current font size
        SharedPreferences prefs = getSharedPreferences();
        if (!prefs.contains(getKey())) {
            persistInt(DEFAULT_SIZE);
        }
        currentSize = prefs.getInt(getKey(), DEFAULT_SIZE);
        updateDisplay();

        // Set up button listeners
        btnDecrease.setOnClickListener(v -> {
            if (currentSize > MIN_SIZE) {
                currentSize--;
                persistInt(currentSize);
                updateDisplay();
                notifyChanged();
            }
        });

        btnIncrease.setOnClickListener(v -> {
            if (currentSize < MAX_SIZE) {
                currentSize++;
                persistInt(currentSize);
                updateDisplay();
                notifyChanged();
            }
        });
    }

    private void updateDisplay() {
        if (tvFontSize != null) {
            tvFontSize.setText("Font size: " + currentSize);
        }
        if (btnDecrease != null) {
            btnDecrease.setEnabled(currentSize > MIN_SIZE);
        }
        if (btnIncrease != null) {
            btnIncrease.setEnabled(currentSize < MAX_SIZE);
        }
    }
}
