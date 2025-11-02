package org.levimc.launcher;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.WeakReference;

/**
 * Handler xử lý các exception không được bắt trong ứng dụng
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private static final String TAG = "CrashHandler";
    private static CrashHandler instance;
    private final Context context;
    private final Thread.UncaughtExceptionHandler defaultHandler;
    private WeakReference<Activity> currentActivityRef;

    private CrashHandler(Context context) {
        this.context = context.getApplicationContext();
        this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    /**
     * Khởi tạo CrashHandler
     */
    public static void init(Context context) {
        if (instance == null) {
            instance = new CrashHandler(context);
            Thread.setDefaultUncaughtExceptionHandler(instance);

            // Theo dõi Activity hiện tại
            if (context instanceof Application) {
                try {
                    ((Application) context).registerActivityLifecycleCallbacks(
                        new Application.ActivityLifecycleCallbacks() {
                            @Override
                            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {}

                            @Override
                            public void onActivityStarted(@NonNull Activity activity) {}

                            @Override
                            public void onActivityResumed(@NonNull Activity activity) {
                                // Không lưu CrashActivity để tránh vòng lặp
                                if (!(activity instanceof CrashActivity)) {
                                    instance.currentActivityRef = new WeakReference<>(activity);
                                }
                            }

                            @Override
                            public void onActivityPaused(@NonNull Activity activity) {
                                if (instance.currentActivityRef != null &&
                                    instance.currentActivityRef.get() == activity) {
                                    instance.currentActivityRef.clear();
                                }
                            }

                            @Override
                            public void onActivityStopped(@NonNull Activity activity) {}

                            @Override
                            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {}

                            @Override
                            public void onActivityDestroyed(@NonNull Activity activity) {}
                        }
                    );
                } catch (Exception e) {
                    Log.e(TAG, "Error registering activity lifecycle callbacks", e);
                }
            }
        }
    }

    @Override
    public void uncaughtException(@NonNull Thread thread, @NonNull Throwable throwable) {
        try {
            Log.e(TAG, "Uncaught exception detected", throwable);

            // Kiểm tra nếu là lỗi native library libc++_shared.so
            if (throwable instanceof UnsatisfiedLinkError &&
                throwable.getMessage() != null &&
                throwable.getMessage().contains("libc++_shared.so")) {

                Log.d(TAG, "Detected libc++_shared.so error, opening Minecraft PE");

                // Mở Minecraft PE
                try {
                    Intent minecraftIntent = context.getPackageManager()
                        .getLaunchIntentForPackage("com.mojang.minecraftpe");
                    if (minecraftIntent != null) {
                        minecraftIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(minecraftIntent);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error opening Minecraft PE", e);
                }

                // Kết thúc activity hiện tại
                if (currentActivityRef != null) {
                    Activity activity = currentActivityRef.get();
                    if (activity != null && !activity.isFinishing()) {
                        try {
                            activity.runOnUiThread(() -> {
                                android.widget.Toast.makeText(activity,
                                    "Ứng dụng không được hỗ trợ",
                                    android.widget.Toast.LENGTH_LONG).show();
                            });
                        } catch (Exception e) {
                            Log.e(TAG, "Error showing toast", e);
                        }
                    }
                }

                // Đợi một chút để Toast hiển thị
                Thread.sleep(2000);

                // Kết thúc process
                Process.killProcess(Process.myPid());
                System.exit(10);
                return;
            }

            // Lấy thông tin lỗi
            String errorMessage = throwable.getClass().getSimpleName() + ": " +
                                 (throwable.getMessage() != null ? throwable.getMessage() : "No message");
            String stackTrace = getStackTraceString(throwable);

            Log.d(TAG, "Error message: " + errorMessage);

            // Tạo Intent để mở CrashActivity
            Intent intent = new Intent(context, CrashActivity.class);
            intent.putExtra(CrashActivity.EXTRA_ERROR_MESSAGE, errorMessage);
            intent.putExtra(CrashActivity.EXTRA_STACK_TRACE, stackTrace);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            // Kết thúc Activity hiện tại nếu có
            if (currentActivityRef != null) {
                Activity activity = currentActivityRef.get();
                if (activity != null && !activity.isFinishing()) {
                    try {
                        activity.finish();
                    } catch (Exception e) {
                        Log.e(TAG, "Error finishing activity", e);
                    }
                }
            }

            // Mở CrashActivity
            context.startActivity(intent);

            // Đợi một chút để Activity khởi động
            Thread.sleep(100);

            // Kết thúc process
            Process.killProcess(Process.myPid());
            System.exit(10);
        } catch (Exception e) {
            Log.e(TAG, "Error in crash handler", e);
            // Nếu có lỗi khi xử lý crash, gọi handler mặc định
            if (defaultHandler != null) {
                defaultHandler.uncaughtException(thread, throwable);
            } else {
                // Nếu không có handler mặc định, in lỗi và thoát
                throwable.printStackTrace();
                Process.killProcess(Process.myPid());
                System.exit(10);
            }
        }
    }

    /**
     * Chuyển stack trace thành chuỗi
     */
    private String getStackTraceString(Throwable throwable) {
        try {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            throwable.printStackTrace(printWriter);
            printWriter.close();
            return stringWriter.toString();
        } catch (Exception e) {
            return "Error getting stack trace: " + e.getMessage();
        }
    }
}

