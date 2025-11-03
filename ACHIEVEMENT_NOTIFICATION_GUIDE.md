# Achievement Notification Utility - Hướng Dẫn Sử Dụng

## 📋 Tổng Quan

`AchievementNotificationUtil` là một utility class giúp hiển thị thông báo kiểu achievement với animation trượt từ phải sang trái, xuất hiện ở góc trên bên trái màn hình.

---

## ✨ Tính Năng

- ✅ Animation trượt từ **phải sang trái** (slide-in from right)
- ✅ Hiển thị ở vị trí **góc trên bên trái** màn hình
- ✅ Có thể **tùy chỉnh icon, title, description**
- ✅ Tự động biến mất sau 3 giây
- ✅ Click để đóng sớm
- ✅ Animation fade-in/fade-out mượt mà
- ✅ Background đẹp với border vàng (giống Minecraft achievement)

---

## 🚀 Cách Sử Dụng

### 1. Cách Đơn Giản (Chỉ title và description)

```java
// Sử dụng icon mặc định (java icon)
AchievementNotificationUtil.showNotification(
    activity,
    "Achievement Unlocked!",
    "Bạn đã hoàn thành nhiệm vụ đầu tiên"
);
```

### 2. Với Custom Icon

```java
// Sử dụng icon tùy chỉnh
AchievementNotificationUtil.showNotification(
    activity,
    R.drawable.ic_coin,           // Icon của bạn
    "Nhận được xu!",              // Title
    "+10 xu từ xem quảng cáo"     // Description
);
```

### 3. Sử dụng Builder Pattern (Tùy chỉnh nâng cao)

```java
new AchievementNotificationUtil.Builder(activity)
    .setIcon(R.drawable.ic_achievement)
    .setTitle("Thành tích mới!")
    .setDescription("Bạn đã đạt cấp 10")
    .setDisplayDuration(5000)      // Hiển thị 5 giây
    .setMarginTop(100)             // Cách top 100dp
    .setMarginStart(20)            // Cách left 20dp
    .show();
```

---

## 💡 Ví Dụ Thực Tế

### Trong Fragment getCoin.java - Khi nhận xu từ quảng cáo:

```java
@Override
public void onUnityAdsShowComplete(String placementId, UnityAds.UnityAdsShowCompletionState state) {
    if (state == UnityAds.UnityAdsShowCompletionState.COMPLETED) {
        // Grant coins
        grantAdRewardCoins();
        
        // Show notification
        AchievementNotificationUtil.showNotification(
            requireActivity(),
            R.drawable.ic_coin,
            "Nhận thưởng!",
            "+10 xu từ quảng cáo"
        );
    }
}
```

### Trong MainActivity - Khi đăng nhập thành công:

```java
private void onLoginSuccess(String username) {
    // Show welcome notification
    AchievementNotificationUtil.showNotification(
        this,
        R.drawable.ic_user,
        "Chào mừng trở lại!",
        "Xin chào " + username
    );
}
```

### Khi mua item thành công:

```java
private void onPurchaseSuccess(String itemName, int cost) {
    new AchievementNotificationUtil.Builder(activity)
        .setIcon(R.drawable.ic_shopping_cart)
        .setTitle("Mua thành công!")
        .setDescription("Đã mua " + itemName + " (-" + cost + " xu)")
        .setDisplayDuration(4000)
        .show();
}
```

### Khi tải mod/resource pack thành công:

```java
private void onDownloadComplete(String modName) {
    AchievementNotificationUtil.showNotification(
        activity,
        R.drawable.ic_download,
        "Tải xuống hoàn tất!",
        modName + " đã sẵn sàng"
    );
}
```

### Khi nhận daily reward:

```java
private void onDailyRewardClaimed(int coins) {
    new AchievementNotificationUtil.Builder(activity)
        .setIcon(R.drawable.ic_gift)
        .setTitle("Phần thưởng hàng ngày!")
        .setDescription("+" + coins + " xu")
        .show();
}
```

---

## 🎨 Tùy Chỉnh

### Thay đổi thời gian hiển thị:

```java
new AchievementNotificationUtil.Builder(activity)
    .setTitle("Thông báo")
    .setDescription("Sẽ biến mất sau 10 giây")
    .setDisplayDuration(10000)  // 10 giây
    .show();
```

### Thay đổi vị trí:

```java
new AchievementNotificationUtil.Builder(activity)
    .setTitle("Thông báo")
    .setDescription("Cách top 200dp")
    .setMarginTop(200)      // Cách top 200dp
    .setMarginStart(50)     // Cách left 50dp
    .show();
```

### Custom icon từ resources:

```java
// Sử dụng icon có sẵn trong drawable
AchievementNotificationUtil.showNotification(
    activity,
    R.drawable.java,        // hoặc bất kỳ drawable nào
    "Custom Icon",
    "Mô tả của bạn"
);
```

---

## 📐 Thông Số Mặc Định

| Tham số | Giá trị | Mô tả |
|---------|---------|-------|
| `ANIMATION_DURATION` | 500ms | Thời gian animation slide |
| `DISPLAY_DURATION` | 3000ms | Thời gian hiển thị (3 giây) |
| `MARGIN_TOP` | 80dp | Khoảng cách từ top |
| `MARGIN_START` | 16dp | Khoảng cách từ left |

---

## 🎭 Animation Chi Tiết

### Slide-in (Trượt vào):
1. Notification bắt đầu từ **ngoài màn hình bên phải**
2. Trượt sang **trái** trong 500ms
3. Dừng lại ở vị trí **góc trên bên trái**
4. Đồng thời có **fade-in effect** (mờ dần rõ)

### Slide-out (Trượt ra):
1. Sau 3 giây (hoặc khi click)
2. Trượt từ **vị trí hiện tại** sang **ngoài màn hình bên phải**
3. Đồng thời có **fade-out effect** (rõ dần mờ)
4. Tự động xóa view khỏi hierarchy

---

## 🎨 Giao Diện

### Background Style:
- Màu nền: Đen trong suốt (`#DD000000`)
- Border: Vàng 2dp (`#FFD700`)
- Rounded corners: 8dp
- Elevation: 8dp (shadow)

### Text Style:
- **Title**: Vàng (`#FFFF00`), 18sp, bold, font Minecraft
- **Description**: Trắng (`#FFFFFF`), 16sp, font Minecraft

### Icon:
- Kích thước: 48dp x 48dp
- ScaleType: fitCenter
- Margin right: 8dp

---

## ⚠️ Lưu Ý Quan Trọng

### 1. Chỉ gọi từ Activity hoặc Fragment:
```java
// ✅ ĐÚNG
AchievementNotificationUtil.showNotification(activity, "Title", "Desc");

// ✅ ĐÚNG (trong Fragment)
AchievementNotificationUtil.showNotification(requireActivity(), "Title", "Desc");

// ❌ SAI (không có activity context)
AchievementNotificationUtil.showNotification(getApplicationContext(), "Title", "Desc");
```

### 2. Kiểm tra Activity còn sống:
```java
if (activity != null && !activity.isFinishing()) {
    AchievementNotificationUtil.showNotification(activity, "Title", "Desc");
}
```

### 3. Sử dụng trong background thread:
```java
// Utility tự động chạy trên UI thread, nhưng nếu gọi từ background:
new Handler(Looper.getMainLooper()).post(() -> {
    AchievementNotificationUtil.showNotification(activity, "Title", "Desc");
});
```

---

## 🔧 Troubleshooting

### Notification không xuất hiện?
1. Kiểm tra activity còn sống: `!activity.isFinishing()`
2. Kiểm tra layout `notification.xml` tồn tại
3. Kiểm tra drawable `notification_background.xml` tồn tại
4. Kiểm tra icon resource tồn tại

### Notification bị crop (cắt)?
- Tăng `minWidth` trong `notification.xml`
- Kiểm tra text quá dài

### Animation không mượt?
- Đảm bảo không chạy quá nhiều notification cùng lúc
- Giảm `ANIMATION_DURATION` nếu cần

### Click không dismiss?
- Đảm bảo `android:clickable="true"` trong layout
- Kiểm tra không có view nào che phủ notification

---

## 📱 Ví Dụ Hoàn Chỉnh

```java
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Test notification
        findViewById(R.id.btnTest).setOnClickListener(v -> {
            showTestNotification();
        });
    }

    private void showTestNotification() {
        // Cách 1: Đơn giản
        AchievementNotificationUtil.showNotification(
            this,
            "Test Notification",
            "Click để xem notification!"
        );

        // Cách 2: Với custom icon
        new Handler().postDelayed(() -> {
            AchievementNotificationUtil.showNotification(
                this,
                R.drawable.ic_star,
                "Achievement!",
                "Bạn đã mở khóa thành tích mới"
            );
        }, 4000);

        // Cách 3: Builder pattern
        new Handler().postDelayed(() -> {
            new AchievementNotificationUtil.Builder(this)
                .setIcon(R.drawable.ic_coin)
                .setTitle("Nhận xu!")
                .setDescription("+100 xu từ sự kiện đặc biệt")
                .setDisplayDuration(5000)
                .show();
        }, 8000);
    }
}
```

---

## 🎉 Hoàn Thành!

Bây giờ bạn có thể sử dụng `AchievementNotificationUtil` để hiển thị thông báo đẹp mắt với animation mượt mà trong app của mình!

**Các icon có sẵn trong project:**
- `R.drawable.java` - Icon mặc định
- `R.drawable.ic_coin` - Icon xu (nếu có)
- Bất kỳ drawable nào trong `res/drawable/`

**Tips:**
- Sử dụng cho các thông báo quan trọng
- Không spam quá nhiều notification
- Kết hợp với sound effect cho trải nghiệm tốt hơn
- Có thể thêm vibration khi notification xuất hiện

Chúc bạn thành công! 🚀

