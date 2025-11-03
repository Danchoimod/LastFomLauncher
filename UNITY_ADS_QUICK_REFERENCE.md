# 🚀 Quick Reference: Unity Ads

## ⚡ Current Setup

```java
Game ID: 5974445
Placement: Rewarded_Android
Test Mode: ON (true)
```

---

## 🎯 Important Files

### 1. `getCoin.java` - Ad implementation
```
Location: app/src/main/java/org/levimc/launcher/ui/fragment/getCoin.java
Purpose: Handles ad loading, display, and rewards
```

### 2. `AndroidManifest.xml` - Permissions
```
Location: app/src/main/AndroidManifest.xml
Permissions: INTERNET, ACCESS_NETWORK_STATE, ACCESS_WIFI_STATE, AD_ID
```

### 3. `build.gradle` - Dependencies
```
Location: app/build.gradle
Dependency: com.unity3d.ads:unity-ads:4.7.0
```

---

## ✅ Status: WORKING!

### What You See in Logs (NORMAL):
```
✓ Unity Ads initialized successfully
✓ Unity Ads loaded: Rewarded_Android
✓ sending view event to https://impact.applifier.com/events/v2/video/video_end
⚠️ Unity Ads was not able to get current network type [IGNORE THIS]
```

---

## 🔥 Before Production:

**MUST CHANGE in `getCoin.java` line 46:**
```java
private static final boolean TEST_MODE = false; // ⚠️ SET TO FALSE!
```

---

## 💰 Expected Revenue (Production):

| Region | eCPM | Revenue/1000 Views |
|--------|------|-------------------|
| 🇺🇸 US | $10-15 | $10-15 |
| 🇪🇺 EU | $8-12 | $8-12 |
| 🇯🇵 JP | $6-10 | $6-10 |
| 🌏 Asia | $3-7 | $3-7 |
| 🌍 Other | $2-5 | $2-5 |

**Example**: 10,000 daily ad views in US = $100-150/day

---

## 📞 Quick Links

- **Dashboard**: https://dashboard.unity3d.com/
- **Docs**: https://docs.unity.com/ads/
- **Support**: https://support.unity.com/

---

## 🐛 Troubleshooting

| Problem | Solution |
|---------|----------|
| "Ad not ready" | Wait 5 sec, check internet |
| "Init failed" | Verify Game ID: 5974445 |
| No reward | Check Firestore rules |
| "Load failed" | Check internet, retry |

---

**Status**: ✅ READY TO TEST  
**Next**: Build app and test "WATCH AD" button!

