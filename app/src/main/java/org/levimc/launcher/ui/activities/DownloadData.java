package org.levimc.launcher.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import org.levimc.launcher.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadData extends BaseActivity {
    private ProgressBar progressBar;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_data);

        progressBar = findViewById(R.id.progress_bar);
        db = FirebaseFirestore.getInstance();

        checkAndDownloadData();
    }

    private void checkAndDownloadData() {
        db.collection("update").document("version")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        goToNextScreen();
                        return;
                    }

                    long remoteVersion = documentSnapshot.getLong("index");
                    String serverUrl = documentSnapshot.getString("serverUrl");

                    SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
                    long localVersion = prefs.getLong("index", 0);

                    if (remoteVersion != localVersion) {
                        downloadFile(serverUrl, remoteVersion, prefs);
                    } else {
                        goToNextScreen();
                    }
                })
                .addOnFailureListener(e -> goToNextScreen());
    }

    private void downloadFile(String serverUrl, long remoteVersion, SharedPreferences prefs) {
        new Thread(() -> {
            boolean success = false;
            try {
                runOnUiThread(() ->
                        Toast.makeText(this, "🔄 Bắt đầu tải dữ liệu...", Toast.LENGTH_SHORT).show()
                );

                URL url = new URL(serverUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(8000);
                connection.setReadTimeout(8000);
                connection.connect();

                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw new Exception("HTTP error: " + responseCode);
                }

                int fileLength = connection.getContentLength();
                InputStream input = connection.getInputStream();

                File appDir = getExternalFilesDir(null);
                if (appDir != null && !appDir.exists()) appDir.mkdirs();

                File versionsFile = new File(appDir, "versions.json");

                try (OutputStream output = new FileOutputStream(versionsFile)) {
                    byte[] buffer = new byte[4096];
                    int total = 0;
                    int count;
                    while ((count = input.read(buffer)) != -1) {
                        total += count;
                        output.write(buffer, 0, count);

                        if (fileLength > 0) {
                            int progress = (int) (total * 100L / fileLength);
                            runOnUiThread(() -> progressBar.setProgress(progress));

                            // Hiển thị toast theo mốc phần trăm (ít spam)
                            if (progress == 25 || progress == 50 || progress == 75) {
                                int finalProgress = progress;
                                runOnUiThread(() ->
                                        Toast.makeText(this, "📥 Đang tải... " + finalProgress + "%", Toast.LENGTH_SHORT).show()
                                );
                            }
                        }
                    }
                    output.flush();
                }

                input.close();
                success = true;

                prefs.edit().putLong("index", remoteVersion).apply();

            } catch (Exception e) {
                e.printStackTrace();
            }

            boolean finalSuccess = success;
            runOnUiThread(() -> {
                if (finalSuccess) {
                    progressBar.setProgress(100);
                    Toast.makeText(this, "✅ Tải dữ liệu thành công!", Toast.LENGTH_LONG).show();
                    goToNextScreen();
                } else {
                    Toast.makeText(this, "❌ Tải dữ liệu thất bại. Kiểm tra kết nối mạng.", Toast.LENGTH_LONG).show();
                    progressBar.setProgress(0);
                }
            });
        }).start();
    }



    private void goToNextScreen() {
        progressBar.postDelayed(() -> {
            startActivity(new Intent(DownloadData.this, MainLauncher.class));
            finish();
        }, 1000);
    }
}