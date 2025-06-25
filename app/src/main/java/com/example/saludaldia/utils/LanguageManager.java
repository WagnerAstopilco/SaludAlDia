package com.example.saludaldia.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.preference.PreferenceManager;
import java.util.Locale;

public class LanguageManager {
    private static final String PREF_LANG = "pref_language";
    private static final String DEFAULT_LANG = "es";

    public static Context setLocale(Context context) {
        return updateResources(context, getLanguage(context));
    }

    public static Context setLocale(Context context, String language) {
        persistLanguage(context, language);
        return updateResources(context, language);
    }

    public static String getLanguage(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(PREF_LANG, DEFAULT_LANG);
    }

    private static void persistLanguage(Context context, String language) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PREF_LANG, language);
        editor.apply();
    }

    @SuppressWarnings("deprecation")
    private static Context updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale);
            context = context.createConfigurationContext(configuration);
        } else {
            configuration.locale = locale;
            resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        }
        return context;
    }
}
