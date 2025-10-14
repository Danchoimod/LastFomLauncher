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
    private int pendingDownloads = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_data);

        progressBar = findViewById(R.id.progress_bar);
        db = FirebaseFirestore.getInstance();

        checkAndDownloadData();
    }

    private void checkAndDownloadData() {
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);

        // Kiểm tra cả version và patchnotes
        db.collection("update").document("version")
                .get()
                .addOnSuccessListener(versionDoc -> {
                    db.collection("update").document("patchnotes")
                            .get()
                            .addOnSuccessListener(patchnotesDoc -> {
                                boolean needDownloadVersion = false;
                                boolean needDownloadPatchnotes = false;

                                // Kiểm tra version
                                if (versionDoc.exists()) {
                                    Long remoteVersionIndex = versionDoc.getLong("index");
                                    String versionUrl = versionDoc.getString("serverUrl");
                                    long localVersionIndex = prefs.getLong("versionIndex", 0);

                                    if (remoteVersionIndex != null && remoteVersionIndex != localVersionIndex) {
                                        needDownloadVersion = true;
                                    }
                                }

                                // Kiểm tra patchnotes
                                if (patchnotesDoc.exists()) {
                                    Long remotePatchnotesIndex = patchnotesDoc.getLong("index");
                                    String patchnotesUrl = patchnotesDoc.getString("serverUrl");
                                    long localPatchnotesIndex = prefs.getLong("patchnotesIndex", 0);

                                    if (remotePatchnotesIndex != null && remotePatchnotesIndex != localPatchnotesIndex) {
                                        needDownloadPatchnotes = true;
                                    }
                                }

                                // Tải xuống nếu cần
                                if (needDownloadVersion && needDownloadPatchnotes) {
                                    pendingDownloads = 2;
                                    downloadFile(versionDoc.getString("serverUrl"),
                                            versionDoc.getLong("index"),
                                            "versions.json",
                                            "versionIndex",
                                            prefs);
                                    downloadFile(patchnotesDoc.getString("serverUrl"),
                                            patchnotesDoc.getLong("index"),
                                            "patchnotes.json",
                                            "patchnotesIndex",
                                            prefs);
                                } else if (needDownloadVersion) {
                                    pendingDownloads = 1;
                                    downloadFile(versionDoc.getString("serverUrl"),
                                            versionDoc.getLong("index"),
                                            "versions.json",
                                            "versionIndex",
                                            prefs);
                                } else if (needDownloadPatchnotes) {
                                    pendingDownloads = 1;
                                    downloadFile(patchnotesDoc.getString("serverUrl"),
                                            patchnotesDoc.getLong("index"),
                                            "patchnotes.json",
                                            "patchnotesIndex",
                                            prefs);
                                } else {
                                    goToNextScreen();
                                }
                            })
                            .addOnFailureListener(e -> {
                                // Nếu patchnotes fail, vẫn kiểm tra version
                                checkVersionOnly(versionDoc, prefs);
                            });
                })
                .addOnFailureListener(e -> goToNextScreen());
    }

    private void checkVersionOnly(com.google.firebase.firestore.DocumentSnapshot versionDoc, SharedPreferences prefs) {
        if (!versionDoc.exists()) {
            goToNextScreen();
            return;
        }

        Long remoteVersion = versionDoc.getLong("index");
        String serverUrl = versionDoc.getString("serverUrl");
        long localVersion = prefs.getLong("versionIndex", 0);

        if (remoteVersion != null && remoteVersion != localVersion) {
            pendingDownloads = 1;
            downloadFile(serverUrl, remoteVersion, "versions.json", "versionIndex", prefs);
        } else {
            goToNextScreen();
        }
    }

    private void downloadFile(String serverUrl, long remoteVersion, String fileName, String indexKey, SharedPreferences prefs) {
        new Thread(() -> {
            boolean success = false;
            try {
                runOnUiThread(() ->
                        Toast.makeText(this, "Downloading " + fileName + "...", Toast.LENGTH_SHORT).show()
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

                File targetFile = new File(appDir, fileName);

                try (OutputStream output = new FileOutputStream(targetFile)) {
                    byte[] buffer = new byte[4096];
                    int total = 0;
                    int count;
                    while ((count = input.read(buffer)) != -1) {
                        total += count;
                        output.write(buffer, 0, count);

                        if (fileLength > 0) {
                            int progress = (int) (total * 100L / fileLength);
                            runOnUiThread(() -> progressBar.setProgress(progress));

                            if (progress == 25 || progress == 50 || progress == 75) {
                                int finalProgress = progress;
                                runOnUiThread(() ->
                                        Toast.makeText(this, "Downloading " + fileName + "... " + finalProgress + "%", Toast.LENGTH_SHORT).show()
                                );
                            }
                        }
                    }
                    output.flush();
                }

                input.close();
                success = true;

                prefs.edit().putLong(indexKey, remoteVersion).apply();

            } catch (Exception e) {
                e.printStackTrace();
            }

            boolean finalSuccess = success;
            runOnUiThread(() -> {
                if (finalSuccess) {
                    progressBar.setProgress(100);
                    Toast.makeText(this, fileName + " downloaded successfully!", Toast.LENGTH_SHORT).show();

                    // Giảm số lượng download còn lại
                    pendingDownloads--;
                    if (pendingDownloads <= 0) {
                        goToNextScreen();
                    }
                } else {
                    Toast.makeText(this, "Failed to download " + fileName + ". Check network connection.", Toast.LENGTH_LONG).show();
                    progressBar.setProgress(0);
                    // Vẫn tiếp tục nếu có lỗi
                    pendingDownloads--;
                    if (pendingDownloads <= 0) {
                        goToNextScreen();
                    }
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