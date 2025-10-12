package org.levimc.launcher.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import android.os.Environment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;
import org.levimc.launcher.databinding.FragmentInstallVersionBinding;
import org.levimc.launcher.ui.entity.Version;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class installVersion extends Fragment {

    private FragmentInstallVersionBinding binding;
    private List<Version> versions = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentInstallVersionBinding.inflate(getLayoutInflater(), container, false);
        View root = binding.getRoot();
        binding.progressBarLayout.setVisibility(View.INVISIBLE);
        loadVersionsFromJson();
        setupSpinner();

        binding.installButton.setOnClickListener(v -> {
            int position = binding.versonSpinner.getSelectedItemPosition();
            if (position >= 0 && position < versions.size()) {
                Version selectedVersion = versions.get(position);
                downloadSelectedVersion(selectedVersion);
            } else {
                Toast.makeText(getContext(), "Vui lòng chọn phiên bản", Toast.LENGTH_SHORT).show();
            }
        });

        return root;
    }

    private void loadVersionsFromJson() {
        try {
            File appDir = requireContext().getExternalFilesDir(null);
            File versionsFile = new File(appDir, "versions.json");

            if (!versionsFile.exists()) {
                Toast.makeText(getContext(), "Không tìm thấy file versions.json", Toast.LENGTH_SHORT).show();
                return;
            }

            FileInputStream fis = new FileInputStream(versionsFile);
            byte[] data = new byte[(int) versionsFile.length()];
            fis.read(data);
            fis.close();

            String jsonString = new String(data, "UTF-8");
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

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Lỗi đọc file JSON: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupSpinner() {
        List<String> versionNames = new ArrayList<>();
        for (Version v : versions) {
            versionNames.add(v.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                org.levimc.launcher.R.layout.spinner_item,
                versionNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.versonSpinner.setAdapter(adapter);

        binding.versonSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                Version selectedVersion = versions.get(position);
//                Toast.makeText(getContext(), selectedVersion.getName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
    }
    private void downloadSelectedVersion(Version version) {
        new Thread(() -> {
            try {
                // Tạo đường dẫn thư mục
                File minecraftDir = new File(Environment.getExternalStorageDirectory(),
                        "games/org.levimc/minecraft");
                File versionDir = new File(minecraftDir, "minecraft_" + version.getName());

                // Tạo thư mục nếu chưa tồn tại
                if (!versionDir.exists()) {
                    versionDir.mkdirs();
                }

                // File đích
                File destinationFile = new File(versionDir, "base.apk.levi");

                // Hiển thị progress bar trên UI thread
                requireActivity().runOnUiThread(() -> {
                    binding.progressBarLayout.setVisibility(View.VISIBLE);
                    binding.progressBar.setProgress(0);
                    binding.tvDownloading.setText("Downloading... " + version.getName() + "...");
                });

                // Tải file từ URL
                URL url = new URL(version.getUrl());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                int fileLength = connection.getContentLength();

                // Download file
                InputStream input = connection.getInputStream();
                FileOutputStream output = new FileOutputStream(destinationFile);

                byte[] buffer = new byte[4096];
                long total = 0;
                int count;

                while ((count = input.read(buffer)) != -1) {
                    total += count;

                    // Cập nhật progress
                    if (fileLength > 0) {
                        final int progress = (int) (total * 100 / fileLength);
                        requireActivity().runOnUiThread(() -> {
                            binding.progressBar.setProgress(progress);
                            binding.tvDownloading.setText("Đang tải " + version.getName() + "... " + progress + "%");
                        });
                    }

                    output.write(buffer, 0, count);
                }

                output.flush();
                output.close();
                input.close();

                // Hoàn thành
                requireActivity().runOnUiThread(() -> {
                    binding.progressBarLayout.setVisibility(View.INVISIBLE);
                    Toast.makeText(getContext(), "Tải thành công: " + version.getName(), Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> {
                    binding.progressBarLayout.setVisibility(View.INVISIBLE);
                    Toast.makeText(getContext(), "Lỗi tải file: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
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
