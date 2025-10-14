package org.levimc.launcher.ui.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Bundle;
import android.view.animation.AlphaAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import org.levimc.launcher.R;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends BaseActivity {

    ImageView imgLeaf;
    ImageView tvAppName;
    private FirebaseFirestore db; // Firestore

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // ✅ Khởi tạo Firestore NGAY tại đây
        db = FirebaseFirestore.getInstance();

        imgLeaf = findViewById(R.id.imgLeaf);
        tvAppName = findViewById(R.id.tvAppName);
        ImageView imgLoadingIcon = findViewById(R.id.imgLoadingIcon);

        startLeafAnimation();
        startAppNameAnimation();

        // Sau 3 giây kiểm tra login
        tvAppName.postDelayed(() -> {
            SharedPreferences prefs = getSharedPreferences("user_info", MODE_PRIVATE);
            String userId = prefs.getString("user_id", null);

            if (userId != null) { //nếu đã nhập
                db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Boolean isActive = documentSnapshot.getBoolean("active");
                        if (isActive != null && !isActive) {
                            // Chưa kích hoạt license
                            Intent intent = new Intent(this, lisense.class);
                            startActivity(intent);
                            finish();
                        }// Kiểm tra lisense thành công thì chuyển vào màn hình ở dưới
                        else {
                            CheckDataUpdate();
                        }
                    } else {
                        Toast.makeText(this, "User data not found.", Toast.LENGTH_SHORT).show();
                        redirectToLogin();
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch user data.", Toast.LENGTH_SHORT).show();
                    redirectToLogin();
                });
            } else {
                redirectToLogin();
            }
        }, 3000);

        // Hiển thị animation loading GIF
        Glide.with(this)
                .asGif()
                .load(Uri.parse("file:///android_asset/loading.gif"))
                .into(imgLoadingIcon);
    }

    private void startLeafAnimation() {
        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, -600, 0);
        translateAnimation.setDuration(1500);
        translateAnimation.setFillAfter(true);
        translateAnimation.setInterpolator(this, android.R.anim.bounce_interpolator);
        imgLeaf.startAnimation(translateAnimation);
    }

    private void applyTextGradient(TextView textView) {
        int startColor = Color.parseColor("#7BAAF7");
        int endColor = Color.parseColor("#B287F7");
        Shader shader = new LinearGradient(
                0, 0, textView.getPaint().measureText(textView.getText().toString()), 0,
                startColor, endColor, Shader.TileMode.CLAMP
        );
        textView.getPaint().setShader(shader);
    }

    private void startAppNameAnimation() {
        AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1f);
        alphaAnimation.setDuration(2000);
        alphaAnimation.setFillAfter(true);
        tvAppName.startAnimation(alphaAnimation);
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, WelcomeActivity.class);
        startActivity(intent);
        finish();
    }
    private void CheckDataUpdate(){
        db = FirebaseFirestore.getInstance();
        db.collection("update").document("version")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long remoteIndex = documentSnapshot.getLong("index");
                        if (remoteIndex != null) {
                            SharedPreferences dataPrefs = getSharedPreferences("prefs", MODE_PRIVATE);
                            long localIndex = dataPrefs.getLong("index", -1);

                            // Nếu index khác nhau, xóa index local và vào DownloadData
                            if (remoteIndex != localIndex) {
                                dataPrefs.edit().remove("index").apply();
                                Intent intent = new Intent(this, DownloadData.class);
                                startActivity(intent);
                                finish();
                                return;
                            }
                        }
                    }
                    Intent intent = new Intent(this, MainLauncher.class);
                    startActivity(intent);
                    finish();
                }).addOnFailureListener(e -> {
                    // Xử lý lỗi nếu cần
                });
    }
}
