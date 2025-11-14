# Theme Background System

## Tổng quan
Hệ thống cho phép người dùng chọn theme background cho launcher từ 30 theme có sẵn trong drawable.

## Các file đã tạo/chỉnh sửa:

### 1. **ThemeBackgroundManager.java**
- Quản lý việc lưu/load theme đã chọn vào SharedPreferences
- Cung cấp phương thức để lấy drawable resource ID của theme đã chọn

**Vị trí:** `app/src/main/java/org/levimc/launcher/util/ThemeBackgroundManager.java`

### 2. **general_settings.java** 
- Fragment cài đặt với Spinner để chọn theme
- Tự động lưu theme khi người dùng chọn
- Load theme đã lưu khi mở fragment

**Vị trí:** `app/src/main/java/org/levimc/launcher/ui/fragment/general_settings.java`

### 3. **Play.java**
- Fragment play hiển thị theme đã chọn ở ImageView `launcherlayoutImageView1`
- Tự động load theme mỗi khi fragment được tạo

**Vị trí:** `app/src/main/java/org/levimc/launcher/ui/fragment/Play.java`

### 4. **strings.xml**
- Thêm mảng `theme_spinner_array` với 30 tên theme:
  - Theme 1: Copper Update
  - Theme 2: Wild Update
  - Theme 3: Caves & Cliffs
  - ... (30 themes total)

**Vị trí:** `app/src/main/res/values/strings.xml`

## Cách hoạt động:

1. **Chọn theme:**
   - Người dùng vào General Settings
   - Chọn theme từ spinner "LAUNCHER THEME"
   - Theme được lưu tự động vào SharedPreferences

2. **Hiển thị theme:**
   - Khi vào màn hình Play
   - ThemeBackgroundManager load theme đã lưu
   - Hiển thị theme tương ứng ở `launcherlayoutImageView1`

3. **Theme drawable mapping:**
   - Theme 1 (Copper Update) → `theme1.jpg`
   - Theme 2 (Wild Update) → `theme2.jpg`
   - ...
   - Theme 30 (Deep Dark) → `theme30.png`

## Drawables yêu cầu:
Cần có 30 file drawable trong `res/drawable/`:
- theme1.jpg
- theme2.jpg
- theme3.jpg
- ...
- theme28.png
- theme29.png
- theme30.png

✅ Đã kiểm tra: Tất cả 30 theme đã có trong drawable!

## Sử dụng ThemeBackgroundManager:

```java
// Khởi tạo
ThemeBackgroundManager manager = new ThemeBackgroundManager(context);

// Lưu theme (0-29)
manager.saveSelectedTheme(5);

// Lấy theme index đã lưu
int themeIndex = manager.getSelectedThemeIndex();

// Lấy drawable resource ID của theme đã chọn
int drawableId = manager.getSelectedThemeDrawableId();

// Hiển thị theme
imageView.setImageResource(drawableId);
```

## Lưu ý:
- Theme index chạy từ 0-29 (tương ứng với 30 themes)
- Theme mặc định là theme1 (index 0 - Copper Update)
- Dữ liệu được lưu trong SharedPreferences với tên "ThemePreferences"

