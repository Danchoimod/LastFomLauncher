package org.levimc.launcher.util;

import android.content.Context;
import android.content.SharedPreferences;

public class ThemeBackgroundManager {
    private static final String PREFS_NAME = "ThemePreferences";
    private static final String KEY_SELECTED_THEME = "selected_theme";
    private static final int DEFAULT_THEME = 0; // theme1 (Copper Update)

    private final SharedPreferences prefs;
    private final Context context;

    // Mapping theme indices to drawable resource names
    private static final String[] THEME_DRAWABLES = {
        "theme1", "theme2", "theme3", "theme4", "theme5",
        "theme6", "theme7", "theme8", "theme9", "theme10",
        "theme11", "theme12", "theme13", "theme14", "theme15",
        "theme16", "theme17", "theme18", "theme19", "theme20",
        "theme21", "theme22", "theme23", "theme24", "theme25",
        "theme26", "theme27", "theme28", "theme29", "theme30"
    };

    public ThemeBackgroundManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Save selected theme index
     * @param themeIndex Index of the theme (0-29)
     */
    public void saveSelectedTheme(int themeIndex) {
        if (themeIndex >= 0 && themeIndex < THEME_DRAWABLES.length) {
            prefs.edit().putInt(KEY_SELECTED_THEME, themeIndex).apply();
        }
    }

    /**
     * Get selected theme index
     * @return Index of the selected theme (0-29)
     */
    public int getSelectedThemeIndex() {
        return prefs.getInt(KEY_SELECTED_THEME, DEFAULT_THEME);
    }

    /**
     * Get drawable resource ID for the selected theme
     * @return Resource ID of the selected theme drawable
     */
    public int getSelectedThemeDrawableId() {
        int index = getSelectedThemeIndex();
        return getThemeDrawableId(index);
    }

    /**
     * Get drawable resource ID for a specific theme index
     * @param themeIndex Index of the theme (0-29)
     * @return Resource ID of the theme drawable
     */
    public int getThemeDrawableId(int themeIndex) {
        if (themeIndex >= 0 && themeIndex < THEME_DRAWABLES.length) {
            String drawableName = THEME_DRAWABLES[themeIndex];
            return context.getResources().getIdentifier(
                drawableName,
                "drawable",
                context.getPackageName()
            );
        }
        return getThemeDrawableId(DEFAULT_THEME);
    }

    /**
     * Get drawable resource name for the selected theme
     * @return Resource name of the selected theme drawable
     */
    public String getSelectedThemeDrawableName() {
        int index = getSelectedThemeIndex();
        if (index >= 0 && index < THEME_DRAWABLES.length) {
            return THEME_DRAWABLES[index];
        }
        return THEME_DRAWABLES[DEFAULT_THEME];
    }
}

