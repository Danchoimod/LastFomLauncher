# AdMob Server-Side Verification Setup Guide

## 📦 Files Created

### 1. VERCEL_API_verify-reward.js
**Mục đích:** API endpoint để nhận callback từ Google AdMob và grant coins cho user

**Copy file này vào:** `api/admob/verify-reward.js` trong project Vercel của bạn

## 🔧 Setup Steps

### Step 1: Copy API File
```bash
# Trong project website của bạn
mkdir -p api/admob
cp VERCEL_API_verify-reward.js api/admob/verify-reward.js
```

### Step 2: Install Dependencies
```bash
npm install firebase-admin
```

### Step 3: Setup Environment Variables trong Vercel

Vào Vercel Dashboard → Your Project → Settings → Environment Variables

Thêm các biến sau:

```env
FIREBASE_PROJECT_ID=lastfom-launcher
FIREBASE_CLIENT_EMAIL=firebase-adminsdk-xxxxx@lastfom-launcher.iam.gserviceaccount.com
FIREBASE_PRIVATE_KEY="-----BEGIN PRIVATE KEY-----\nMIIE...\n-----END PRIVATE KEY-----\n"
```

**Lấy credentials:**
1. Vào [Firebase Console](https://console.firebase.google.com/)
2. Chọn project "lastfom-launcher"
3. Settings → Service Accounts
4. Click "Generate New Private Key"
5. Mở file JSON vừa download
6. Copy các giá trị vào Vercel Environment Variables

### Step 4: Deploy lên Vercel
```bash
vercel --prod
```

### Step 5: Configure AdMob SSV

1. Vào [AdMob Console](https://apps.admob.com/)
2. Chọn app của bạn
3. Vào Ad Units → Chọn Rewarded Ad Unit
4. Settings → Server-Side Verification

**Nhập URL:**
```
https://your-domain.vercel.app/api/admob/verify-reward
```

Hoặc nếu dùng subdomain:
```
https://lflauncher.vercel.app/api/admob/verify-reward
```

**Query Parameters Template:**
```
ad_network=[AD_NETWORK]&ad_unit=[AD_UNIT]&reward_amount=[REWARD_AMOUNT]&reward_item=[REWARD_ITEM]&timestamp=[TIMESTAMP]&transaction_id=[TRANSACTION_ID]&user_id=[USER_ID]&signature=[SIGNATURE]&key_id=[KEY_ID]&custom_data=[CUSTOM_DATA]
```

## 🔐 Security Features

### 1. Signature Verification
API tự động verify signature từ Google AdMob để đảm bảo request hợp lệ:
- Fetch public keys từ Google: `https://www.gstatic.com/admob/reward/verifier-keys.json`
- Cache keys 24 giờ để tối ưu performance
- Verify mọi request với SHA256 signature

### 2. Transaction Deduplication
Mỗi transaction_id chỉ được xử lý 1 lần:
- Lưu vào collection `admob_transactions`
- Prevent double-rewarding nếu Google gửi callback nhiều lần

### 3. Firebase Transaction
Dùng Firestore transaction để đảm bảo atomic operations:
- Update user coins
- Save transaction record
- Rollback nếu có lỗi

## 📊 Database Structure

### Collection: `users`
```javascript
{
  "userId": "firebase_auth_uid",
  "coins": 150,
  "lastRewardTimestamp": 1698765432000,
  "email": "user@example.com"
}
```

### Collection: `admob_transactions`
```javascript
{
  "transactionId": "unique_transaction_id_from_google",
  "userId": "firebase_auth_uid",
  "rewardAmount": 10,
  "adNetwork": "5450213213286189855",
  "adUnit": "ca-app-pub-8177702634836557/XXXXXXXX",
  "timestamp": 1698765432000,
  "processedAt": 1698765432100,
  "customData": "user_id=xxx&reward_amount=10"
}
```

### Collection: `admob_analytics` (Optional)
```javascript
{
  "type": "reward_granted",
  "userId": "firebase_auth_uid",
  "rewardAmount": 10,
  "transactionId": "unique_transaction_id",
  "timestamp": 1698765432000
}
```

## 🧪 Testing

### Test với cURL:
```bash
curl -X GET "https://your-domain.vercel.app/api/admob/verify-reward?ad_network=5450213213286189855&ad_unit=ca-app-pub-123456789&reward_amount=10&reward_item=coins&timestamp=1698765432&transaction_id=test_123&user_id=test_user_id&signature=test_signature&key_id=1234567"
```

**Expected Response:**
```json
{
  "error": "Invalid signature"
}
```

Đây là response đúng vì test signature không hợp lệ. Chỉ Google AdMob mới có signature đúng.

### Test với Google Test Ads:
1. Build và chạy app Android
2. Vào Get Coins screen
3. Click "WATCH AD"
4. Xem test ad
5. Check Vercel logs:
```bash
vercel logs
```

Bạn sẽ thấy:
```
SSV Callback received: { ad_network: '...', user_id: '...' }
✓ Signature verified successfully
✓ Granted 10 coins to user xxx. New balance: 150
```

## 🐛 Troubleshooting

### Lỗi: "Invalid signature"
**Nguyên nhân:** Signature không hợp lệ
**Giải pháp:**
- Đảm bảo SSV URL trong AdMob Console đúng
- Check logs xem query string có đúng format không
- Verify public keys được fetch thành công

### Lỗi: "User not found"
**Nguyên nhân:** userId không tồn tại trong Firestore
**Giải pháp:**
- Đảm bảo user đã login và có document trong collection `users`
- Check userId trong Android app khớp với Firestore

### Lỗi: "Missing required parameters"
**Nguyên nhân:** Thiếu parameters trong request
**Giải pháp:**
- Check SSV URL template trong AdMob Console
- Đảm bảo có đủ parameters: `ad_network`, `ad_unit`, `reward_amount`, etc.

### Lỗi: "Failed to fetch verification keys"
**Nguyên nhân:** Không fetch được public keys từ Google
**Giải pháp:**
- Check internet connection của Vercel
- Retry sau vài phút

## 📈 Monitoring

### View Logs trong Vercel:
```bash
vercel logs --follow
```

### View Transactions trong Firebase Console:
1. Vào Firestore Database
2. Mở collection `admob_transactions`
3. Xem các transactions đã xử lý

### View Analytics:
```javascript
// Query trong Firebase Console
db.collection('admob_analytics')
  .where('type', '==', 'reward_granted')
  .orderBy('timestamp', 'desc')
  .limit(100)
```

## 🔄 Update Process

Khi update code:
1. Sửa file `api/admob/verify-reward.js`
2. Commit và push lên Git
3. Vercel tự động deploy
4. Test lại với test ads

## 🚀 Production Checklist

- [ ] Copy API file vào project Vercel
- [ ] Install firebase-admin dependency
- [ ] Setup environment variables
- [ ] Deploy lên Vercel
- [ ] Lấy production URL
- [ ] Configure SSV trong AdMob Console
- [ ] Thay Test Ad Unit ID bằng Real Ad Unit ID trong Android app
- [ ] Test với real ads
- [ ] Monitor logs và transactions
- [ ] Setup alerts cho errors

## 💡 Best Practices

1. **Always verify signature** - Đừng bao giờ skip bước verify signature
2. **Use transactions** - Dùng Firestore transactions để đảm bảo data consistency
3. **Deduplicate transactions** - Check transaction_id để tránh double-rewarding
4. **Log everything** - Log mọi request để debug
5. **Monitor errors** - Setup alerts cho errors trong production
6. **Cache public keys** - Cache keys để giảm latency
7. **Rate limiting** - Consider thêm rate limiting nếu cần

## 📚 References

- [AdMob SSV Documentation](https://developers.google.com/admob/android/rewarded-video-ssv)
- [Vercel Serverless Functions](https://vercel.com/docs/functions)
- [Firebase Admin SDK](https://firebase.google.com/docs/admin/setup)

