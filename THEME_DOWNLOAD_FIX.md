# Theme Download Fix - Sửa lỗi không tải ảnh theme

## Vấn đề trước đó
Ảnh của theme không được tải xuống mặc dù file `theme.json` đã được download thành công.

## Nguyên nhân
Logic cũ trong `downloadImagesFromJson()` sử dụng **if-else** thay vì **if độc lập**:
```java
if (isPatchnotesDownloaded) {
    // tải ảnh patchnotes
} else if (isThemeDownloaded) {  // ❌ Chỉ chạy nếu patchnotes KHÔNG được tải
    // tải ảnh theme
}
```

Khi cả patchnotes và theme đều được download, code chỉ tải ảnh patchnotes và BỎ QUA theme.

## Giải pháp
Refactor code để tách thành hàm riêng `downloadImagesFromFile()` và gọi **cả hai** nếu cần:

```java
private void downloadImagesFromJson() {
    new Thread(() -> {
        try {
            File appDir = getExternalFilesDir(null);

            // ✅ Tải ảnh patchnotes nếu có
            if (isPatchnotesDownloaded) {
                downloadImagesFromFile(appDir, "patchnotes.json", "patchnotes", "images");
            }

            // ✅ Tải ảnh theme nếu có
            if (isThemeDownloaded) {
                downloadImagesFromFile(appDir, "theme.json", "theme", "images/theme");
            }

            // Chuyển màn hình sau khi tải xong
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
```

## Cấu trúc thư mục ảnh
- **Patchnotes images**: `{appDir}/images/`
  - Ví dụ: `1763058599721.jpg`
  
- **Theme images**: `{appDir}/images/theme/`
  - Ví dụ: `1763058599721.jpg`, `1763059034959.jpg`

## Cách hoạt động
1. Download `theme.json` từ Firebase khi `index` thay đổi
2. Đánh dấu `isThemeDownloaded = true`
3. Sau khi tất cả files được tải (version, patchnotes, theme), gọi `downloadImagesFromJson()`
4. Download ảnh từ cả patchnotes VÀ theme (nếu cả hai đều có)
5. Lưu ảnh theme vào `images/theme/` với tên theo `id` từ JSON

## Testing
Để test xem ảnh theme có được tải không:
1. Thay đổi `index` trong Firebase document `update/theme`
2. Chạy lại app
3. Kiểm tra thư mục: `{appDir}/images/theme/`
4. Các file ảnh nên có tên theo `id` trong JSON (ví dụ: `1763058599721.jpg`)

## File đã sửa
- `DownloadData.java` - Refactor logic download ảnh để hỗ trợ cả patchnotes và theme

