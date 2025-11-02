# Hệ thống xử lý Crash cho LeviLauncher

## Các file đã tạo:

1. **CrashActivity.java** - Màn hình hiển thị lỗi khi app crash
2. **CrashHandler.java** - Handler bắt các exception không được xử lý
3. **LauncherApplication.java** - Application class để khởi tạo CrashHandler
4. **activity_crash.xml** - Layout cho màn hình crash
5. **TestCrashActivity.java** - Activity để test crash handler (có thể xóa sau khi test)

## Cách hoạt động:

1. Khi app khởi động, `LauncherApplication.onCreate()` được gọi
2. `CrashHandler.init()` được gọi để đăng ký handler toàn cục
3. Khi có exception không được bắt, `CrashHandler.uncaughtException()` được gọi
4. CrashHandler sẽ:
   - Log thông tin lỗi
   - Tạo Intent mở CrashActivity với thông tin lỗi
   - Kết thúc activity hiện tại
   - Mở CrashActivity
5. CrashActivity hiển thị:
   - Thông báo lỗi
   - Stack trace đầy đủ
   - 3 nút: Sao chép, Khởi động lại, Đóng

## Cách test:

### Cách 1: Sử dụng TestCrashActivity
Thêm code này vào một activity bất kỳ để mở TestCrashActivity:

```java
Button testButton = findViewById(R.id.your_button_id);
testButton.setOnClickListener(v -> {
    Intent intent = new Intent(this, TestCrashActivity.class);
    startActivity(intent);
});
```

### Cách 2: Tạo crash thủ công
Thêm code gây crash vào bất kỳ đâu trong app:

```java
// Test NullPointerException
String test = null;
test.length();

// Hoặc test RuntimeException
throw new RuntimeException("Test crash");

// Hoặc test ArrayIndexOutOfBoundsException
int[] arr = new int[1];
int x = arr[10];
```

## Các tính năng của màn hình crash:

1. **Sao chép lỗi** - Copy toàn bộ thông tin lỗi vào clipboard để dễ báo lỗi
2. **Khởi động lại** - Restart app từ đầu
3. **Đóng** - Đóng app hoàn toàn

## Lưu ý:

- Màn hình crash sử dụng findViewById thay vì ViewBinding để tránh lỗi
- Tất cả code đều có try-catch để đảm bảo không bị crash trong crash handler
- CrashActivity sử dụng WeakReference để tránh memory leak
- Không lưu reference đến CrashActivity trong lifecycle callbacks để tránh vòng lặp

## Xóa test code sau khi hoàn tất:

Sau khi test xong, có thể xóa:
1. TestCrashActivity.java
2. Dòng khai báo TestCrashActivity trong AndroidManifest.xml

