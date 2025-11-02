package org.levimc.launcher.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import org.levimc.launcher.R;
import org.levimc.launcher.databinding.ActivityMarketplaceDetailBinding;
import org.levimc.launcher.network.ApiClient;

import java.util.Locale;
import java.util.Set;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class MarketplaceDetailActivity extends BaseActivity {

    private String id, name, description, imageUrl, owner, ownerUrl, type, url;
    private double price;
    private Integer packId;

    private ImageView cover;
    private TextView title, typeChip, author, count, priceView, balanceView, descView;
    private Button primaryBtn;
    private ProgressBar progress;

    private ActivityMarketplaceDetailBinding binding;

    private boolean isOwned = false;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marketplace_detail);

        id = getIntent().getStringExtra("id");
        name = getIntent().getStringExtra("name");
        description = getIntent().getStringExtra("description");
        imageUrl = getIntent().getStringExtra("imageUrl");
        owner = getIntent().getStringExtra("owner");
        ownerUrl = getIntent().getStringExtra("ownerUrl");
        type = getIntent().getStringExtra("type");
        url = getIntent().getStringExtra("url");
    price = getIntent().getDoubleExtra("price", -1);
    if (getIntent().hasExtra("packid")) packId = getIntent().getIntExtra("packid", -1);
    if (packId != null && packId < 0) packId = null;

        cover = findViewById(R.id.coverImage);
        title = findViewById(R.id.detailTitle);
        typeChip = findViewById(R.id.typeChip);
        author = findViewById(R.id.author);
        count = findViewById(R.id.countLabel);
        priceView = findViewById(R.id.priceLabel);
        balanceView = findViewById(R.id.balanceLabel);
        descView = findViewById(R.id.detailDesc);
        primaryBtn = findViewById(R.id.primaryBtn);
        progress = findViewById(R.id.progress);

        // Wire up back button to finish the activity
        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        title.setText(TextUtils.isEmpty(name) ? "(no title)" : name);
        typeChip.setText(displayType(type));
        author.setText(TextUtils.isEmpty(owner) ? "Unknown" : owner);
        author.setPaintFlags(author.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        author.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(ownerUrl)) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(ownerUrl)));
            }
        });

        count.setText("1 " + displayType(type));
        descView.setText(TextUtils.isEmpty(description) ? getString(R.string.no_description) : description);

        if (!TextUtils.isEmpty(imageUrl)) {
            Glide.with(this).load(imageUrl).placeholder(R.drawable.ic_launcher_background).into(cover);
        } else {
            cover.setImageResource(R.drawable.ic_launcher_background);
        }

        priceView.setText(formatPrice(price));

        primaryBtn.setOnClickListener(v -> {
            if (isOwned) {
                if (!TextUtils.isEmpty(url)) startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                else Toast.makeText(this, R.string.no_download_link, Toast.LENGTH_SHORT).show();
            } else {
                doPurchase();
            }
        });

        refreshOwnedAndBalance();
        // Also fetch coin directly from Firestore (users/{userId}.coin)
        fetchUserCoinFromFirestore();
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        primaryBtn.setEnabled(!loading);
    }

    private void refreshOwnedAndBalance() {
        setLoading(true);
        primaryBtn.setVisibility(View.GONE);
        // Prefer a direct ownership check when possible
        ApiClient.checkOwnedAsync(this, id, packId, (owned, balance, error) -> {
            mainHandler.post(() -> {
                setLoading(false);
                primaryBtn.setVisibility(View.VISIBLE);
                if (owned != null) {
                    isOwned = owned;
                    primaryBtn.setText(isOwned ? R.string.download_now : R.string.buy_now);
                }
                if (balance != null) balanceView.setText(getString(R.string.current_balance_format, balance));
            });
        });
    }

    // legacy support kept via ApiClient.checkOwnedAsync fallback

    private void doPurchase() {
        android.util.Log.d("MarketplaceDetail", "=== PURCHASE BUTTON CLICKED ===");
        android.util.Log.d("MarketplaceDetail", "Item ID: " + id);
        android.util.Log.d("MarketplaceDetail", "Pack ID: " + packId);
        android.util.Log.d("MarketplaceDetail", "Price: " + price);
        
        setLoading(true);
        ApiClient.purchaseAsync(this, id, packId, (success, message, newBalance, error) -> {
            mainHandler.post(() -> {
                android.util.Log.d("MarketplaceDetail", "Purchase callback received");
                android.util.Log.d("MarketplaceDetail", "Success: " + success);
                android.util.Log.d("MarketplaceDetail", "Message: " + message);
                android.util.Log.d("MarketplaceDetail", "New balance: " + newBalance);
                android.util.Log.d("MarketplaceDetail", "Error: " + (error != null ? error.getMessage() : "null"));
                
                setLoading(false);
                if (success) {
                    android.util.Log.i("MarketplaceDetail", "Purchase SUCCESS!");
                    Toast.makeText(this, R.string.purchase_success, Toast.LENGTH_SHORT).show();
                    isOwned = true;
                    primaryBtn.setText(R.string.download_now);
                    if (newBalance != null) {
                        balanceView.setText(getString(R.string.current_balance_format, newBalance));
                    } else {
                        // Best-effort refresh
                        refreshOwnedAndBalance();
                        fetchUserCoinFromFirestore();
                    }
                } else {
                    android.util.Log.e("MarketplaceDetail", "Purchase FAILED!");
                    String msg = message;
                    if (error != null) msg = error.getMessage();
                    if (msg == null || msg.isEmpty()) msg = getString(R.string.purchase_failed);
                    android.util.Log.e("MarketplaceDetail", "Error message shown to user: " + msg);
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                }
                android.util.Log.d("MarketplaceDetail", "=== PURCHASE FLOW COMPLETE ===");
            });
        });
    }

    private String displayType(String type) {
        if (type == null) return "";
        String t = type.toLowerCase(Locale.US);
        if (t.contains("map")) return "Maps";
        if (t.contains("texture")) return "Texture pack";
        if (t.contains("skin")) return "Skins";
        if (t.contains("mod")) return "Mods";
        if (t.contains("addon")) return "Addon";
        return type;
    }

    private String formatPrice(double price) {
        if (price <= 0) return getString(R.string.free_label);
        if (Math.floor(price) == price) return ((int) price) + " LFC";
        return price + " LFC";
    }

    private void fetchUserCoinFromFirestore() {
        SharedPreferences prefs = getSharedPreferences("user_info", MODE_PRIVATE);
        String userId = prefs.getString("user_id", null);
        if (userId == null || userId.isEmpty()) return;
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(snapshot -> updateCoinFromSnapshot(snapshot))
                .addOnFailureListener(e -> { /* ignore silently */ });
    }

    private void updateCoinFromSnapshot(DocumentSnapshot snapshot) {
        if (snapshot == null || !snapshot.exists()) return;
        Number coin = (Number) snapshot.get("coin");
        if (coin != null) {
            int c = coin.intValue();
            balanceView.setText(getString(R.string.current_balance_format, c));
        }
    }
}
