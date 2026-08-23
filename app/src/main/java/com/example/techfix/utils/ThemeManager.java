package com.example.techfix.utils;

import android.content.Context;
import androidx.appcompat.app.AppCompatDelegate;

public final class ThemeManager {
    private static final String PREFS = "TechFixSettings";
    private static final String KEY_DARK_MODE = "darkMode";

    private ThemeManager() { }

    public static void applySavedTheme(Context context) {
        boolean darkMode = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getBoolean(KEY_DARK_MODE, true);
        AppCompatDelegate.setDefaultNightMode(darkMode
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);
    }

    public static boolean isDarkMode(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getBoolean(KEY_DARK_MODE, true);
    }

    public static void setDarkMode(Context context, boolean darkMode) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_DARK_MODE, darkMode)
                .apply();
        AppCompatDelegate.setDefaultNightMode(darkMode
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);
    }
}
