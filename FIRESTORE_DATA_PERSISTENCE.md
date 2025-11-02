# ✅ Lưu Trữ Dữ Liệu Với Firebase Firestore

## 📌 Vấn Đề
Trước đây, ứng dụng lưu trạng thái (số lượt xem quảng cáo, phần thưởng hàng ngày) vào **SharedPreferences** (lưu local trên thiết bị). Khi người dùng xóa dữ liệu ứng dụng, tất cả thông tin sẽ bị mất.

## 🔧 Giải Pháp
Hiện tại, ứng dụng đã được cập nhật để lưu trữ dữ liệu quan trọng lên **Firebase Firestore**. Dữ liệu này được lưu trữ trên cloud và liên kết với `user_id` của người dùng.

## 📊 Cấu Trúc Dữ Liệu Firestore

### Collection: `users`
Mỗi document có ID = `user_id` của người dùng.

```
users (collection)
├── {user_id} (document)
│   ├── lastAdDate: "2025-10-28"          // Ngày xem quảng cáo gần nhất
│   ├── adsWatchedToday: 5                // Số lượt xem quảng cáo hôm nay
│   ├── lastDailyReward: "2025-10-28"     // Ngày nhận phần thưởng hàng ngày gần nhất
│   └── ... (các trường khác)
```

## 🔄 Cách Hoạt Động

### 1. Khi người dùng mở màn hình Get Coins
```java
loadUserAdDataFromFirestore()
```
- Tải dữ liệu từ Firestore
- Kiểm tra ngày hiện tại
- Nếu là ngày mới → reset số lượt xem quảng cáo về 0
- Nếu cùng ngày → lấy số lượt đã xem

### 2. Khi người dùng xem quảng cáo
```java
saveAdWatchCountToFirestore()
```
- Tăng `adsWatchedToday` lên 1
- Cập nhật `lastAdDate` = ngày hôm nay
- Lưu lên Firestore

### 3. Khi người dùng nhận phần thưởng hàng ngày
```java
claimDailyReward()
```
- Cập nhật `lastDailyReward` = ngày hôm nay
- Lưu lên Firestore
- Server sẽ cộng coins vào tài khoản

## 🔐 Lợi Ích

### ✅ Dữ liệu không bị mất khi:
- Xóa dữ liệu ứng dụng
- Gỡ cài đặt và cài lại ứng dụng
- Đổi thiết bị (nếu đăng nhập cùng tài khoản)

### ✅ Bảo mật
- Dữ liệu được lưu trên server của Google
- Mỗi người dùng chỉ truy cập được dữ liệu của mình
- Không thể cheat bằng cách xóa dữ liệu local

### ✅ Đồng bộ
- Dữ liệu tự động đồng bộ giữa các thiết bị
- Khi người dùng đăng nhập trên thiết bị mới, dữ liệu vẫn còn

## 📝 Code Được Thay Đổi

### File: `getCoin.java`

#### Trước đây (lưu local):
```java
private void saveAdWatchCount() {
    prefs.edit()
        .putInt("ad_watch_count", adsWatchedToday)
        .apply();
}
```

#### Bây giờ (lưu Firestore):
```java
private void saveAdWatchCountToFirestore() {
    String today = getTodayDate();
    Map<String, Object> updates = new HashMap<>();
    updates.put("lastAdDate", today);
    updates.put("adsWatchedToday", adsWatchedToday);
    
    db.collection("users").document(userId)
        .update(updates)
        .addOnSuccessListener(aVoid -> {
            Log.d(TAG, "Ad watch count saved: " + adsWatchedToday);
        })
        .addOnFailureListener(e -> {
            Log.e(TAG, "Failed to save ad watch count", e);
        });
}
```

## 🚀 Kiểm Tra

### Test Case 1: Xóa dữ liệu ứng dụng
1. Xem 3 quảng cáo
2. Vào Settings → Apps → LeviLaunchroid → Clear Data
3. Mở lại app và đăng nhập
4. ✅ Số lượt xem quảng cáo vẫn còn 3/10

### Test Case 2: Đổi ngày
1. Xem 5 quảng cáo hôm nay
2. Đợi qua ngày mới (hoặc thay đổi ngày hệ thống)
3. Mở lại app
4. ✅ Số lượt xem quảng cáo reset về 0/10

### Test Case 3: Nhận phần thưởng hàng ngày
1. Nhấn nút "CLAIM" để nhận 3 coins
2. Xóa dữ liệu app và đăng nhập lại
3. ✅ Nút "CLAIM" vẫn hiển thị "CLAIMED"
4. Đợi qua ngày mới
5. ✅ Nút "CLAIM" có thể nhấn lại

## 🔍 Debug

### Xem dữ liệu trong Firestore:
1. Vào Firebase Console: https://console.firebase.google.com/
2. Chọn project: `lastfom-launcher`
3. Vào Firestore Database
4. Tìm collection: `users`
5. Tìm document theo `user_id`

### Kiểm tra logs:
```bash
adb logcat | grep getCoin
```

Logs quan trọng:
- `Ad watch count saved: X` → Đã lưu số lượt xem
- `User ad document created` → Tạo document mới cho user
- `Failed to save ad watch count` → Lỗi khi lưu

## 📌 Lưu Ý

1. **Yêu cầu đăng nhập**: User phải đăng nhập để có `user_id` hợp lệ
2. **Yêu cầu internet**: Cần kết nối internet để đồng bộ với Firestore
3. **Offline mode**: Firestore có hỗ trợ offline cache, nhưng nên sync khi có internet
4. **Security Rules**: Cần cấu hình Firestore Rules để bảo mật dữ liệu

## ⚠️ Xử Lý Lỗi Thường Gặp

### Lỗi: PERMISSION_DENIED
```
FirebaseFirestoreException: PERMISSION_DENIED: Missing or insufficient permissions
```

**Nguyên nhân**: Firestore Security Rules chặn quyền ghi dữ liệu.

**Giải pháp**:
1. Chạy script: `fix-firestore-permission.bat`
2. Hoặc xem hướng dẫn chi tiết trong: `FIRESTORE_SECURITY_RULES_FIX.md`
3. Cấu hình Rules trong Firebase Console

**Rules khuyến nghị** (cho development):
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read: if true;
      allow create: if request.resource.data.keys().hasAll(['lastAdDate', 'adsWatchedToday']);
      allow update: if request.resource.data.diff(resource.data).affectedKeys()
                       .hasOnly(['lastAdDate', 'adsWatchedToday', 'lastDailyReward', 'userId']);
    }
    match /marketplace/{itemId} {
      allow read: if true;
      allow write: if true;
    }
  }
}
```

### Lỗi: Document does not exist
Code đã được cập nhật để tự động tạo document nếu chưa tồn tại bằng cách sử dụng `set()` với `SetOptions.merge()`.

### Lỗi: Network connection failed
Firestore có offline cache, dữ liệu sẽ tự động sync khi có internet trở lại.

## 🔒 Firestore Security Rules (Khuyến nghị)

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      // Chỉ cho phép user đọc/ghi dữ liệu của chính mình
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

## 📚 Tài Liệu Tham Khảo
- [Firebase Firestore Documentation](https://firebase.google.com/docs/firestore)
- [Android Firestore Get Started](https://firebase.google.com/docs/firestore/quickstart)

