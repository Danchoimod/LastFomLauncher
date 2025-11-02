package org.levimc.launcher;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.levimc.launcher.ui.activities.BaseActivity;

/**
 * Activity hiển thị thông tin lỗi khi ứng dụng bị crash
 */
public class CrashActivity extends BaseActivity {

    public static final String EXTRA_ERROR_MESSAGE = "error_message";
    public static final String EXTRA_STACK_TRACE = "stack_trace";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_crash);
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setOnSystemUiVisibilityChangeListener(visibility -> {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    decorView.setSystemUiVisibility(uiOptions);
                }
            });
            // Lấy thông tin lỗi từ Intent
            String errorMessage = getIntent().getStringExtra(EXTRA_ERROR_MESSAGE);
            String stackTrace = getIntent().getStringExtra(EXTRA_STACK_TRACE);

            // Tìm các view
            TextView errorMessageText = findViewById(R.id.error_message_text);
            TextView stackTraceText = findViewById(R.id.stack_trace_text);
            Button copyErrorButton = findViewById(R.id.copy_error_button);
            Button restartButton = findViewById(R.id.restart_button);
            Button closeButton = findViewById(R.id.close_button);

            // Hiển thị thông tin lỗi
            if (errorMessage != null && errorMessageText != null) {
                errorMessageText.setText(errorMessage);
            }

            if (stackTrace != null && stackTraceText != null) {
                stackTraceText.setText(stackTrace);
            }

            // Nút sao chép lỗi
            if (copyErrorButton != null) {
                copyErrorButton.setOnClickListener(v -> copyErrorToClipboard(errorMessage, stackTrace));
            }

            // Nút khởi động lại ứng dụng
            if (restartButton != null) {
                restartButton.setOnClickListener(v -> restartApp());
            }

            // Nút đóng ứng dụng
            if (closeButton != null) {
                closeButton.setOnClickListener(v -> {
                    finish();
                    System.exit(0);
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Nếu có lỗi khi hiển thị crash screen, đơn giản là thoát
            finish();
            System.exit(0);
        }
    }

    /**
     * Sao chép thông tin lỗi vào clipboard
     */
    private void copyErrorToClipboard(String errorMessage, String stackTrace) {
        try {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            String errorText = "Error: " + errorMessage + "\n\nStack Trace:\n" + stackTrace;
            ClipData clip = ClipData.newPlainText("Error Log", errorText);
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "Đã sao chép thông tin lỗi", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Khởi động lại ứng dụng
     */
    private void restartApp() {
        try {
            Intent intent = getBaseContext().getPackageManager()
                    .getLaunchIntentForPackage(getBaseContext().getPackageName());
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
            finish();
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            finish();
            System.exit(0);
        }
    }

    @Override
    public void onBackPressed() {
        // Không cho phép quay lại khi ứng dụng bị crash
        super.onBackPressed();
        finish();
        System.exit(0);
    }
}

