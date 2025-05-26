package com.example.saludaldia.utils;

import androidx.appcompat.app.AppCompatDelegate;

public class ThemeHelper {
    public static void applyTheme(boolean darkModeEnabled) {
        AppCompatDelegate.setDefaultNightMode(
                darkModeEnabled ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }
}