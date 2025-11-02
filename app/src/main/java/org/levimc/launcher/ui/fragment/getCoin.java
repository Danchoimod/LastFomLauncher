package org.levimc.launcher.ui.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.ads.rewarded.ServerSideVerificationOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.levimc.launcher.databinding.FragmentGetCoinBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class getCoin extends Fragment {

    private static final String TAG = "getCoin";
    private static final int MAX_ADS_PER_DAY = 10;
    private static final int COINS_PER_AD = 10;
    private static final int DAILY_REWARD_COINS = 5;

    // Production Ad Unit ID - Get from AdMob Console
    // Bước 1: Vào https://apps.admob.com
    // Bước 2: Chọn app của bạn -> Ad units
    // Bước 3: Copy Ad unit ID của Rewarded ad unit
    private static final String AD_UNIT_ID = "ca-app-pub-8177702634836557/8123511561"; // ⚠️ REPLACE WITH YOUR REAL AD UNIT ID
    // Test ID (for testing): ca-app-pub-3940256099942544/5224354917

    private FragmentGetCoinBinding binding;
    private RewardedAd rewardedAd;
    private SharedPreferences prefs;
    private FirebaseFirestore db;
    private String userId;

    private int adsWatchedToday = 0;
    private boolean dailyRewardClaimed = false;
    private boolean isLoadingAd = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = requireContext().getSharedPreferences("user_info", android.content.Context.MODE_PRIVATE);
        userId = prefs.getString("user_id", null);
        db = FirebaseFirestore.getInstance();

        // Initialize Mobile Ads SDK
        MobileAds.initialize(requireContext(), initializationStatus -> {
            Log.d(TAG, "AdMob initialized");
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentGetCoinBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (userId == null || userId.isEmpty()) {
            Toast.makeText(requireContext(), "Please login first", Toast.LENGTH_SHORT).show();
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
            return;
        }

        // Load data from Firestore
        loadUserAdDataFromFirestore();

        // Watch Ad button
        binding.watchAdButton.setOnClickListener(v -> showRewardedAd());

        // Claim Daily button
        binding.claimDailyButton.setOnClickListener(v -> claimDailyReward());

        // Load first ad
        loadRewardedAd();
    }

    private void claimDailyReward() {
        if (dailyRewardClaimed) {
            Toast.makeText(requireContext(), "Daily reward already claimed today!", Toast.LENGTH_SHORT).show();
            return;
        }

        String today = getTodayDate();

        // Update Firestore: mark as claimed AND increment coins
        Map<String, Object> updates = new HashMap<>();
        updates.put("lastDailyReward", today);
        updates.put("userId", userId);
        updates.put("coin", FieldValue.increment(DAILY_REWARD_COINS)); // Increment coins by 3

        db.collection("users").document(userId)
            .set(updates, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener(aVoid -> {
                dailyRewardClaimed = true;
                updateUI();

                Log.d(TAG, "Daily reward claimed successfully! +" + DAILY_REWARD_COINS + " coins");
                Toast.makeText(requireContext(),
                    "Claimed " + DAILY_REWARD_COINS + " coins successfully! ✓",
                    Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to claim daily reward", e);
                Toast.makeText(requireContext(), "Failed to claim reward: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void loadRewardedAd() {
        if (isLoadingAd) return;

        isLoadingAd = true;
        AdRequest adRequest = new AdRequest.Builder().build();

        RewardedAd.load(requireContext(), AD_UNIT_ID, adRequest, new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull RewardedAd ad) {
                rewardedAd = ad;
                isLoadingAd = false;
                Log.d(TAG, "Rewarded ad loaded");

                // Set Server-Side Verification Options
                ServerSideVerificationOptions options = new ServerSideVerificationOptions.Builder()
                    .setCustomData("user_id=" + userId + "&reward_amount=" + COINS_PER_AD)
                    .setUserId(userId)
                    .build();
                rewardedAd.setServerSideVerificationOptions(options);

                // Set callback for full screen content
                rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        Log.d(TAG, "Ad dismissed");
                        rewardedAd = null;
                        loadRewardedAd(); // Load next ad
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                        Log.e(TAG, "Ad failed to show: " + adError.getMessage());
                        rewardedAd = null;
                        isLoadingAd = false;
                        Toast.makeText(requireContext(), "Failed to show ad", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        Log.d(TAG, "Ad showed fullscreen");
                    }
                });
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                Log.e(TAG, "Failed to load ad: " + loadAdError.getMessage());
                rewardedAd = null;
                isLoadingAd = false;
            }
        });
    }

    private void showRewardedAd() {
        if (adsWatchedToday >= MAX_ADS_PER_DAY) {
            Toast.makeText(requireContext(), "You've reached the daily limit of " + MAX_ADS_PER_DAY + " ads",
                Toast.LENGTH_SHORT).show();
            return;
        }

        if (rewardedAd == null) {
            Toast.makeText(requireContext(), "Ad is not ready yet. Please try again in a moment.",
                Toast.LENGTH_SHORT).show();
            if (!isLoadingAd) {
                loadRewardedAd(); // Try loading again
            }
            return;
        }

        rewardedAd.show(requireActivity(), new OnUserEarnedRewardListener() {
            @Override
            public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                // User earned reward
                Log.d(TAG, "User earned reward: " + rewardItem.getAmount());

                // Increment counter and save to Firestore
                adsWatchedToday++;
                saveAdWatchCountToFirestore();

                // Also increment coins directly (in case SSV callback fails or is not configured)
                grantAdRewardCoins();

                updateUI();

                Toast.makeText(requireContext(),
                    "+" + COINS_PER_AD + " coins earned! ✓",
                    Toast.LENGTH_SHORT).show();

                // If reached limit, go back to marketplace
                if (adsWatchedToday >= MAX_ADS_PER_DAY) {
                    Toast.makeText(requireContext(),
                        "Daily limit reached! Come back tomorrow.",
                        Toast.LENGTH_LONG).show();

                    if (getParentFragmentManager() != null) {
                        getParentFragmentManager().popBackStack();
                    }
                }
            }
        });
    }

    /**
     * Load ad watch count and daily reward status from Firestore
     */
    private void loadUserAdDataFromFirestore() {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String today = getTodayDate();
                    String lastAdDate = documentSnapshot.getString("lastAdDate");
                    String lastDailyReward = documentSnapshot.getString("lastDailyReward");

                    // Check ad watch count
                    if (today.equals(lastAdDate)) {
                        // Same day, get count from Firestore
                        Long count = documentSnapshot.getLong("adsWatchedToday");
                        adsWatchedToday = count != null ? count.intValue() : 0;
                    } else {
                        // New day, reset count
                        adsWatchedToday = 0;
                    }

                    // Check daily reward status
                    dailyRewardClaimed = today.equals(lastDailyReward);

                } else {
                    // New user, create document
                    adsWatchedToday = 0;
                    dailyRewardClaimed = false;
                    createUserAdDocument();
                }

                updateUI();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to load user ad data", e);
                Toast.makeText(requireContext(), "Failed to load data", Toast.LENGTH_SHORT).show();

                // Fallback to default values
                adsWatchedToday = 0;
                dailyRewardClaimed = false;
                updateUI();
            });
    }

    /**
     * Create initial user ad document in Firestore
     */
    private void createUserAdDocument() {
        String today = getTodayDate();
        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", userId);
        userData.put("lastAdDate", today);
        userData.put("adsWatchedToday", 0);
        userData.put("lastDailyReward", "");

        db.collection("users").document(userId)
            .set(userData)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "User ad document created");
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to create user ad document", e);
            });
    }

    /**
     * Save ad watch count to Firestore
     */
    private void saveAdWatchCountToFirestore() {
        String today = getTodayDate();
        Map<String, Object> updates = new HashMap<>();
        updates.put("lastAdDate", today);
        updates.put("adsWatchedToday", adsWatchedToday);
        updates.put("userId", userId); // Add userId for security rules

        db.collection("users").document(userId)
            .set(updates, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Ad watch count saved: " + adsWatchedToday);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to save ad watch count", e);
                Toast.makeText(requireContext(), "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    /**
     * Grant coins to user after watching ad
     */
    private void grantAdRewardCoins() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("coin", FieldValue.increment(COINS_PER_AD)); // Increment coins by 10

        db.collection("users").document(userId)
            .update(updates)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Ad reward granted: +" + COINS_PER_AD + " coins");
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to grant ad reward", e);
                // Try with set+merge if update fails (document might not exist)
                db.collection("users").document(userId)
                    .set(updates, com.google.firebase.firestore.SetOptions.merge())
                    .addOnSuccessListener(aVoid2 -> {
                        Log.d(TAG, "Ad reward granted (via set): +" + COINS_PER_AD + " coins");
                    })
                    .addOnFailureListener(e2 -> {
                        Log.e(TAG, "Failed to grant ad reward (retry)", e2);
                    });
            });
    }

    private void updateUI() {
        int remaining = MAX_ADS_PER_DAY - adsWatchedToday;

        // Update old remaining text
        binding.adsRemainingText.setText("plays remaining: " + remaining);

        // Update new ad limit display
        binding.adLimitText.setText(remaining + " / " + MAX_ADS_PER_DAY + " ads remaining");

        // Update reward amount display
        binding.rewardAmountText.setText(String.valueOf(COINS_PER_AD));

        // Disable watch ad button if limit reached
        boolean canWatchAds = adsWatchedToday < MAX_ADS_PER_DAY;
        binding.watchAdButton.setEnabled(canWatchAds);
        binding.watchAdButton.setAlpha(canWatchAds ? 1.0f : 0.5f);

        if (!canWatchAds) {
            binding.watchAdButton.setText("LIMIT REACHED");
        } else {
            binding.watchAdButton.setText("WATCH AD →");
        }

        // Update daily reward button
        binding.claimDailyButton.setEnabled(!dailyRewardClaimed);
        binding.claimDailyButton.setAlpha(dailyRewardClaimed ? 0.5f : 1.0f);

        if (dailyRewardClaimed) {
            binding.dailyRewardStatus.setText("Come back tomorrow!");
            binding.claimDailyButton.setText("CLAIMED");
        } else {
            binding.dailyRewardStatus.setText("Claim 3 coins every day!");
            binding.claimDailyButton.setText("CLAIM");
        }
    }

    private String getTodayDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return sdf.format(new Date());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}