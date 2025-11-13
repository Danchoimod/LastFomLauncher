package org.levimc.launcher.util;

import android.app.Activity;
import android.view.View;

public class FullscreenUtil {

    /**
     * Kích hoạt chế độ toàn màn hình (ẩn thanh điều hướng & status bar)
     */
    public static void enableImmersiveMode(Activity activity) {
        View decorView = activity.getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
    }

    /**
     * Thoát khỏi chế độ toàn màn hình (hiện lại thanh điều hướng & status bar)
     */
    public static void disableImmersiveMode(Activity activity) {
        View decorView = activity.getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }
}
