# ✅ AdMob SSV Production Checklist

## 🎯 Mục tiêu
Cho phép user xem quảng cáo và nhận **10 LFCoins/video** (tối đa 10 videos/ngày)

---

## 📋 Checklist (Làm theo thứ tự)

### Phase 1: Chuẩn bị thông tin

- [ ] **1.1** Lấy **Ad Unit ID** từ AdMob Console
  - Link: https://apps.admob.com
  - Vào: **Ad units** → Chọn **Rewarded Ad Unit**
  - Copy ID (format: `ca-app-pub-XXXXXXXX/YYYYYY`)
  - Ghi lại: `______________________________`

- [ ] **1.2** Xác nhận **Vercel URL** của API
  - Link: https://vercel.com/dashboard
  - Format: `https://your-project.vercel.app`
  - Ghi lại: `______________________________`

- [ ] **1.3** Kiểm tra **Firebase Project ID**
  - Check file: `app/src/google-services.json`
  - Xác nhận: `lastfom-launcher` ✅

---

### Phase 2: Cấu hình code Android

- [ ] **2.1** Mở file `getCoin.java` (line 32)
  ```
  Path: app/src/main/java/org/levimc/launcher/ui/fragment/getCoin.java
  ```

- [ ] **2.2** Thay **Test Ad Unit ID** → **Real Ad Unit ID**
  ```java
  // BEFORE:
  private static final String AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917";
  
  // AFTER:
  private static final String AD_UNIT_ID = "ca-app-pub-XXXXXXXX/YYYYYY"; // ← Your real ID
  ```

- [ ] **2.3** Save file

---

### Phase 3: Cấu hình AdMob Console

- [ ] **3.1** Đăng nhập AdMob Console
  - Link: https://apps.admob.com

- [ ] **3.2** Vào **Settings** ⚙️ → **Account** → **Account information**

- [ ] **3.3** Kéo xuống phần **"Apps"** → Click vào app **LastFom Launcher**

- [ ] **3.4** Tìm phần **"Rewarded ads"** hoặc **"Server-side verification"**

- [ ] **3.5** Click **"Add URL"** hoặc **"Configure"**

- [ ] **3.6** Nhập SSV URL:
  ```
  https://your-project.vercel.app/api/verify-reward
  ```
  
- [ ] **3.7** Click **Save** ✅

- [ ] **3.8** Chờ 5-10 phút để AdMob sync (quan trọng!)

---

### Phase 4: Cấu hình Vercel (Firebase Service Account)

- [ ] **4.1** Kiểm tra env var trên Vercel
  - Link: https://vercel.com/dashboard → Your Project → **Settings** → **Environment Variables**
  - Tìm biến: `FIREBASE_SERVICE_ACCOUNT`

- [ ] **4.2** Nếu chưa có, tạo Service Account Key:
  - [ ] **4.2a** Vào Firebase Console:
    ```
    https://console.firebase.google.com/project/lastfom-launcher/settings/serviceaccounts/adminsdk
    ```
  
  - [ ] **4.2b** Click **"Generate new private key"** → Download JSON file
  
  - [ ] **4.2c** Convert JSON → Base64:
    ```powershell
    # Windows PowerShell:
    [Convert]::ToBase64String([IO.File]::ReadAllBytes("C:\path\to\serviceAccountKey.json"))
    ```
  
  - [ ] **4.2d** Add vào Vercel:
    - Name: `FIREBASE_SERVICE_ACCOUNT`
    - Value: `<Base64 string>`
    - Environments: Production + Preview + Development
    - Click **Save**

- [ ] **4.3** Redeploy Vercel project
  - Vào: **Deployments** → Click **"Redeploy"**
  - Hoặc: Push code lên Git để auto-deploy

---

### Phase 5: Test API

- [ ] **5.1** Chạy test script:
  ```powershell
  cd C:\APp\LeviLaunchroid-1.0.15
  .\test-admob-api.ps1 -VercelUrl "https://your-project.vercel.app"
  ```

- [ ] **5.2** Kiểm tra kết quả:
  - ✅ **Test 1**: API accessible (HTTP 400)
  - ✅ **Test 2**: Signature verification works (HTTP 401)

- [ ] **5.3** Nếu có lỗi, check Vercel logs:
  ```
  https://vercel.com/dashboard → Deployments → Latest → Logs
  ```

---

### Phase 6: Build & Test App

- [ ] **6.1** Build debug APK:
  ```powershell
  cd C:\APp\LeviLaunchroid-1.0.15
  .\gradlew assembleDebug
  ```

- [ ] **6.2** Install APK lên điện thoại:
  ```powershell
  adb install app\build\outputs\apk\debug\app-debug.apk
  ```

- [ ] **6.3** Test flow hoàn chỉnh:
  - [ ] Login vào app
  - [ ] Vào **Marketplace** → Click **GET LF COINS**
  - [ ] Click **WATCH AD** → Xem quảng cáo **HẾT**
  - [ ] Chờ 3-5 giây
  - [ ] Check Firebase: User `coin` có tăng lên **+10** không?

---

### Phase 7: Verify trong Firebase

- [ ] **7.1** Mở Firebase Console
  - Link: https://console.firebase.google.com/project/lastfom-launcher

- [ ] **7.2** Vào **Firestore Database**

- [ ] **7.3** Check collection **`users`**:
  ```json
  {
    "userId": "xxx",
    "coin": 10,  // ← Should increase by +10
    "lastRewardTimestamp": 1698480000000
  }
  ```

- [ ] **7.4** Check collection **`admob_transactions`**:
  ```json
  {
    "transactionId": "xxx",
    "userId": "xxx",
    "rewardAmount": 10,
    "timestamp": 1698480000000,
    "processedAt": 1698480000000
  }
  ```

- [ ] **7.5** (Optional) Check collection **`admob_analytics`**:
  ```json
  {
    "type": "reward_granted",
    "userId": "xxx",
    "rewardAmount": 10
  }
  ```

---

### Phase 8: Production Release

- [ ] **8.1** Update version:
  - File: `app/build.gradle`
  ```gradle
  versionCode 16
  versionName "1.0.16"
  ```

- [ ] **8.2** Build release bundle:
  ```powershell
  .\gradlew bundleRelease
  ```

- [ ] **8.3** Sign APK/AAB (if needed)

- [ ] **8.4** Upload lên Google Play Console

- [ ] **8.5** Test với beta testers trước khi release

---

## 🔧 Troubleshooting

| Vấn đề | Giải pháp |
|--------|-----------|
| Không nhận coin sau khi xem ads | 1. Check AdMob SSV URL đã config chưa<br>2. Check Vercel logs có error không<br>3. Chờ 10-30s rồi check lại Firebase |
| API trả lỗi 500 | Check `FIREBASE_SERVICE_ACCOUNT` trong Vercel env vars |
| API trả lỗi 401 | Normal! Signature chỉ valid khi từ AdMob gọi đến |
| Ads không hiện | 1. Check Ad Unit ID đúng chưa<br>2. Check AdMob account active chưa<br>3. Dùng Test ID để test UI flow |

---

## 📞 Support Links

- **AdMob Console**: https://apps.admob.com
- **Firebase Console**: https://console.firebase.google.com/project/lastfom-launcher
- **Vercel Dashboard**: https://vercel.com/dashboard
- **AdMob Help**: https://support.google.com/admob

---

## 🎉 Kết quả mong đợi

Sau khi hoàn thành checklist:

✅ User có thể xem quảng cáo trong app  
✅ Sau khi xem xong, nhận **10 LFCoins** tự động  
✅ Giới hạn **10 ads/ngày** được enforce  
✅ Coins được verify bởi server (không thể cheat)  
✅ Transaction được log đầy đủ trong Firebase  

---

**Thời gian setup dự kiến**: 20-30 phút  
**Độ khó**: ⭐⭐⭐☆☆ (Trung bình)

---

_Generated: October 28, 2025_
