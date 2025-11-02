# 🚀 AdMob Server-Side Verification (SSV) - Production Setup Guide

## ✅ Tổng quan
Hệ thống AdMob SSV cho phép user xem quảng cáo và nhận **10 LFCoins/video**, với giới hạn **10 videos/ngày**.

### Flow hoạt động:
```
User xem ads → AdMob verify → Gọi Vercel API → API verify signature → Cộng coins vào Firebase → Done ✅
```

---

## 📝 Các bước setup

### **BƯỚC 1: Lấy Real Ad Unit ID từ AdMob Console**

1. Đăng nhập vào AdMob Console: https://apps.admob.com
2. Chọn app **LastFom Launcher** (package: `org.levimc.launcher`)
3. Vào **Ad units** → Tìm **Rewarded Ad Unit**
4. Copy **Ad unit ID** (format: `ca-app-pub-XXXXXXXXXX/YYYYYYYYYY`)
5. Thay vào file `getCoin.java`:

```java
// Thay dòng này:
private static final String AD_UNIT_ID = "ca-app-pub-8177702634836557/XXXXXXXX";

// Bằng Ad Unit ID thật của bạn:
private static final String AD_UNIT_ID = "ca-app-pub-8177702634836557/1234567890"; // ví dụ
```

---

### **BƯỚC 2: Cấu hình Server-Side Verification URL trong AdMob Console**

1. Vào AdMob Console: https://apps.admob.com
2. **Settings** (⚙️) → **Account** → **Account information**
3. Kéo xuống phần **"Apps"** → Click vào app của bạn
4. Tìm phần **"Rewarded ads"** hoặc **"Server-side verification"**
5. Click **"Add URL"** hoặc **"Configure"**

6. Nhập URL callback của Vercel API:

```
https://your-vercel-project.vercel.app/api/verify-reward
```

**⚠️ Quan trọng**: Thay `your-vercel-project` bằng domain Vercel thật của bạn.

Ví dụ:
```
https://lflauncher-api.vercel.app/api/verify-reward
https://lastfom-launcher.vercel.app/api/verify-reward
```

7. Click **Save** ✅

---

### **BƯỚC 3: Verify Firebase Service Account trên Vercel**

1. Đăng nhập vào Vercel Dashboard: https://vercel.com
2. Vào project của bạn → **Settings** → **Environment Variables**
3. Kiểm tra biến `FIREBASE_SERVICE_ACCOUNT` đã được set chưa

#### Nếu chưa có, tạo Service Account:

**3.1. Tạo Service Account Key từ Firebase Console:**

```bash
# Vào Firebase Console:
https://console.firebase.google.com/project/lastfom-launcher/settings/serviceaccounts/adminsdk

# Click "Generate new private key"
# Download file JSON về máy (ví dụ: serviceAccountKey.json)
```

**3.2. Convert JSON sang Base64:**

```bash
# Windows PowerShell:
[Convert]::ToBase64String([IO.File]::ReadAllBytes("path\to\serviceAccountKey.json"))

# Mac/Linux:
base64 -i serviceAccountKey.json
```

**3.3. Add vào Vercel Environment Variables:**

- **Name**: `FIREBASE_SERVICE_ACCOUNT`
- **Value**: `<Base64 string from step 3.2>`
- **Environment**: Production, Preview, Development (chọn tất cả)
- Click **Save** ✅

**3.4. Redeploy Vercel project:**

```bash
# Vào Vercel Dashboard → Deployments → Click "Redeploy"
# Hoặc push code lên git để trigger auto-deploy
```

---

### **BƯỚC 4: Test API endpoint trước khi deploy app**

Mở browser và test API:

```
https://your-vercel-project.vercel.app/api/verify-reward?test=1
```

**Kết quả mong đợi:**

```json
{
  "success": false,
  "error": "Missing required parameters"
}
```

✅ Nếu thấy response như trên → API đã hoạt động!

❌ Nếu báo lỗi khác → Check logs trong Vercel Dashboard

---

### **BƯỚC 5: Build & Deploy Android App**

1. **Update version trong `build.gradle` (optional):**

```gradle
versionCode 16
versionName "1.0.16"
```

2. **Build APK/AAB:**

```bash
# Debug build (for testing):
./gradlew assembleDebug

# Release build (for production):
./gradlew bundleRelease
```

3. **Install và test:**

```bash
# Install debug APK:
adb install app/build/outputs/apk/debug/app-debug.apk

# Hoặc install từ Android Studio
```

4. **Test flow:**

- Login vào app
- Vào **Marketplace** → Click **GET LF COINS**
- Click **WATCH AD** → Xem quảng cáo hết
- Kiểm tra Firebase Console → Collection `users` → Xem field `coin` có tăng không

---

### **BƯỚC 6: Verify trong Firebase Console**

1. Vào Firebase Console: https://console.firebase.google.com/project/lastfom-launcher
2. Vào **Firestore Database**
3. Kiểm tra 3 collections:

#### **a) Collection `users`:**
```json
{
  "userId": "xxx",
  "coin": 10,  // ← Should increase by 10 after watching ad
  "lastRewardTimestamp": 1698480000000
}
```

#### **b) Collection `admob_transactions`:**
```json
{
  "transactionId": "xxx",
  "userId": "xxx",
  "rewardAmount": 10,
  "adNetwork": "5450213213286189855",
  "adUnit": "ca-app-pub-xxx/xxx",
  "timestamp": 1698480000000,
  "processedAt": 1698480000000
}
```

#### **c) Collection `admob_analytics` (optional):**
```json
{
  "type": "reward_granted",
  "userId": "xxx",
  "rewardAmount": 10,
  "transactionId": "xxx",
  "timestamp": 1698480000000
}
```

---

## 🔍 Troubleshooting

### ❌ Không nhận được coin sau khi xem ads

**Nguyên nhân có thể:**

1. **SSV URL chưa được config trong AdMob Console**
   - ✅ Fix: Làm lại **BƯỚC 2**

2. **Firebase Service Account không hợp lệ**
   - ✅ Fix: Làm lại **BƯỚC 3**
   - Check Vercel logs: `https://vercel.com/<project>/deployments/<deployment>/logs`

3. **User ID không đúng**
   - ✅ Fix: Check SharedPreferences trong app, đảm bảo `user_id` được lưu đúng

4. **Signature verification failed**
   - ✅ Fix: AdMob cần 24-48h để sync SSV URL. Đợi và test lại.

5. **Test Ad Unit ID vẫn đang dùng**
   - ✅ Fix: Thay bằng Real Ad Unit ID (BƯỚC 1)

---

### 📊 Check Vercel Logs

```bash
# Xem logs real-time:
vercel logs <project-name> --follow

# Hoặc vào Vercel Dashboard:
https://vercel.com/<username>/<project>/deployments/<deployment-id>/logs
```

**Logs mong đợi khi xem ads thành công:**

```
SSV Callback received: {
  ad_network: '5450213213286189855',
  ad_unit: 'ca-app-pub-xxx/xxx',
  user_id: 'FwmrB87VvhZaHhJYdBtqF8q8skv2',
  reward_amount: '10',
  transaction_id: 'xxx',
  timestamp: '2024-10-28T10:30:00Z'
}
✓ Signature verified successfully
✓ Granted 10 coins to user FwmrB87VvhZaHhJYdBtqF8q8skv2. New balance: 50
```

---

### 📱 Test với Test Ads (không tốn tiền)

Trong development, dùng **Test Ad Unit ID** để test:

```java
// Test Rewarded Ad Unit ID
private static final String AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917";
```

**⚠️ Lưu ý**: Test ads **KHÔNG** trigger SSV callback, chỉ dùng để test UI/UX flow.

---

## 🎯 Checklist trước khi release Production

- [ ] **BƯỚC 1**: Thay Test Ad Unit ID → Real Ad Unit ID ✅
- [ ] **BƯỚC 2**: Config SSV URL trong AdMob Console ✅
- [ ] **BƯỚC 3**: Verify Firebase Service Account trên Vercel ✅
- [ ] **BƯỚC 4**: Test API endpoint hoạt động ✅
- [ ] **BƯỚC 5**: Build release APK/AAB ✅
- [ ] **BƯỚC 6**: Test flow end-to-end với real ads ✅
- [ ] **Bonus**: Test với nhiều user khác nhau ✅

---

## 📞 Support

Nếu gặp vấn đề, check:

1. **Vercel Logs**: https://vercel.com/dashboard
2. **Firebase Console**: https://console.firebase.google.com/project/lastfom-launcher
3. **AdMob Console**: https://apps.admob.com
4. **Android Logcat**: `adb logcat -s getCoin`

---

## 🎉 Kết luận

Sau khi setup xong, user có thể:

- ✅ Xem tối đa **10 ads/ngày**
- ✅ Nhận **10 LFCoins/video**
- ✅ Coins được verify và cộng tự động bởi server
- ✅ Không thể cheat (signature verification)
- ✅ Transaction được log đầy đủ trong Firebase

**Chúc bạn thành công! 🚀**
