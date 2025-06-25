package com.example.saludaldia;


import android.content.Context;
import android.content.SharedPreferences;
import com.example.saludaldia.utils.FontScaleContextWrapper;
import com.example.saludaldia.utils.ThemeHelper;

import android.app.Application;

public class MyAplication extends Application {

        @Override
        protected void attachBaseContext(Context base) {
            SharedPreferences prefs = base.getSharedPreferences("settings_prefs", MODE_PRIVATE);
            boolean initialDarkMode = prefs.getBoolean("dark_mode", false);
            ThemeHelper.applyTheme(initialDarkMode);
            Context wrappedContext = FontScaleContextWrapper.wrap(base);
            super.attachBaseContext(wrappedContext);
        }

        @Override
        public void onCreate() {
            super.onCreate();
        }
}
