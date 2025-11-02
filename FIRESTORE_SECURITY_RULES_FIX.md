# 🔒 Fix Firestore Permission Denied Error

## ❌ Lỗi Hiện Tại
```
PERMISSION_DENIED: Missing or insufficient permissions.
```

**Nguyên nhân**: Firestore Security Rules hiện tại không cho phép app ghi dữ liệu vào collection `users`.

## ✅ Giải Pháp

### Bước 1: Truy cập Firebase Console
1. Vào: https://console.firebase.google.com/
2. Chọn project: **lastfom-launcher**
3. Ở menu bên trái, chọn **Firestore Database**
4. Chọn tab **Rules** (ở trên cùng)

### Bước 2: Cập nhật Security Rules

#### ⚠️ Cách 1: Rules Tạm Thời (Dùng cho Development/Testing)
**Lưu ý**: Cách này CHO PHÉP MỌI NGƯỜI đọc/ghi dữ liệu. CHỈ dùng để test!

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // CHỈ DÙNG CHO TESTING - XÓA KHI RA SẢN PHẨM
    match /{document=**} {
      allow read, write: if true;
    }
  }
}
```

**Khi nào dùng**: Khi bạn muốn test nhanh, chưa có hệ thống authentication.

---

#### ✅ Cách 2: Rules Bảo Mật (Khuyến nghị - Dùng cho Production)

##### Option A: Nếu app KHÔNG có Firebase Authentication

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Cho phép đọc/ghi vào collection users
    match /users/{userId} {
      // Mọi người có thể đọc
      allow read: if true;
      
      // Chỉ cho phép ghi nếu userId trùng với user_id trong request
      allow write: if request.resource.data.keys().hasAll(['lastAdDate', 'adsWatchedToday']) 
                   && userId == request.resource.data.get('userId', '');
    }
    
    // Cho phép đọc/ghi marketplace
    match /marketplace/{itemId} {
      allow read: if true;
      allow write: if true; // Hoặc thêm điều kiện admin
    }
  }
}
```

##### Option B: Nếu app CÓ Firebase Authentication (đăng nhập bằng Google/Email)

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Chỉ cho phép user đọc/ghi dữ liệu của chính mình
    match /users/{userId} {
      allow read: if request.auth != null && request.auth.uid == userId;
      allow write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Marketplace: mọi người đọc được, chỉ admin mới ghi được
    match /marketplace/{itemId} {
      allow read: if true;
      allow write: if request.auth != null; // Hoặc thêm điều kiện admin
    }
  }
}
```

##### Option C: Rules Linh Hoạt (Khuyến nghị nhất)

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // === USERS COLLECTION ===
    match /users/{userId} {
      // Cho phép mọi người đọc thông tin cơ bản
      allow read: if true;
      
      // Cho phép ghi nếu:
      // - Có xác thực và đúng userId, HOẶC
      // - Là tạo mới và có các trường bắt buộc
      allow create: if request.resource.data.keys().hasAll(['lastAdDate', 'adsWatchedToday']);
      
      // Cho phép update nếu:
      // - Chỉ cập nhật các trường cho phép (lastAdDate, adsWatchedToday, lastDailyReward, coin)
      allow update: if request.resource.data.diff(resource.data).affectedKeys()
                       .hasOnly(['lastAdDate', 'adsWatchedToday', 'lastDailyReward', 'userId', 'coin']);
    }
    
    // === MARKETPLACE COLLECTION ===
    match /marketplace/{itemId} {
      allow read: if true;
      allow write: if true; // Nếu muốn chỉ admin: request.auth.token.admin == true
    }
  }
}
```

### Bước 3: Publish Rules
1. Sau khi chỉnh sửa xong, nhấn nút **Publish** (màu xanh)
2. Đợi vài giây để rules được áp dụng
3. Test lại app

---

## 🧪 Kiểm Tra Rules

### Test trong Firebase Console
1. Vào tab **Rules** 
2. Nhấn **Rules Playground** (góc phải màn hình)
3. Test các trường hợp:
   - **Read**: `get /users/{userId}`
   - **Write**: `create /users/{userId}` với data mẫu

### Test trong App
1. Mở app và login
2. Nhấn "CLAIM" để nhận phần thưởng hàng ngày
3. Xem logs:
   ```bash
   adb logcat | grep -E "(getCoin|Firestore)"
   ```
4. Nếu thành công, sẽ thấy: "Ad watch count saved: X"

---

## 🚨 Troubleshooting

### Lỗi vẫn còn sau khi đổi Rules?
- Đợi 1-2 phút để rules được đồng bộ
- Force close app và mở lại
- Xóa cache Firestore: Settings → Apps → LeviLaunchroid → Clear Cache

### Làm sao biết userId của mình?
```bash
adb logcat | grep "user_id"
```
Hoặc thêm log trong code:
```java
Log.d("getCoin", "Current userId: " + userId);
```

### Test với Firestore Emulator (Advanced)
```bash
firebase emulators:start --only firestore
```

---

## 📋 Checklist

- [ ] Đã truy cập Firebase Console
- [ ] Đã chọn đúng project (lastfom-launcher)
- [ ] Đã vào Firestore Database → Rules
- [ ] Đã copy Rules phù hợp (Cách 1, 2, hoặc 3)
- [ ] Đã nhấn Publish
- [ ] Đã đợi 1-2 phút
- [ ] Đã test lại app
- [ ] Không còn lỗi PERMISSION_DENIED

---

## 🎯 Khuyến Nghị

### Cho Development/Testing
➡️ Dùng **Cách 1** (allow all) để test nhanh

### Cho Production
➡️ Dùng **Cách 2 Option C** (Rules linh hoạt)

### Khi có Firebase Authentication
➡️ Dùng **Cách 2 Option B** (xác thực chặt chẽ)

---

## 📚 Tài Liệu Tham Khảo
- [Firestore Security Rules Documentation](https://firebase.google.com/docs/firestore/security/get-started)
- [Rules Language Reference](https://firebase.google.com/docs/firestore/security/rules-structure)
- [Rules Playground](https://firebase.google.com/docs/rules/simulator)

