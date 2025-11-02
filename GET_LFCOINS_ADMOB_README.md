# Get LFCoins với AdMob - Hướng dẫn

## Tính năng đã thêm

1. **Fragment Get LFCoins** - UI giống hình mẫu
2. **Google AdMob Rewarded Ads** - Xem quảng cáo nhận coin
3. **Giới hạn 10 quảng cáo/ngày** - Reset mỗi ngày
4. **Daily Reward** - 3 coins miễn phí mỗi ngày
5. **Navigation từ Marketplace** - Click nút "+" để vào Get Coins

## Files đã thay đổi

### 1. build.gradle
- Thêm dependency: `com.google.android.gms:play-services-ads:23.5.0`

### 2. AndroidManifest.xml
- Thêm AdMob App ID: `ca-app-pub-8177702634836557~4752957203`

### 3. fragment_get_coin.xml
- UI hoàn chỉnh với:
  - Header "GET LFCOINS"
  - Mô tả về coins
  - Info card hiển thị số lượt xem còn lại
  - 4 cards: Daily (3 coins) + 3x Video Ads (5 coins mỗi cái)
  - Button "GET →"

### 4. getCoin.java
- Logic AdMob Rewarded Ads
- Tracking số lượt xem quảng cáo (max 10/ngày)
- Daily reward system
- Firebase integration để cộng coins
- Auto reset mỗi ngày

### 5. Marketplace.java
- Thêm click listener cho nút "+" (addLfCoinText)
- Navigate sang getCoin fragment

## Cách hoạt động

### Daily Reward
- User click vào card "Daily" (3 coins)
- Click button "GET →"
- System check xem hôm nay đã claim chưa
- Nếu chưa: +3 coins vào Firestore và mark là đã claim
- Reset vào 00:00 ngày hôm sau

### Rewarded Ads
- User click vào 1 trong 3 cards "Video Upload Reward" (5 coins)
- Click button "GET →"
- System check:
  - Đã xem đủ 10 quảng cáo hôm nay chưa?
  - Quảng cáo đã load chưa?
  - User đã login chưa?
- Hiển thị quảng cáo
- Sau khi xem xong: +10 coins vào Firestore
- Increment counter (max 10/ngày)
- Reset vào 00:00 ngày hôm sau

### Tracking & Limits
**SharedPreferences keys:**
- `ad_watch_date`: ngày cuối cùng xem ads (format: yyyy-MM-dd)
- `ad_watch_count`: số lượt đã xem trong ngày
- `last_daily_reward`: ngày claim daily reward cuối cùng

**Logic reset:**
- Mỗi khi load fragment, check ngày hiện tại
- Nếu khác ngày trong SharedPreferences → reset counter về 0

## Ad Unit IDs

### Test Ads (đang dùng)
```java
AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
```

### Production Ads (cần thay thế)
1. Vào [AdMob Console](https://apps.admob.com/)
2. Tạo Ad Unit mới cho "Rewarded Ad"
3. Copy Ad Unit ID
4. Thay thế trong `getCoin.java`:
```java
private static final String AD_UNIT_ID = "ca-app-pub-8177702634836557/XXXXXXXX";
```

**⚠️ LƯU Ý:** Test Ad ID chỉ dùng để test, KHÔNG được dùng trên production!

## Cấu hình AdMob Console

### Bước 1: Tạo App
1. Đăng nhập [AdMob](https://apps.admob.com/)
2. Apps → Add App
3. Chọn platform: Android
4. Enter app name: "LF Launcher"
5. Copy App ID: `ca-app-pub-8177702634836557~4752957203` (đã có)

### Bước 2: Tạo Ad Unit
1. Vào app vừa tạo
2. Ad Units → Add Ad Unit
3. Chọn format: **Rewarded**
4. Enter ad unit name: "Get Coins Reward"
5. Reward settings:
   - Reward amount: 10
   - Reward item: Coins
6. Create Ad Unit
7. Copy Ad Unit ID → paste vào code

### Bước 3: Test Ads
- Để test, dùng Test Device
- Trong code, thêm test device ID:
```java
MobileAds.initialize(requireContext(), initializationStatus -> {
    RequestConfiguration config = new RequestConfiguration.Builder()
        .setTestDeviceIds(Arrays.asList("YOUR_TEST_DEVICE_ID"))
        .build();
    MobileAds.setRequestConfiguration(config);
});
```

## UI Cards Explained

```
┌─────────────────────┬─────────────────────┐
│   Daily Reward      │   Video Ad 1        │
│      🪙 3           │      🪙 5           │
│     (Free)          │   (Watch Ad)        │
├─────────────────────┼─────────────────────┤
│   Video Ad 2        │   Video Ad 3        │
│      🪙 5           │      🪙 5           │
│   (Watch Ad)        │   (Watch Ad)        │
└─────────────────────┴─────────────────────┘
```

- **Daily Card**: Màu vàng viền, claim 1 lần/ngày, không cần xem ads
- **Video Cards**: 3 cards giống nhau, mỗi cái là 1 slot để xem ads
- **Selection**: Card được chọn sẽ có elevation cao hơn
- **Disabled state**: Cards bị disable sẽ có opacity 0.5

## Coins Flow

```
User → Select Card → Click GET
         ↓
    Is Daily Card?
    ├─ Yes → Check claimed today?
    │         ├─ Yes → Show toast "Already claimed"
    │         └─ No → Grant 3 coins → Mark claimed
    │
    └─ No → Check ads limit?
              ├─ >= 10 → Show toast "Daily limit reached"
              └─ < 10 → Show Ad → On Complete:
                        ├─ Grant 10 coins
                        ├─ Increment counter
                        └─ Update UI
```

## Firebase Firestore Structure

```json
{
  "users": {
    "userId": {
      "coins": 100,
      "username": "...",
      "email": "..."
    }
  }
}
```

Khi grant coins:
```java
// Get current coins
long currentCoins = doc.getLong("coins") ?? 0;

// Update with new amount
updates.put("coins", currentCoins + rewardAmount);
db.collection("users").document(userId).update(updates);
```

## Test Scenarios

### Scenario 1: Daily Reward
1. Mở app lần đầu trong ngày
2. Vào Get Coins
3. Click card "Daily" (3 coins)
4. Click "GET →"
5. ✅ Nhận 3 coins, quay về Marketplace
6. Vào lại Get Coins
7. Click card "Daily" lần nữa
8. ❌ Toast: "Daily reward already claimed today!"

### Scenario 2: Watch Ads
1. Vào Get Coins
2. Click card "Video Upload Reward"
3. Click "GET →"
4. ✅ Hiện quảng cáo
5. Xem hết quảng cáo
6. ✅ Nhận 10 coins
7. Lặp lại 10 lần
8. ❌ Toast: "Daily limit reached"

### Scenario 3: Next Day Reset
1. Đợi sang ngày mới (hoặc change system date)
2. Vào Get Coins
3. ✅ Counter reset về 0
4. ✅ Daily reward available lại

## Troubleshooting

### Lỗi: "No view found for id fragment_container"
**Nguyên nhân:** Sai container ID khi navigate fragment

**Giải pháp:**
- Marketplace chạy trong BedrockContainer fragment
- Container ID đúng là `R.id.bedrockContent` (không phải `fragment_container`)
- Đã fix trong code:
```java
getParentFragmentManager().beginTransaction()
    .replace(R.id.bedrockContent, getCoinFragment)
    .addToBackStack(null)
    .commit();
```

### Lỗi: "Ad failed to load"
**Nguyên nhân:** 
- Không có internet
- Ad inventory trống
- Sai Ad Unit ID

**Giải pháp:**
- Check internet connection
- Dùng Test Ad ID để test
- Verify Ad Unit ID trong AdMob Console

### Lỗi: "Please login first"
**Nguyên nhân:** User chưa login, không có userId

**Giải pháp:**
- Đảm bảo user đã login trước khi vào Get Coins
- Check SharedPreferences có "user_id" không

### Lỗi: "Failed to grant coins"
**Nguyên nhân:** Firestore update fail

**Giải pháp:**
- Check Firestore rules
- Verify userId exists in Firestore
- Check internet connection

### Ads không hiện trên production
**Nguyên nhân:** Đang dùng Test Ad ID

**Giải pháp:**
- Thay bằng real Ad Unit ID
- Remove test device configuration
- Deploy lên Google Play (ads thường không hiện khi test APK)

## Production Checklist

- [ ] Thay Test Ad Unit ID bằng Real Ad Unit ID
- [ ] Remove test device configuration code
- [ ] Test trên real device với real ads
- [ ] Verify Firestore rules cho production
- [ ] Test daily reset logic
- [ ] Test counter persistence
- [ ] Test navigation back to Marketplace
- [ ] Verify AdMob account payment settings

## Notes

- **Test ads**: Dùng Google test ad units, không tính revenue
- **Real ads**: Cần app được approve trên Google Play
- **Revenue**: Mất 2-3 ngày để ads bắt đầu fill rate tốt
- **Policy**: Không được click ads của mình để test!
- **Limits**: 10 ads/day là hợp lý, tránh spam

## Next Steps

1. ✅ Build và test với Test Ads
2. ✅ Verify daily reset logic
3. ✅ Test navigation flow
4. 📝 Tạo real Ad Unit ID trong AdMob
5. 📝 Replace Test ID bằng Real ID
6. 📝 Test trên production
7. 📝 Monitor revenue trong AdMob Console

