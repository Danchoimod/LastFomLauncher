package org.levimc.launcher.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;
import org.levimc.launcher.R;
import org.levimc.launcher.databinding.ActivityWelcomeBinding;

public class WelcomeActivity extends AppCompatActivity {

    private TextView btnLogin_discord;
    private ImageView imageAvatar;
    private TextView term;
    private FirebaseFirestore db;
    private Button launcher_play_button;
    private ActivityWelcomeBinding binding;
    private Boolean logined = true; // Mặc định false khi phát hành

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityWelcomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Ánh xạ view
        btnLogin_discord = findViewById(R.id.btnLoginDiscord);
        term = findViewById(R.id.term);
        launcher_play_button = findViewById(R.id.launcher_play_button);

        // Khởi tạo Firestore
        db = FirebaseFirestore.getInstance();

        // Ẩn thanh điều hướng & trạng thái (Immersive mode)
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
        decorView.setOnSystemUiVisibilityChangeListener(visibility -> {
            if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                decorView.setSystemUiVisibility(uiOptions);
            }
        });

        // Click vào dòng điều khoản
        term.setOnClickListener(v -> {
            Toast.makeText(this, "This is a beta test version", Toast.LENGTH_SHORT).show();
        });

        // Nút "PLAY" (bắt đầu launcher)
        launcher_play_button.setOnClickListener(v -> {
            if (logined && binding.lisenseCheck.isChecked()) {
                // Lấy userId đã lưu trong SharedPreferences
                SharedPreferences prefs = getSharedPreferences("user_info", MODE_PRIVATE);
                String userId = prefs.getString("user_id", null);

                if (userId != null) {
                    db.collection("users").document(userId).get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    Boolean isActive = documentSnapshot.getBoolean("active");
                                    if (isActive != null && !isActive) {
                                        // Người dùng chưa kích hoạt license
                                        Intent intent = new Intent(this, lisense.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        // Người dùng đã kích hoạt -> chuyển qua DownloadData
                                        Intent intent = new Intent(this, DownloadData.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                } else {
                                    Toast.makeText(this, "User data not found.", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to fetch user data.", Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Toast.makeText(this, "No user ID found. Please log in again.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Please log in & accept license", Toast.LENGTH_SHORT).show();
            }
        });

        // Nút đăng nhập Discord
        btnLogin_discord.setOnClickListener(v -> {
            String url = "https://lflauncher.vercel.app/api/auth/discord?scheme=1";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.setPackage("com.android.chrome");
            try {
                startActivity(intent);
            } catch (Exception e) {
                intent.setPackage(null);
                startActivity(intent);
            }
        });

        // Kiểm tra deep link (khi quay lại app từ website Discord OAuth)
        handleDeepLink(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleDeepLink(intent);
    }

    private void handleDeepLink(Intent intent) {
        Uri data = intent.getData();
        if (data != null && "lflauncher".equals(data.getScheme()) && "auth".equals(data.getHost())) {
            // Nhận giá trị jwt và user từ website trả về
            String jwt = data.getQueryParameter("jwt");
            String userJson = data.getQueryParameter("user");

            if (jwt != null && !jwt.isEmpty() && userJson != null && !userJson.isEmpty()) {
                try {
                    // Parse thông tin người dùng từ JSON
                    JSONObject user = new JSONObject(userJson);
                    String userId = user.optString("id", "");
                    String avatarId = user.optString("avatar", "");
                    String username = user.optString("username", "");
                    String avatarUrl = "";

                    if (!userId.isEmpty() && !avatarId.isEmpty()) {
                        avatarUrl = "https://cdn.discordapp.com/avatars/" + userId + "/" + avatarId + ".png";
                    }

                    // Cập nhật giao diện
                    btnLogin_discord.setText("Login successful, hello " + username + "!");
                    btnLogin_discord.setTextColor(Color.parseColor("#6cc248"));
                    logined = true;

                    // Lưu toàn bộ thông tin vào SharedPreferences
                    SharedPreferences prefs = getSharedPreferences("user_info", MODE_PRIVATE);
                    prefs.edit()
                            .putString("jwt", jwt)
                            .putString("user_json", userJson)
                            .putString("avatar_url", avatarUrl)
                            .putString("username", username)
                            .putString("user_id", userId) // ✅ Thêm userId để dùng sau này
                            .apply();

                    Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                } catch (JSONException e) {
                    Toast.makeText(this, "Lỗi xử lý thông tin user!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Không nhận được mã truy cập hoặc thông tin user!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
