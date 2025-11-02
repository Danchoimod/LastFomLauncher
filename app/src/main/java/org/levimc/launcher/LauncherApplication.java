package org.levimc.launcher;

import android.app.Application;

/**
 * Application class cho LeviLauncher
 */
public class LauncherApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Khởi tạo CrashHandler để bắt các lỗi không được xử lý
        CrashHandler.init(this);
    }
}

