package org.levimc.launcher;

import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Activity để test crash handler
 * Xóa file này sau khi test xong
 */
public class TestCrashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);

        Button crashButton = new Button(this);
        crashButton.setText("Test Crash - NullPointerException");
        crashButton.setOnClickListener(v -> {
            String test = null;
            test.length(); // Gây ra NullPointerException
        });

        Button crashButton2 = new Button(this);
        crashButton2.setText("Test Crash - ArrayIndexOutOfBounds");
        crashButton2.setOnClickListener(v -> {
            int[] arr = new int[1];
            int x = arr[10]; // Gây ra ArrayIndexOutOfBoundsException
        });

        Button crashButton3 = new Button(this);
        crashButton3.setText("Test Crash - RuntimeException");
        crashButton3.setOnClickListener(v -> {
            throw new RuntimeException("Test crash message");
        });

        layout.addView(crashButton);
        layout.addView(crashButton2);
        layout.addView(crashButton3);

        setContentView(layout);
    }
}

