# Activity Loop Fix - Hướng dẫn sửa lỗi vòng lặp Activity

## Vấn đề

Khi thoát app và vào lại, app bị loop mở các activity liên tục:
- SplashActivity → DownloadData → MainLauncher → SplashActivity → ...

## Nguyên nhân

1. **SplashActivity là LAUNCHER activity**: Mỗi khi mở app, SplashActivity luôn được khởi chạy
2. **Sử dụng FLAG_ACTIVITY_CLEAR_TASK không đúng cách**: Khi kết hợp với `launchMode="singleTask"`, có thể gây ra vòng lặp
3. **Không kiểm tra app đã running**: SplashActivity chạy lại toàn bộ flow mỗi khi vào app

## Giải pháp đã áp dụng

### 1. Thêm kiểm tra `isTaskRoot()` trong SplashActivity

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    // Kiểm tra nếu app đã running và người dùng chỉ đang quay lại
    if (!isTaskRoot()) {
        // App đã running, không phải lần đầu khởi động
        finish();
        return;
    }
    
    // ... phần còn lại của onCreate
}
```

**Giải thích:**
- `isTaskRoot()` trả về `true` nếu activity này là activity gốc của task
- Nếu `false`, nghĩa là đã có activity khác trong stack → app đang running
- Trong trường hợp này, chỉ cần `finish()` SplashActivity để quay về activity trước đó

### 2. Thêm `noHistory="true"` trong AndroidManifest

```xml
<!-- SplashActivity -->
<activity
    android:name="org.levimc.launcher.ui.activities.SplashActivity"
    android:exported="true"
    android:screenOrientation="sensorLandscape"
    android:launchMode="singleTask"
    android:noHistory="true"
    tools:ignore="DiscouragedApi">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>

<!-- DownloadData -->
<activity
    android:name="org.levimc.launcher.ui.activities.DownloadData"
    android:exported="false"
    android:screenOrientation="sensorLandscape"
    android:noHistory="true"
    tools:ignore="DiscouragedApi" />
```

**Giải thích:**
- `noHistory="true"`: Activity sẽ không được giữ trong back stack
- Khi người dùng navigate đi, activity này sẽ tự động finish
- Phù hợp cho splash screen và download screen vì không cần quay lại

### 3. Bỏ FLAG_ACTIVITY_CLEAR_TASK

**Trước:**
```java
Intent intent = new Intent(this, MainLauncher.class);
intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
startActivity(intent);
finish();
```

**Sau:**
```java
Intent intent = new Intent(this, MainLauncher.class);
startActivity(intent);
finish();
```

**Giải thích:**
- Không cần `CLEAR_TASK` vì đã có `noHistory="true"`
- `CLEAR_TASK` + `singleTask` launchMode có thể gây conflict
- Đơn giản hóa flow navigation

## Luồng hoạt động sau khi sửa

### Lần đầu mở app:
1. **SplashActivity** (isTaskRoot = true)
   - Kiểm tra login
   - Kiểm tra license
   - Kiểm tra update
2. **DownloadData** (nếu có update)
   - Tải dữ liệu
   - Auto finish sau khi xong (noHistory)
3. **MainLauncher**
   - Màn hình chính

### Khi thoát app và vào lại:
1. **SplashActivity** được launch (LAUNCHER intent)
2. Kiểm tra `isTaskRoot()` → `false` (vì MainLauncher đang trong stack)
3. `finish()` ngay lập tức
4. Quay về **MainLauncher** (activity vẫn đang sống)

### Khi app bị kill và mở lại:
1. **SplashActivity** (isTaskRoot = true)
2. Flow như lần đầu mở app

## Launch Modes giải thích

### singleTask
- Chỉ có 1 instance của activity trong hệ thống
- Nếu activity đã tồn tại, đưa nó lên trên thay vì tạo mới
- Các activity phía trên sẽ bị clear

### noHistory
- Activity không được lưu trong back stack
- Khi navigate đi, activity tự động finish
- Phù hợp cho splash, tutorial, download screens

## Testing checklist

- [x] Lần đầu mở app → hiển thị SplashActivity → chuyển đến MainLauncher
- [x] Nhấn HOME → vào lại app → hiển thị MainLauncher (không hiện Splash)
- [x] Nhấn BACK từ MainLauncher → Exit app (không quay về Splash)
- [x] Force stop app → mở lại → hiển thị SplashActivity
- [x] Có update mới → hiển thị DownloadData → chuyển MainLauncher
- [x] Nhấn HOME khi đang tải → vào lại → tiếp tục tải (hoặc hiện MainLauncher)

## Lưu ý quan trọng

1. **Không dùng CLEAR_TASK với singleTask**: Có thể gây ra hành vi không mong muốn
2. **isTaskRoot() để phát hiện app state**: Cách tốt nhất để biết app đang running
3. **noHistory cho transient screens**: Splash, Loading, Tutorial không nên ở trong back stack
4. **Test kỹ flow navigation**: Đặc biệt là HOME → Resume và Force Stop → Relaunch

## Troubleshooting

### Vẫn bị loop sau khi sửa?
- Kiểm tra có activity nào khác gọi lại SplashActivity không
- Kiểm tra AndroidManifest có duplicate activity declarations không
- Clear app data và test lại

### MainLauncher bị recreate khi resume?
- Kiểm tra `android:configChanges` trong manifest
- Kiểm tra có `finish()` không đúng chỗ không
- Thêm log để trace lifecycle

### DownloadData không tự finish?
- Đảm bảo `goToNextScreen()` được gọi
- Kiểm tra `noHistory="true"` đã được thêm
- Kiểm tra thread không bị block

## Code liên quan

### Files đã sửa:
1. `SplashActivity.java` - Thêm isTaskRoot() check, bỏ CLEAR_TASK flags
2. `AndroidManifest.xml` - Thêm noHistory cho SplashActivity và DownloadData

### Files liên quan:
- `DownloadData.java` - Download và chuyển đến MainLauncher
- `MainLauncher.java` - Màn hình chính
- `WelcomeActivity.java` - Màn hình welcome/login

## Tài liệu tham khảo

- [Android Task và Back Stack](https://developer.android.com/guide/components/activities/tasks-and-back-stack)
- [Launch Modes](https://developer.android.com/guide/components/activities/tasks-and-back-stack#TaskLaunchModes)
- [Intent Flags](https://developer.android.com/reference/android/content/Intent#flags)

