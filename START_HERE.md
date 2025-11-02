# 🎯 Quick Start - AdMob SSV Integration

## 📁 Files trong thư mục này

| File | Mục đích |
|------|----------|
| **CHECKLIST.md** | ✅ Checklist từng bước để setup (BẮT ĐẦU TỪ ĐÂY) |
| **ADMOB_SSV_PRODUCTION_SETUP.md** | 📖 Hướng dẫn chi tiết đầy đủ |
| **quick-setup-admob.ps1** | 🚀 Script tự động setup nhanh |
| **test-admob-api.ps1** | 🧪 Script test API endpoint |
| **admob-config.ini** | ⚙️ File config mẫu |

---

## 🚀 Bắt đầu nhanh (3 bước)

### **Bước 1: Chạy Quick Setup Script**

```powershell
cd C:\APp\LeviLaunchroid-1.0.15
.\quick-setup-admob.ps1
```

Script sẽ tự động:
- Hỏi Ad Unit ID và Vercel URL
- Update file `getCoin.java`
- Tạo file config

---

### **Bước 2: Cấu hình AdMob Console (5 phút)**

1. Vào https://apps.admob.com
2. **Settings** → **Account** → **Apps** → Chọn app của bạn
3. Thêm **Server-Side Verification URL**:
   ```
   https://your-vercel-project.vercel.app/api/verify-reward
   ```
4. Click **Save** ✅

---

### **Bước 3: Test & Deploy**

```powershell
# Test API
.\test-admob-api.ps1 -VercelUrl "https://your-vercel-project.vercel.app"

# Build app
.\gradlew assembleDebug

# Install & test
adb install app\build\outputs\apk\debug\app-debug.apk
```

---

## 📖 Chi tiết đầy đủ

Nếu cần hướng dẫn chi tiết, xem:
- **CHECKLIST.md** - Checklist từng bước
- **ADMOB_SSV_PRODUCTION_SETUP.md** - Hướng dẫn đầy đủ + troubleshooting

---

## 🎯 Kết quả mong đợi

✅ User xem ads → Nhận **10 LFCoins**  
✅ Giới hạn **10 ads/ngày**  
✅ Coins được verify bởi server (không thể cheat)  
✅ Transaction log trong Firebase  

---

## 🔗 Links quan trọng

- **AdMob Console**: https://apps.admob.com
- **Firebase Console**: https://console.firebase.google.com/project/lastfom-launcher
- **Vercel Dashboard**: https://vercel.com/dashboard

---

## 🆘 Cần giúp đỡ?

1. Check **CHECKLIST.md** → Phase 8: Troubleshooting
2. Check Vercel logs: https://vercel.com/dashboard → Deployments → Logs
3. Check Firebase Console: https://console.firebase.google.com/project/lastfom-launcher

---

_Last updated: October 28, 2025_
