# Theme Selection Feature - Chọn theme từ theme.json

## Tổng quan
Tính năng cho phép người dùng chọn theme từ danh sách trong `theme.json`, hiển thị ảnh nền theme vào `launcherlayoutImageView1` trong Play fragment, và lưu lựa chọn vào SharedPreferences.

## Các file đã thêm/sửa

### 1. **ThemeManager.java** (NEW)
`c:\APp\LflauncherMain\LastFomLauncher\app\src\main\java\org\levimc\launcher\util\ThemeManager.java`

Helper class quản lý theme với các chức năng:
- `loadThemes(Context)` - Load danh sách theme từ `theme.json`
- `getSelectedThemeId(Context)` - Lấy theme ID đã chọn từ SharedPreferences
- `saveSelectedThemeId(Context, themeId)` - Lưu theme ID vào SharedPreferences
- `getSelectedTheme(Context)` - Lấy theme object đang được chọn
- `getThemeImageFile(Context, themeId)` - Lấy file ảnh của theme từ `images/theme/`

### 2. **general_settings.java** (UPDATED)
Cập nhật spinner để load theme từ JSON:
```java
private void setupThemeSpinner(Spinner themeSpinner) {
    // Load themes from theme.json
    List<ThemeManager.Theme> themes = ThemeManager.loadThemes(requireContext());
    
    // Create adapter with theme names
    ArrayAdapter<ThemeManager.Theme> themeAdapter = new ArrayAdapter<>(
            requireContext(),
            R.layout.spinner_item,
            themes
    );
    themeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    themeSpinner.setAdapter(themeAdapter);
    
    // Set selected theme from SharedPreferences
    String selectedThemeId = ThemeManager.getSelectedThemeId(requireContext());
    for (int i = 0; i < themes.size(); i++) {
        if (themes.get(i).id.equals(selectedThemeId)) {
            themeSpinner.setSelection(i);
            break;
        }
    }
    
    // Save selection when user changes theme
    themeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            ThemeManager.Theme selectedTheme = themes.get(position);
            ThemeManager.saveSelectedThemeId(requireContext(), selectedTheme.id);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing
        }
    });
}
```

### 3. **Play.java** (UPDATED)
Thêm chức năng load ảnh theme:
```java
private void loadThemeBackground() {
    if (binding == null) return;
    
    new Thread(() -> {
        try {
            // Get selected theme
            ThemeManager.Theme selectedTheme = ThemeManager.getSelectedTheme(requireContext());
            
            // Get image file from local storage
            File imageFile = ThemeManager.getThemeImageFile(requireContext(), selectedTheme.id);
            
            if (imageFile != null && imageFile.exists()) {
                // Load image from file
                Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                
                // Set image on UI thread
                requireActivity().runOnUiThread(() -> {
                    if (binding != null) {
                        binding.launcherlayoutImageView1.setImageBitmap(bitmap);
                    }
                });
            } else {
                // Use default drawable if image file not found
                requireActivity().runOnUiThread(() -> {
                    if (binding != null) {
                        binding.launcherlayoutImageView1.setImageResource(R.drawable.copperupdate);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Use default drawable on error
            requireActivity().runOnUiThread(() -> {
                if (binding != null) {
                    binding.launcherlayoutImageView1.setImageResource(R.drawable.copperupdate);
                }
            });
        }
    }).start();
}
```

## Cách hoạt động

### Flow khi app khởi động:
1. `DownloadData` kiểm tra và tải `theme.json` từ Firebase nếu có update
2. Download ảnh theme vào thư mục `images/theme/`
3. `Play` fragment load theme đã lưu từ SharedPreferences
4. Hiển thị ảnh theme vào `launcherlayoutImageView1`

### Flow khi chọn theme:
1. Người dùng mở Settings → General Settings
2. Chọn theme từ `theme_spinner` (hiển thị `name` từ theme.json)
3. Theme ID được lưu vào SharedPreferences
4. Khi quay lại Play fragment, `onResume()` được gọi
5. `loadThemeBackground()` load ảnh theme mới từ file

## Cấu trúc dữ liệu

### theme.json
```json
{
  "theme": [
    {
      "name": "Original",
      "img": "https://example.com/original.jpg",
      "id": "1763058599721"
    },
    {
      "id": "1763059034959",
      "name": "Minecraft Dungeons",
      "img": "https://example.com/dungeons.jpg"
    }
  ]
}
```

### SharedPreferences (theme_prefs)
- Key: `selected_theme_id`
- Value: ID của theme đã chọn (ví dụ: "1763058599721")

### Thư mục ảnh
- Path: `{externalFilesDir}/images/theme/`
- Tên file: `{theme_id}.jpg` (hoặc .png, .webp)
- Ví dụ: `1763058599721.jpg`, `1763059034959.jpg`

## Testing

1. **Upload theme.json lên Firebase**:
   - Tạo document `update/theme` với fields: `index` và `serverUrl`
   
2. **Chạy app**:
   - App sẽ tải `theme.json` và ảnh theme về

3. **Kiểm tra Settings**:
   - Vào Settings → General Settings
   - Mở theme_spinner
   - Danh sách theme sẽ hiển thị theo `name` trong JSON

4. **Chọn theme**:
   - Chọn theme từ spinner
   - Quay lại Play fragment
   - Ảnh nền sẽ thay đổi theo theme đã chọn

5. **Restart app**:
   - Đóng và mở lại app
   - Theme đã chọn vẫn được giữ nguyên

## Lưu ý
- Nếu `theme.json` không tồn tại, sẽ hiển thị theme mặc định "Original"
- Nếu ảnh theme không tìm thấy, sẽ dùng drawable mặc định `R.drawable.copperupdate`
- Ảnh được load trong background thread để tránh lag UI
- Theme được reload mỗi khi `onResume()` để cập nhật khi chuyển tab

