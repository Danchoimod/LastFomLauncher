package org.levimc.launcher.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;

import org.levimc.launcher.R;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DownloadData extends BaseActivity {
    private ProgressBar progressBar;
    private TextView tvDownloading;
    private FirebaseFirestore db;
    private int pendingDownloads = 0;
    private boolean isPatchnotesDownloaded = false;

    private boolean isThemeDownloaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_data);

        progressBar = findViewById(R.id.progress_bar);
        tvDownloading = findViewById(R.id.tvDownloading);
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
                        tvDownloading.setText("Downloading " + fileName + "...")
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
                            int finalProgress = progress;
                            runOnUiThread(() -> {
                                progressBar.setProgress(finalProgress);
                                tvDownloading.setText("Downloading " + fileName + "... " + finalProgress + "%");
                            });
                        }
                    }
                    output.flush();
                }

                input.close();
                success = true;

                prefs.edit().putLong(indexKey, remoteVersion).apply();

                // Nếu là patchnotes.json thì đánh dấu để tải ảnh
                if (fileName.equals("patchnotes.json")) {
                    isPatchnotesDownloaded = true;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            boolean finalSuccess = success;
            runOnUiThread(() -> {
                if (finalSuccess) {
                    progressBar.setProgress(100);
                    tvDownloading.setText(fileName + " downloaded successfully!");

                    // Giảm số lượng download còn lại
                    pendingDownloads--;
                    if (pendingDownloads <= 0) {
                        // Nếu patchnotes vừa tải xong, tải ảnh trước khi chuyển màn hình
                        if (isPatchnotesDownloaded) {
                            downloadImagesFromPatchnotes();
                        } else {
                            goToNextScreen();
                        }
                    }
                } else {
                    tvDownloading.setText("Failed to download " + fileName + ". Check network connection.");
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

    private void downloadImagesFromPatchnotes() {
        new Thread(() -> {
            try {
                // Đọc file patchnotes.json
                File appDir = getExternalFilesDir(null);
                File patchnotesFile = new File(appDir, "patchnotes.json");

                if (!patchnotesFile.exists()) {
                    runOnUiThread(this::goToNextScreen);
                    return;
                }

                // Đọc nội dung file
                FileInputStream fis = new FileInputStream(patchnotesFile);
                byte[] buffer = new byte[(int) patchnotesFile.length()];
                fis.read(buffer);
                fis.close();
                String jsonContent = new String(buffer, StandardCharsets.UTF_8);

                // Parse JSON
                JSONObject root = new JSONObject(jsonContent);
                JSONArray patchnotes = root.getJSONArray("patchnotes");

                // Tạo thư mục images
                File imagesDir = new File(appDir, "images");
                if (!imagesDir.exists()) {
                    imagesDir.mkdirs();
                }

                // Đếm số ảnh cần tải
                int imageCount = patchnotes.length();
                if (imageCount == 0) {
                    runOnUiThread(this::goToNextScreen);
                    return;
                }

                int downloadedCount = 0;
                for (int i = 0; i < imageCount; i++) {
                    JSONObject item = patchnotes.getJSONObject(i);
                    String imageUrl = item.getString("img");
                    String id = item.getString("id");

                    // Lấy extension từ URL
                    String extension = ".jpg";
                    if (imageUrl.contains(".png")) extension = ".png";
                    else if (imageUrl.contains(".webp")) extension = ".webp";

                    String fileName = id + extension;
                    File imageFile = new File(imagesDir, fileName);

                    // Nếu file đã tồn tại thì bỏ qua download
                    if (imageFile.exists()) {
                        downloadedCount++;
                        int finalDownloaded = downloadedCount;
                        runOnUiThread(() ->
                                tvDownloading.setText("Downloading images... (" + finalDownloaded + "/" + imageCount + ")")
                        );
                        continue;
                    }

                    // Tải ảnh
                    downloadImage(imageUrl, imageFile);
                    downloadedCount++;
                    int finalDownloaded = downloadedCount;
                    runOnUiThread(() ->
                            tvDownloading.setText("Downloading images... (" + finalDownloaded + "/" + imageCount + ")")
                    );
                }

                runOnUiThread(() -> {
                    tvDownloading.setText("All images downloaded!");
                    progressBar.setProgress(100);
                    goToNextScreen();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(this::goToNextScreen);
            }
        }).start();
    }

    private void downloadImage(String imageUrl, File targetFile) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.connect();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream input = connection.getInputStream();
                FileOutputStream output = new FileOutputStream(targetFile);

                byte[] buffer = new byte[4096];
                int count;
                while ((count = input.read(buffer)) != -1) {
                    output.write(buffer, 0, count);
                }

                output.flush();
                output.close();
                input.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void goToNextScreen() {
        progressBar.postDelayed(() -> {
            startActivity(new Intent(DownloadData.this, MainLauncher.class));
            finish();
        }, 1000);
    }
}