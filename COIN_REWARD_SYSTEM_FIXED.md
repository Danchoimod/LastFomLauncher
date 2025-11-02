# 💰 Coin Reward System - Fixed!

## ❌ Vấn Đề Trước Đây
- User claim daily reward hoặc xem quảng cáo
- `lastDailyReward` được cập nhật trong Firestore
- **NHƯNG coins KHÔNG tăng!**

## ✅ Đã Fix

### 1. Daily Reward (Phần thưởng hàng ngày)
```java
// Trước (CHỈ ghi nhận, KHÔNG cộng coins):
updates.put("lastDailyReward", today);

// SAU (Ghi nhận VÀ cộng coins):
updates.put("lastDailyReward", today);
updates.put("coin", FieldValue.increment(DAILY_REWARD_COINS)); // +3 coins
```

### 2. Ad Reward (Xem quảng cáo)
```java
// Thêm method mới: grantAdRewardCoins()
// Sử dụng FieldValue.increment() để cộng coins
updates.put("coin", FieldValue.increment(COINS_PER_AD)); // +10 coins
```

## 🔧 Cách Hoạt Động

### Khi User Claim Daily Reward:
1. Kiểm tra xem đã claim hôm nay chưa
2. Nếu chưa → Gọi `claimDailyReward()`
3. Firestore cập nhật:
   - `lastDailyReward = "2025-10-28"`
   - `coin += 3` (tăng 3 coins)
4. UI hiển thị: "Claimed 3 coins successfully! ✓"

### Khi User Xem Quảng Cáo:
1. User nhấn "WATCH AD"
2. Hiển thị quảng cáo
3. Khi xem xong → Gọi `onUserEarnedReward()`
4. Gọi `grantAdRewardCoins()`
5. Firestore cập nhật:
   - `adsWatchedToday += 1`
   - `lastAdDate = "2025-10-28"`
   - `coin += 10` (tăng 10 coins)
6. UI hiển thị: "+10 coins earned! ✓"

## 📊 Firestore Structure

```
users/{userId}
├── coin: 113                      // Total coins (auto-incremented)
├── lastDailyReward: "2025-10-28"  // Last claim date
├── lastAdDate: "2025-10-28"       // Last ad watch date
├── adsWatchedToday: 5             // Ads watched today
└── userId: "1415401572397224028"
```

## 🔑 Key Changes

### File: `getCoin.java`

#### 1. Import FieldValue
```java
import com.google.firebase.firestore.FieldValue;
```

#### 2. Updated `claimDailyReward()`
```java
private void claimDailyReward() {
    // ...
    updates.put("coin", FieldValue.increment(DAILY_REWARD_COINS)); // ✅ NEW
    db.collection("users").document(userId)
        .set(updates, SetOptions.merge())
        .addOnSuccessListener(aVoid -> {
            Toast.makeText(requireContext(), 
                "Claimed " + DAILY_REWARD_COINS + " coins successfully! ✓",
                Toast.LENGTH_SHORT).show();
        });
}
```

#### 3. Added `grantAdRewardCoins()`
```java
private void grantAdRewardCoins() {
    Map<String, Object> updates = new HashMap<>();
    updates.put("coin", FieldValue.increment(COINS_PER_AD));
    
    db.collection("users").document(userId)
        .update(updates)
        .addOnSuccessListener(aVoid -> {
            Log.d(TAG, "Ad reward granted: +" + COINS_PER_AD + " coins");
        })
        .addOnFailureListener(e -> {
            // Fallback: Try with set+merge if document doesn't exist
            db.collection("users").document(userId)
                .set(updates, SetOptions.merge())
                .addOnSuccessListener(aVoid2 -> {
                    Log.d(TAG, "Ad reward granted (via set): +" + COINS_PER_AD + " coins");
                });
        });
}
```

#### 4. Updated `onUserEarnedReward()`
```java
@Override
public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
    adsWatchedToday++;
    saveAdWatchCountToFirestore();
    grantAdRewardCoins(); // ✅ NEW - Cộng coins trực tiếp
    updateUI();
    
    Toast.makeText(requireContext(),
        "+" + COINS_PER_AD + " coins earned! ✓",
        Toast.LENGTH_SHORT).show();
}
```

## 🔒 Firestore Security Rules

**⚠️ QUAN TRỌNG**: Phải cập nhật rules để cho phép increment field `coin`!

### Cách 1: Testing (Cho phép tất cả)
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if true;
    }
  }
}
```

### Cách 2: Bảo mật (Khuyến nghị)
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read: if true;
      allow create: if request.resource.data.keys().hasAll(['lastAdDate', 'adsWatchedToday']);
      allow update: if request.resource.data.diff(resource.data).affectedKeys()
                       .hasOnly(['lastAdDate', 'adsWatchedToday', 'lastDailyReward', 'userId', 'coin']);
      //                                                                                        ^^^^
      //                                                                        CHÚ Ý: Thêm 'coin' vào đây!
    }
    match /marketplace/{itemId} {
      allow read: if true;
      allow write: if true;
    }
  }
}
```

## 🧪 Test

### Test Case 1: Claim Daily Reward
1. Mở app, vào màn hình Get Coins
2. Nhấn "CLAIM" để nhận phần thưởng hàng ngày
3. ✅ Thấy toast: "Claimed 3 coins successfully! ✓"
4. Kiểm tra Firestore:
   - Field `coin` tăng lên 3
   - Field `lastDailyReward` = ngày hôm nay
5. Thử claim lại → Hiển thị: "Daily reward already claimed today!"

### Test Case 2: Watch Ad
1. Nhấn "WATCH AD →"
2. Xem quảng cáo đến hết
3. ✅ Thấy toast: "+10 coins earned! ✓"
4. Kiểm tra Firestore:
   - Field `coin` tăng lên 10
   - Field `adsWatchedToday` tăng lên 1
5. Xem tối đa 10 quảng cáo/ngày

### Test Case 3: Reset sau khi xóa data
1. Vào Settings → Apps → LeviLaunchroid → Clear Data
2. Mở lại app và login
3. Vào Get Coins
4. ✅ Số coins vẫn đúng (load từ Firestore)
5. ✅ Trạng thái daily reward vẫn đúng

## 📝 Logs để Debug

```bash
adb logcat | grep -E "(getCoin|coin)"
```

**Logs thành công**:
```
getCoin: Daily reward claimed successfully! +3 coins
getCoin: Ad reward granted: +10 coins
getCoin: Ad watch count saved: 1
```

**Logs lỗi** (nếu có):
```
getCoin: Failed to claim daily reward: PERMISSION_DENIED
→ Cần cập nhật Firestore Security Rules

getCoin: Failed to grant ad reward: Document not found
→ App sẽ tự động retry với set+merge
```

## 🎯 Summary

| Action | Coins | Firestore Update |
|--------|-------|------------------|
| Claim Daily Reward | +3 | `coin += 3`, `lastDailyReward = today` |
| Watch Ad | +10 | `coin += 10`, `adsWatchedToday += 1` |
| Max Ads/Day | 10 ads | `adsWatchedToday` max = 10 |

**Total max coins/day**: 3 (daily) + 10×10 (ads) = **103 coins/day**

## 📚 Files Changed
- ✅ `getCoin.java` - Added FieldValue.increment for coins
- ✅ `fix-firestore-permission.bat` - Updated rules to include 'coin' field
- ✅ `FIRESTORE_SECURITY_RULES_FIX.md` - Updated rules documentation

## ⚡ Next Steps

1. **Cập nhật Firestore Rules** (quan trọng nhất!)
   - Vào Firebase Console
   - Copy rules từ `fix-firestore-permission.bat`
   - Publish

2. **Test app**
   - Claim daily reward
   - Watch ads
   - Check coins in Firestore

3. **Monitor logs**
   - `adb logcat | grep getCoin`
   - Kiểm tra lỗi (nếu có)

---

**🎉 DONE! Coins giờ sẽ tự động tăng khi user claim reward hoặc xem quảng cáo!**

