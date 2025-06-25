package com.example.saludaldia.utils;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.res.Configuration;

public class FontScaleContextWrapper extends ContextWrapper {
    public FontScaleContextWrapper(Context base) {
        super(base);
    }

    public static Context wrap(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("settings_prefs", MODE_PRIVATE);
        String fontSize = prefs.getString("font_size", "medium");

        float scale;
        switch (fontSize) {
            case "small":
                scale = 0.85f;
                break;
            case "large":
                scale = 1.3f;
                break;
            default: // medium
                scale = 1.0f;
        }

        Configuration configuration = context.getResources().getConfiguration();
        configuration.fontScale = scale;

        return context.createConfigurationContext(configuration);
    }
}
