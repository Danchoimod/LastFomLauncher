# Unity Ads Setup Guide

## ✅ Migration Complete: AdMob → Unity Ads

Your app has been successfully migrated from AdMob to Unity Ads!

---

## 📋 What Changed

### 1. **Dependencies Updated** (`app/build.gradle`)
- ✅ Removed Google Mobile Ads SDK (AdMob)
- ✅ Added Unity Ads SDK 4.7.0
- ✅ Updated Java compatibility to version 1.8+ (required for Unity Ads)

### 2. **AndroidManifest.xml Updated**
- ✅ Removed AdMob App ID meta-data
- ✅ Added `com.google.android.gms.permission.AD_ID` permission for Android 13+ support

### 3. **getCoin.java Rewritten**
- ✅ Replaced all AdMob code with Unity Ads SDK
- ✅ Implemented Unity Ads listeners:
  - `IUnityAdsInitializationListener` - SDK initialization
  - `IUnityAdsLoadListener` - Ad loading
  - `IUnityAdsShowListener` - Ad display and rewards
- ✅ Updated reward flow to use Unity Ads completion states

---

## 🚀 Setup Instructions

### Step 1: Create Unity Account & Project

1. Go to **Unity Dashboard**: https://dashboard.unity3d.com/
2. Sign up or log in with your account
3. Click **"Create"** → **"New Project"**
4. Name your project (e.g., "LastFom Launcher")
5. Select **"Mobile"** as platform

### Step 2: Enable Unity Ads

1. In your Unity Dashboard, go to **"Monetization"**
2. Click **"Get started"** or **"Enable Unity Ads"**
3. Enable ads for **Android** platform
4. Copy your **Game ID** (format: `1234567`)

### Step 3: Configure Ad Placements

1. Go to **"Monetization"** → **"Ad units"**
2. You should see default placements created automatically:
   - `Rewarded_Android` - Default rewarded ad placement
3. Or create a custom placement:
   - Click **"Create ad unit"**
   - Name: `getcoin`
   - Type: **Rewarded**
   - Platform: **Android**
   - Click **"Save"**

### Step 4: Update Your App Code

Open `getCoin.java` and update these constants:

```java
// Line 44-46
private static final String UNITY_GAME_ID = "5974445"; // ⚠️ REPLACE WITH YOUR GAME ID
private static final String AD_UNIT_ID = "getcoin"; // Use your placement ID
private static final boolean TEST_MODE = true; // Set to false for production
```

**Important:**
- Replace `"5974445"` with your actual Unity Game ID
- Replace `"getcoin"` with your placement ID (default: `"Rewarded_Android"`)
- Keep `TEST_MODE = true` during testing
- Set `TEST_MODE = false` before publishing to Google Play

---

## 🧪 Testing

### Test Mode (Enabled by default)
When `TEST_MODE = true`, Unity Ads will show **test ads** that don't require:
- Real advertisers
- Real ad inventory
- App approval

**Test ads will:**
- ✅ Show immediately
- ✅ Complete full ad cycle
- ✅ Trigger reward callbacks
- ✅ Not generate real revenue

### How to Test:
1. Build and install your app
2. Navigate to the "Get Coins" screen
3. Click **"WATCH AD"** button
4. Unity test ad should appear
5. Watch the full ad (don't skip)
6. Verify you receive 10 coins

### Expected Test Ad Behavior:
- Shows Unity logo and test creative
- Has a countdown timer (15-30 seconds)
- May show "This is a test ad" message
- Triggers reward on completion

---

## 📊 Production Setup

### Before Publishing:

1. **Disable Test Mode:**
   ```java
   private static final boolean TEST_MODE = false;
   ```

2. **Verify Game ID:**
   - Make sure your Unity Game ID is correct
   - Double-check placement ID matches your dashboard

3. **Add your App to Unity Dashboard:**
   - Go to Unity Dashboard → "Apps"
   - Click "Add app"
   - Select Android
   - Enter package name: `org.levimc.launcher`
   - Upload your APK/AAB (optional but recommended)

4. **Configure Payment Details:**
   - Go to "Revenue & Payment" in Unity Dashboard
   - Complete your payment information to receive ad revenue

5. **Enable Mediation (Optional):**
   - Go to "Mediation" to add more ad networks
   - This can increase fill rate and eCPM

---

## 🔍 Troubleshooting

### Issue: "Ad not ready" message

**Causes:**
- No internet connection
- Unity Ads SDK not initialized
- Ad not loaded yet
- Test mode disabled but app not approved

**Solutions:**
1. Check internet connection
2. Wait a few seconds and try again
3. Enable test mode during development
4. Check Logcat for Unity Ads errors:
   ```
   adb logcat | grep -i unity
   ```

### Issue: "Ad initialization failed"

**Causes:**
- Invalid Game ID
- Network error
- SDK version mismatch

**Solutions:**
1. Verify your Game ID is correct (no extra spaces)
2. Check your Unity Dashboard shows the game
3. Clean and rebuild project:
   ```
   gradlew clean assembleDebug
   ```

### Issue: Rewards not granted

**Causes:**
- User skipped ad before completion
- Ad not watched fully
- Firestore permission error

**Solutions:**
1. Make sure user watches ad to completion
2. Check Logcat for errors:
   ```
   adb logcat | grep -E "getCoin|Unity"
   ```
3. Verify Firestore security rules allow writes to user document

### Issue: "Build error" during compilation

**Solutions:**
1. Sync Gradle files
2. Invalidate caches: File → Invalidate Caches → Invalidate and Restart
3. Check you're using Java 8+

---

## 📖 Additional Resources

### Unity Ads Documentation:
- **Integration Guide**: https://docs.unity.com/ads/en/manual/InstallingUnityAdsAndroid
- **API Reference**: https://docs.unity.com/ads/en/manual/APIReference
- **Dashboard**: https://dashboard.unity3d.com/

### Best Practices:
- Don't show ads too frequently (respect MAX_ADS_PER_DAY limit)
- Always reward users for completed ads
- Handle ad failures gracefully
- Test thoroughly before production release

### Support:
- Unity Ads Support: https://support.unity.com/
- Unity Forum: https://forum.unity.com/forums/unity-ads.67/

---

## 💰 Revenue Optimization

### Tips to Maximize Revenue:

1. **Enable Mediation**: Add multiple ad networks
2. **Optimize Placement**: Show ads at natural break points
3. **Increase Fill Rate**: Test different regions/times
4. **Use Analytics**: Track ad performance in Unity Dashboard
5. **A/B Testing**: Test different reward amounts

### Expected Revenue:
- **eCPM**: $5-$15 (varies by region)
- **Fill Rate**: 70-95% (varies by region)
- **Revenue per DAU**: $0.05-$0.20 (depends on ad frequency)

---

## ✅ Checklist

Before production release:

- [ ] Updated `UNITY_GAME_ID` with real Game ID
- [ ] Updated `AD_UNIT_ID` with correct placement
- [ ] Set `TEST_MODE = false`
- [ ] Tested ads in debug build
- [ ] Verified rewards are granted correctly
- [ ] Added app to Unity Dashboard
- [ ] Configured payment details
- [ ] Tested on multiple devices
- [ ] Checked Firestore security rules
- [ ] Updated version code/name
- [ ] Created signed release APK/AAB

---

## 🎉 You're All Set!

Your app is now using Unity Ads instead of AdMob. Make sure to:
1. Update your Game ID
2. Test thoroughly
3. Disable test mode before production
4. Monitor performance in Unity Dashboard

Good luck with your app! 🚀

