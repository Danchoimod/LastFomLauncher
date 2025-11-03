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
import org.levimc.launcher.util.SoundPoolUtil;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends BaseActivity {

    ImageView imgLeaf;
    ImageView tvAppName;
    private FirebaseFirestore db; // Firestore

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        SoundPoolUtil.init(this);
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
        SharedPreferences dataPrefs = getSharedPreferences("prefs", MODE_PRIVATE);

        // Kiểm tra cả version và patchnotes
        db.collection("update").document("version")
                .get()
                .addOnSuccessListener(versionDoc -> {
                    db.collection("update").document("patchnotes")
                            .get()
                            .addOnSuccessListener(patchnotesDoc -> {
                                boolean needUpdate = false;

                                // Kiểm tra version
                                if (versionDoc.exists()) {
                                    Long remoteVersionIndex = versionDoc.getLong("index");
                                    if (remoteVersionIndex != null) {
                                        long localVersionIndex = dataPrefs.getLong("versionIndex", -1);
                                        if (remoteVersionIndex != localVersionIndex) {
                                            needUpdate = true;
                                        }
                                    }
                                }

                                // Kiểm tra patchnotes
                                if (patchnotesDoc.exists()) {
                                    Long remotePatchnotesIndex = patchnotesDoc.getLong("index");
                                    if (remotePatchnotesIndex != null) {
                                        long localPatchnotesIndex = dataPrefs.getLong("patchnotesIndex", -1);
                                        if (remotePatchnotesIndex != localPatchnotesIndex) {
                                            needUpdate = true;
                                        }
                                    }
                                }

                                // Nếu có update, chuyển vào DownloadData
                                if (needUpdate) {
                                    Intent intent = new Intent(this, DownloadData.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // Không có update, vào MainLauncher
                                    SoundPoolUtil.play(this, R.raw.boot_up_2);
                                    Intent intent = new Intent(this, MainLauncher.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                }
                            })
                            .addOnFailureListener(e -> {
                                // Nếu patchnotes fail, vẫn kiểm tra version
                                checkVersionUpdateOnly(versionDoc, dataPrefs);
                            });
                })
                .addOnFailureListener(e -> {
                    // Nếu lỗi, vẫn vào MainLauncher
                    SoundPoolUtil.play(this, R.raw.boot_up_2);
                    Intent intent = new Intent(this, MainLauncher.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });
    }

    private void checkVersionUpdateOnly(com.google.firebase.firestore.DocumentSnapshot versionDoc, SharedPreferences dataPrefs) {
        boolean needUpdate = false;

        if (versionDoc.exists()) {
            Long remoteVersionIndex = versionDoc.getLong("index");
            if (remoteVersionIndex != null) {
                long localVersionIndex = dataPrefs.getLong("versionIndex", -1);
                if (remoteVersionIndex != localVersionIndex) {
                    needUpdate = true;
                }
            }
        }

        if (needUpdate) {
            Intent intent = new Intent(this, DownloadData.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            SoundPoolUtil.play(this, R.raw.boot_up_2);
            Intent intent = new Intent(this, MainLauncher.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }
}
