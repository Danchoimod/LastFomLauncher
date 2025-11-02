# ✅ Checklist: Fix Coin Not Incrementing

## 📋 Bước 1: Cập nhật Code (✅ ĐÃ XONG)
- [x] Import `FieldValue` từ Firestore
- [x] Cập nhật `claimDailyReward()` để increment coins
- [x] Thêm method `grantAdRewardCoins()`
- [x] Cập nhật `onUserEarnedReward()` để gọi `grantAdRewardCoins()`
- [x] Xử lý fallback nếu document chưa tồn tại

## 📋 Bước 2: Cập nhật Firestore Rules (⚠️ BẠN CẦN LÀM)

### Cách 1: Dùng Script (Nhanh nhất)
```bash
cd C:\APp\LeviLaunchroid-1.0.15
open-firebase-console.bat
```

### Cách 2: Thủ công
1. [ ] Mở: https://console.firebase.google.com/
2. [ ] Chọn project: **lastfom-launcher**
3. [ ] Vào: **Firestore Database** → **Rules** (tab trên)
4. [ ] Copy rules dưới đây:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read: if true;
      allow create: if request.resource.data.keys().hasAll(['lastAdDate', 'adsWatchedToday']);
      allow update: if request.resource.data.diff(resource.data).affectedKeys()
                       .hasOnly(['lastAdDate', 'adsWatchedToday', 'lastDailyReward', 'userId', 'coin']);
    }
    match /marketplace/{itemId} {
      allow read: if true;
      allow write: if true;
    }
  }
}
```

5. [ ] Nhấn nút **PUBLISH** (màu xanh)
6. [ ] Đợi 1-2 phút để rules được áp dụng

## 📋 Bước 3: Build & Test App

### Build App
```bash
cd C:\APp\LeviLaunchroid-1.0.15
gradlew assembleDebug
```

### Install to Device
```bash
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

### Test Cases
- [ ] **Test 1: Daily Reward**
  1. Mở app
  2. Vào Get Coins
  3. Nhấn "CLAIM"
  4. ✅ Toast: "Claimed 3 coins successfully! ✓"
  5. Kiểm tra Firestore: `coin` tăng 3

- [ ] **Test 2: Watch Ad**
  1. Nhấn "WATCH AD →"
  2. Xem quảng cáo đến hết
  3. ✅ Toast: "+10 coins earned! ✓"
  4. Kiểm tra Firestore: `coin` tăng 10

- [ ] **Test 3: Persistence (Quan trọng!)**
  1. Settings → Apps → LeviLaunchroid → Clear Data
  2. Mở lại app, login
  3. ✅ Coins vẫn đúng (không bị reset)
  4. ✅ Daily reward status vẫn đúng

## 📋 Bước 4: Monitor Logs

```bash
adb logcat | grep -E "(getCoin|coin|Firestore)"
```

### Logs Thành Công
```
getCoin: Daily reward claimed successfully! +3 coins
getCoin: Ad reward granted: +10 coins
getCoin: Ad watch count saved: 1
```

### Logs Lỗi (Nếu có)
```
getCoin: Failed to claim daily reward: PERMISSION_DENIED
→ Solution: Cập nhật Firestore Rules (xem Bước 2)

getCoin: Failed to grant ad reward: Document not found
→ Solution: App sẽ tự retry, hoặc tạo document thủ công
```

## 📋 Bước 5: Verify trong Firestore Console

1. [ ] Mở: https://console.firebase.google.com/
2. [ ] Vào: Firestore Database → Data
3. [ ] Tìm collection: `users`
4. [ ] Tìm document: `{your_user_id}`
5. [ ] Kiểm tra các fields:
   - [x] `coin`: 113 (hoặc số nào đó > 0)
   - [x] `lastDailyReward`: "2025-10-28"
   - [x] `adsWatchedToday`: 1-10
   - [x] `lastAdDate`: "2025-10-28"

## 🔍 Troubleshooting

### Vấn đề 1: Coins vẫn không tăng
**Nguyên nhân**: Firestore Rules chưa được cập nhật
**Giải pháp**: 
1. Kiểm tra lại Rules trong Firebase Console
2. Đảm bảo có field `'coin'` trong `.hasOnly([...])`
3. Đợi 2-3 phút rồi test lại

### Vấn đề 2: PERMISSION_DENIED error
**Nguyên nhân**: Rules quá chặt hoặc chưa publish
**Giải pháp**:
1. Dùng rules đơn giản để test:
   ```javascript
   match /{document=**} {
     allow read, write: if true;
   }
   ```
2. Sau khi test xong, đổi lại rules bảo mật

### Vấn đề 3: Toast hiển thị success nhưng coins không tăng
**Nguyên nhân**: Lỗi silent (không throw exception)
**Giải pháp**:
1. Check logs: `adb logcat | grep getCoin`
2. Xem có lỗi "Failed to grant" không
3. Kiểm tra internet connection

## 📊 Expected Results

| Action | Before | After | Change |
|--------|--------|-------|--------|
| Claim Daily | coin: 100 | coin: 103 | +3 |
| Watch 1 Ad | coin: 103 | coin: 113 | +10 |
| Watch 10 Ads | coin: 100 | coin: 200 | +100 |

## 🎯 Final Verification

- [ ] User có thể claim daily reward → coins tăng 3
- [ ] User có thể xem quảng cáo → coins tăng 10
- [ ] Tối đa 10 quảng cáo/ngày
- [ ] Daily reward chỉ claim 1 lần/ngày
- [ ] Xóa data app → coins không bị mất
- [ ] Logs không có lỗi PERMISSION_DENIED
- [ ] Firestore data cập nhật real-time

## 📁 Documentation

Đọc thêm chi tiết trong:
- `COIN_REWARD_SYSTEM_FIXED.md` - Giải thích chi tiết cách hoạt động
- `FIRESTORE_SECURITY_RULES_FIX.md` - Hướng dẫn cấu hình rules
- `FIRESTORE_DATA_PERSISTENCE.md` - Giải thích về data persistence

---

**🚀 Bắt đầu từ Bước 2: Cập nhật Firestore Rules!**

