package org.levimc.launcher.ui.fragment;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.os.Environment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;
import org.levimc.launcher.R;
import org.levimc.launcher.databinding.FragmentInstallVersionBinding;
import org.levimc.launcher.ui.entity.Version;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class installVersion extends Fragment {

    private static final String CHANNEL_ID = "levi_downloads";
    private static final int REQ_POST_NOTIFICATIONS = 1001;

    private FragmentInstallVersionBinding binding;
    private List<Version> versions = new ArrayList<>();
    // Toggle state: true => Main Version mode (no [LF]), false => Install mode (with [LF])
    private boolean isMainMode = true; // default true when fragment opens
    // Data shown in spinner after filtering
    private final List<Version> displayedVersions = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter;

    // Hold pending download request while asking permission
    private static class PendingDownload {
        final Version version; final String folderPath; final String fileName;
        PendingDownload(Version v, String f, String n) { this.version = v; this.folderPath = f; this.fileName = n; }
    }
    private PendingDownload pending;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentInstallVersionBinding.inflate(getLayoutInflater(), container, false);
        View root = binding.getRoot();
        if (binding.progressBarLayout != null) {
            binding.progressBarLayout.setVisibility(View.INVISIBLE);
        }

        createNotificationChannelIfNeeded();

        // Load full list once
        loadVersionsFromJson();
        // Prepare spinner adapter and listener
        setupSpinner();

        // Default mode = true (MainVersion). Apply UI and data.
        isMainMode = true;
        updateUiForMode();

        // Toggle mode when tapping icon
        binding.icon.setOnClickListener(v -> {
            isMainMode = !isMainMode;
            updateUiForMode();
        });

        // Local (Main Version) install
        binding.localInstall.setOnClickListener(v -> {
            if (!binding.localInstall.isEnabled()) return;
            int position = binding.versonSpinner.getSelectedItemPosition();
            if (position >= 0 && position < displayedVersions.size()) {
                Version selectedVersion = displayedVersions.get(position);
                startDownloadFlow(selectedVersion, "games/lflauncher/minecraft", "base.apk");
            } else {
                Toast.makeText(getContext(), "Vui lòng chọn phiên bản", Toast.LENGTH_SHORT).show();
            }
        });

        // Cloud (Install) action
        binding.installButton.setOnClickListener(v -> {
            if (!binding.installButton.isEnabled()) return;
            int position = binding.versonSpinner.getSelectedItemPosition();
            if (position >= 0 && position < displayedVersions.size()) {
                Version selectedVersion = displayedVersions.get(position);
                startDownloadFlow(selectedVersion, "games/org.levimc/minecraft", "base.apk.levi");
            } else {
                Toast.makeText(getContext(), "Vui lòng chọn phiên bản", Toast.LENGTH_SHORT).show();
            }
        });

        return root;
    }

    private void createNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Context ctx = requireContext();
            NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null && nm.getNotificationChannel(CHANNEL_ID) == null) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        "Levi Downloads",
                        NotificationManager.IMPORTANCE_LOW
                );
                channel.setDescription("Tiến trình tải phiên bản");
                nm.createNotificationChannel(channel);
            }
        }
    }

    private boolean ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT < 33) return true;
        Context ctx = requireContext();
        int granted = ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS);
        if (granted == PackageManager.PERMISSION_GRANTED) return true;
        requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_POST_NOTIFICATIONS);
        return false;
    }

    private void startDownloadFlow(Version version, String folderPath, String fileName) {
        // Ask notification permission first (Android 13+)
        if (!ensureNotificationPermission()) {
            // Store pending and wait for callback
            pending = new PendingDownload(version, folderPath, fileName);
            // Show light UI indicator if available
            if (binding != null && binding.progressBarLayout != null) {
                binding.progressBarLayout.setVisibility(View.VISIBLE);
            }
            return;
        }
        // Permission already ok -> start download now
        downloadSelectedVersion(version, folderPath, fileName);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_POST_NOTIFICATIONS) {
            boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (granted && pending != null) {
                PendingDownload p = pending; pending = null;
                downloadSelectedVersion(p.version, p.folderPath, p.fileName);
            } else {
                if (binding != null && binding.progressBarLayout != null) {
                    binding.progressBarLayout.setVisibility(View.INVISIBLE);
                }
                Toast.makeText(requireContext(), "Không có quyền thông báo. Vẫn tiến hành tải trong ứng dụng.", Toast.LENGTH_SHORT).show();
                if (pending != null) {
                    PendingDownload p = pending; pending = null;
                    downloadSelectedVersion(p.version, p.folderPath, p.fileName);
                }
            }
        }
    }

    private void loadVersionsFromJson() {
        try {
            File appDir = requireContext().getExternalFilesDir(null);
            File versionsFile = new File(appDir, "versions.json");

            if (!versionsFile.exists()) {
                Toast.makeText(getContext(), "Không tìm thấy file versions.json", Toast.LENGTH_SHORT).show();
                return;
            }

            try (FileInputStream fis = new FileInputStream(versionsFile)) {
                byte[] data = new byte[(int) versionsFile.length()];
                int read = fis.read(data);
                if (read <= 0) {
                    Toast.makeText(getContext(), "versions.json rỗng", Toast.LENGTH_SHORT).show();
                    return;
                }
                String jsonString = new String(data, StandardCharsets.UTF_8);
                JSONObject jsonObject = new JSONObject(jsonString);
                JSONArray versionsArray = jsonObject.getJSONArray("versions");

                versions.clear();
                for (int i = 0; i < versionsArray.length(); i++) {
                    JSONObject versionObj = versionsArray.getJSONObject(i);
                    String id = versionObj.getString("id");
                    String name = versionObj.getString("name");
                    String url = versionObj.getString("url");

                    versions.add(new Version(id, name, url));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Lỗi đọc file JSON: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupSpinner() {
        // Create empty adapter once
        spinnerAdapter = new ArrayAdapter<>(
                requireContext(),
                org.levimc.launcher.R.layout.spinner_item,
                new ArrayList<>()
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.versonSpinner.setAdapter(spinnerAdapter);

        binding.versonSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                // No-op
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void refreshSpinnerData() {
        displayedVersions.clear();
        List<String> names = new ArrayList<>();
        for (Version v : versions) {
            String name = v.getName();
            boolean hasLf = name != null && name.contains("[LF]");
            if (isMainMode) {
                if (!hasLf) { displayedVersions.add(v); names.add(name); }
            } else {
                if (hasLf) { displayedVersions.add(v); names.add(name); }
            }
        }
        spinnerAdapter.clear();
        spinnerAdapter.addAll(names);
        spinnerAdapter.notifyDataSetChanged();
        if (!displayedVersions.isEmpty()) {
            binding.versonSpinner.setSelection(0);
        }
    }

    private void updateUiForMode() {
        try {
            int resId = getResources().getIdentifier(isMainMode ? "grassblock" : "java", "drawable", requireContext().getPackageName());
            if (resId != 0) {
                binding.icon.setImageResource(resId);
            }
        } catch (Exception ignored) {}

        // Update buttons enablement
        applyEnabled(binding.localInstall, isMainMode); // MainVersion button
        applyEnabled(binding.installButton, !isMainMode); // Install button

        // Update description text in English based on mode
        if (binding != null && binding.description != null) {
            String desc = isMainMode
                    ? "This download installs the APK version."
                    : "This feature downloads the loader version.";
            binding.description.setText(desc);
        }

        // Refresh spinner to match current mode
        refreshSpinnerData();
    }

    private void applyEnabled(View v, boolean enabled) {
        v.setEnabled(enabled);
        v.setClickable(enabled);
        v.setAlpha(enabled ? 1f : 0.5f);
    }

    private void downloadSelectedVersion(Version version, String folderPath, String fileName) {
        new Thread(() -> {
            int notificationId = (int) (System.currentTimeMillis() & 0xFFFFFF);
            NotificationManagerCompat nmc = NotificationManagerCompat.from(requireContext());
            NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.stat_sys_download)
                    .setContentTitle("Downloading: " + version.getName())
                    .setContentText("Đang chuẩn bị...")
                    .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                    .setOngoing(true)
                    .setOnlyAlertOnce(true)
                    .setPriority(NotificationCompat.PRIORITY_LOW);

            try { nmc.notify(notificationId, builder.build()); } catch (SecurityException ignore) {}

            HttpURLConnection connection = null;
            try {
                File minecraftDir = new File(Environment.getExternalStorageDirectory(), folderPath);
                File versionDir = new File(minecraftDir, "minecraft_" + version.getName());
                if (!versionDir.exists()) { //noinspection ResultOfMethodCallIgnored
                    versionDir.mkdirs();
                }
                File destinationFile = new File(versionDir, fileName);

                runOnUiThreadSafe(() -> {
                    if (binding != null && binding.progressBarLayout != null) {
                        binding.progressBarLayout.setVisibility(View.VISIBLE);
                        binding.progressBar.setIndeterminate(false);
                        binding.progressBar.setProgress(0);
                        binding.tvDownloading.setText("Downloading... " + version.getName() + "...");
                    }
                });

                URL url = new URL(version.getUrl());
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                int fileLength = connection.getContentLength();
                final boolean unknownLength = fileLength <= 0;

                if (unknownLength) {
                    builder.setProgress(0, 0, true).setContentText("Đang tải...");
                    try { nmc.notify(notificationId, builder.build()); } catch (SecurityException ignore) {}
                    runOnUiThreadSafe(() -> { if (binding != null) binding.progressBar.setIndeterminate(true); });
                }

                // Throttling variables - chỉ cập nhật mỗi 5% hoặc mỗi 500ms
                int lastProgress = -1;
                long lastUpdateTime = 0;
                final long UPDATE_INTERVAL_MS = 500; // Cập nhật tối đa mỗi 500ms
                final int PROGRESS_STEP = 5; // Cập nhật mỗi 5%

                try (InputStream input = connection.getInputStream(); FileOutputStream output = new FileOutputStream(destinationFile)) {
                    byte[] buffer = new byte[16384]; // Tăng buffer size để giảm số lần đọc
                    long total = 0;
                    int count;
                    while ((count = input.read(buffer)) != -1) {
                        total += count;
                        output.write(buffer, 0, count);

                        if (!unknownLength) {
                            final int progress = (int) (total * 100 / fileLength);
                            long currentTime = System.currentTimeMillis();

                            // Chỉ cập nhật nếu:
                            // 1. Progress thay đổi ít nhất 5% HOẶC
                            // 2. Đã qua ít nhất 500ms kể từ lần cập nhật cuối
                            boolean shouldUpdate = (progress - lastProgress >= PROGRESS_STEP) ||
                                                 (currentTime - lastUpdateTime >= UPDATE_INTERVAL_MS);

                            if (shouldUpdate) {
                                lastProgress = progress;
                                lastUpdateTime = currentTime;

                                // Cập nhật notification (giảm từ mỗi lần đọc xuống còn mỗi 5% hoặc 500ms)
                                builder.setProgress(100, progress, false)
                                       .setContentText(progress + "%");
                                try {
                                    nmc.notify(notificationId, builder.build());
                                } catch (SecurityException ignore) {}

                                // Cập nhật UI
                                final int p = progress;
                                runOnUiThreadSafe(() -> {
                                    if (binding != null) {
                                        binding.progressBar.setIndeterminate(false);
                                        binding.progressBar.setProgress(p);
                                        binding.tvDownloading.setText("Downloading " + version.getName() + "... " + p + "%");
                                    }
                                });
                            }
                        }
                    }
                    output.flush();
                }

                // Update the same notification to success state (more reliable in background)
                builder.setSmallIcon(android.R.drawable.stat_sys_download_done)
                        .setCategory(NotificationCompat.CATEGORY_STATUS)
                        .setContentTitle("Download success")
                        .setContentText("Đã tải xong: " + version.getName())
                        .setOngoing(false)
                        .setOnlyAlertOnce(false)
                        .setAutoCancel(true)
                        .setProgress(0,0,false);
                try { nmc.notify(notificationId, builder.build()); } catch (SecurityException ignore) {}

                runOnUiThreadSafe(() -> {
                    if (binding != null && binding.progressBarLayout != null) {
                        binding.progressBarLayout.setVisibility(View.INVISIBLE);
                        Toast.makeText(getContext(), "Tải thành công: " + version.getName(), Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                // Update the same notification to error state
                builder.setSmallIcon(android.R.drawable.stat_notify_error)
                        .setCategory(NotificationCompat.CATEGORY_STATUS)
                        .setContentTitle("Download failed")
                        .setContentText(e.getMessage())
                        .setOngoing(false)
                        .setOnlyAlertOnce(false)
                        .setAutoCancel(true)
                        .setProgress(0,0,false);
                try { nmc.notify(notificationId, builder.build()); } catch (SecurityException ignore) {}
                runOnUiThreadSafe(() -> {
                    if (binding != null && binding.progressBarLayout != null) {
                        binding.progressBarLayout.setVisibility(View.INVISIBLE);
                        Toast.makeText(getContext(), "Lỗi tải file: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            } finally {
                if (connection != null) {
                    try { connection.disconnect(); } catch (Exception ignore) {}
                }
            }
        }).start();
    }

    private void runOnUiThreadSafe(Runnable r) {
        if (!isAdded()) return;
        try {
            requireActivity().runOnUiThread(r);
        } catch (IllegalStateException ignored) { }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Class để lưu thông tin phiên bản
//    private static class Version {
//        private String id;
//        private String name;
//        private String url;
//
//        public Version(String id, String name, String url) {
//            this.id = id;
//            this.name = name;
//            this.url = url;
//        }
//
//        public String getId() { return id; }
//        public String getName() { return name; }
//        public String getUrl() { return url; }
//    }
}
