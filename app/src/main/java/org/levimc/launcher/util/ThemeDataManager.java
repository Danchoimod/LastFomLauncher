package org.levimc.launcher.util;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ThemeDataManager {
    private static final String PREFS_NAME = "theme_prefs";
    private static final String SELECTED_THEME_ID = "selected_theme_id";
    private static final String THEME_FILE = "theme.json";

    public static class Theme {
        public String id;
        public String name;
        public String imgUrl;

        public Theme(String id, String name, String imgUrl) {
            this.id = id;
            this.name = name;
            this.imgUrl = imgUrl;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * Load all themes from theme.json
     */
    public static List<Theme> loadThemes(Context context) {
        List<Theme> themes = new ArrayList<>();
        try {
            File appDir = context.getExternalFilesDir(null);
            File themeFile = new File(appDir, THEME_FILE);

            if (!themeFile.exists()) {
                // Return default theme if file doesn't exist
                themes.add(new Theme("default", "Original", ""));
                return themes;
            }

            // Read file content
            FileInputStream fis = new FileInputStream(themeFile);
            byte[] buffer = new byte[(int) themeFile.length()];
            fis.read(buffer);
            fis.close();
            String jsonContent = new String(buffer, StandardCharsets.UTF_8);

            // Parse JSON
            JSONObject root = new JSONObject(jsonContent);
            JSONArray themeArray = root.getJSONArray("theme");

            for (int i = 0; i < themeArray.length(); i++) {
                JSONObject item = themeArray.getJSONObject(i);
                String id = item.getString("id");
                String name = item.getString("name");
                String img = item.getString("img");
                themes.add(new Theme(id, name, img));
            }

        } catch (Exception e) {
            e.printStackTrace();
            // Add default theme on error
            if (themes.isEmpty()) {
                themes.add(new Theme("default", "Original", ""));
            }
        }
        return themes;
    }

    /**
     * Get selected theme ID from SharedPreferences
     */
    public static String getSelectedThemeId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(SELECTED_THEME_ID, "default");
    }

    /**
     * Save selected theme ID to SharedPreferences
     */
    public static void saveSelectedThemeId(Context context, String themeId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(SELECTED_THEME_ID, themeId).apply();
    }

    /**
     * Get the selected theme object
     */
    public static Theme getSelectedTheme(Context context) {
        List<Theme> themes = loadThemes(context);
        String selectedId = getSelectedThemeId(context);

        for (Theme theme : themes) {
            if (theme.id.equals(selectedId)) {
                return theme;
            }
        }

        // Return first theme if selected not found
        return themes.isEmpty() ? new Theme("default", "Original", "") : themes.get(0);
    }

    /**
     * Get local image file path for theme
     */
    public static File getThemeImageFile(Context context, String themeId) {
        File appDir = context.getExternalFilesDir(null);
        File themeDir = new File(appDir, "images/theme");

        if (!themeDir.exists()) {
            return null;
        }

        // Check for different extensions
        String[] extensions = {".jpg", ".png", ".webp"};
        for (String ext : extensions) {
            File imageFile = new File(themeDir, themeId + ext);
            if (imageFile.exists()) {
                return imageFile;
            }
        }

        return null;
    }
}

