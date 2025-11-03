# 🎉 Achievement Notification - Quick Start

## ✅ Đã hoàn thành

Tôi đã tạo một utility class để hiển thị thông báo kiểu achievement với animation trượt từ phải sang trái!

---

## 📁 Files đã tạo

1. **`AchievementNotificationUtil.java`** - Utility class chính
   - Location: `app/src/main/java/org/levimc/launcher/utils/`
   
2. **`notification_background.xml`** - Background đẹp với border vàng
   - Location: `app/src/main/res/drawable/`

3. **`ACHIEVEMENT_NOTIFICATION_GUIDE.md`** - Hướng dẫn chi tiết

---

## 🚀 Cách sử dụng đơn giản

### Cách 1: Đơn giản nhất
```java
AchievementNotificationUtil.showNotification(
    activity,
    "Tiêu đề",
    "Mô tả"
);
```

### Cách 2: Với custom icon
```java
AchievementNotificationUtil.showNotification(
    activity,
    R.drawable.ic_coin,     // Icon của bạn
    "Nhận xu!",
    "+10 xu"
);
```

### Cách 3: Builder pattern (tùy chỉnh nhiều)
```java
new AchievementNotificationUtil.Builder(activity)
    .setIcon(R.drawable.ic_star)
    .setTitle("Achievement!")
    .setDescription("Bạn đã hoàn thành")
    .setDisplayDuration(5000)  // 5 giây
    .show();
```

---

## ✨ Tính năng

- ✅ Animation **trượt từ phải sang trái**
- ✅ Xuất hiện ở **góc trên bên trái** màn hình
- ✅ **Tự động biến mất** sau 3 giây
- ✅ **Click để đóng** sớm
- ✅ Background đẹp với **border vàng**
- ✅ Font **Minecraft** style

---

## 🎮 Đã tích hợp vào getCoin.java

Notification sẽ tự động hiển thị khi:

1. **Nhận xu từ quảng cáo** (Unity Ads)
   ```
   "Nhận thưởng!"
   "+10 LF Coins từ quảng cáo"
   ```

2. **Nhận daily reward**
   ```
   "Phần thưởng hàng ngày!"
   "+5 LF Coins"
   ```

---

## 📖 Xem thêm

Đọc file **`ACHIEVEMENT_NOTIFICATION_GUIDE.md`** để biết:
- Nhiều ví dụ thực tế hơn
- Cách tùy chỉnh chi tiết
- Troubleshooting
- Best practices

---

## 🎨 Demo

Khi bạn chạy app và:
1. Xem quảng cáo → Notification trượt từ phải
2. Claim daily reward → Notification xuất hiện
3. Notification tự động biến mất sau 3 giây

**Hoặc click vào notification để đóng ngay lập tức!**

---

Enjoy! 🚀

