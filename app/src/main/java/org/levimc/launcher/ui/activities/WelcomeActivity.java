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


import org.json.JSONException;
import org.json.JSONObject;
import org.levimc.launcher.R;

public class WelcomeActivity extends AppCompatActivity {

    private TextView btnLogin_discord;
    private ImageView imageAvatar;

    private TextView term;

    private Button launcher_play_button;

    private Boolean logined = false; // vì đang test nên cho true, nhớ đổi lại false

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcome);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnLogin_discord = findViewById(R.id.btnLoginDiscord);
        term = findViewById(R.id.term);
        launcher_play_button = findViewById(R.id.launcher_play_button);


        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        decorView.setSystemUiVisibility(uiOptions);

        // Lắng nghe khi người dùng vuốt hiện thanh nav/status bar
        decorView.setOnSystemUiVisibilityChangeListener(visibility -> {
            if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                decorView.setSystemUiVisibility(uiOptions);
            }
        });
        term.setOnClickListener(v -> {
//            Intent intent = new Intent(this, DownloadData.class);
//            startActivity(intent);
//            finish();
            Toast.makeText(this, "test", Toast.LENGTH_SHORT).show();
        });

        launcher_play_button.setOnClickListener(v -> {
            if (logined != false){
                Intent intent = new Intent(this, DownloadData.class);
                startActivity(intent);
                finish();
            }else{
                Toast.makeText(this, "Please log in", Toast.LENGTH_SHORT).show();
            }
        });


        btnLogin_discord.setOnClickListener(v -> {
            // Sử dụng scheme lfulauncher://auth để đồng bộ với manifest và website
            String url = "https://lflauncher.vercel.app/api/auth/discord?scheme=1";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.setPackage("com.android.chrome");
            try {
                startActivity(intent);
            } catch (Exception e) {
                // Nếu máy không có Chrome, fallback mở intent mặc định
                intent.setPackage(null);
                startActivity(intent);
            }
        });
        // Kiểm tra xem có dữ liệu deep link khi vào lại app không
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
                    // Parse user info
                    JSONObject user = new JSONObject(userJson);
                    String userId = user.optString("id", "");
                    String avatarId = user.optString("avatar", "");
                    String username = user.optString("username", "");
                    String avatarUrl = "";
                    if (!userId.isEmpty() && !avatarId.isEmpty()) {
                        avatarUrl = "https://cdn.discordapp.com/avatars/" + userId + "/" + avatarId + ".png";
                    } else {
                    }
                    // Hiển thị user name (nếu thích)
                    btnLogin_discord.setText("login successful, hello " + username + "!");
                    btnLogin_discord.setTextColor(Color.parseColor("#6cc248"));
                    logined = true;

                    // Lưu thông tin vào SharedPreferences
                    SharedPreferences prefs = getSharedPreferences("user_info", MODE_PRIVATE);
                    prefs.edit()
                            .putString("jwt", jwt)
                            .putString("user_json", userJson)
                            .putString("avatar_url", avatarUrl)
                            .putString("username", username)
                            .apply();
                    Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                    // TODO: Chuyển màn hình, hoặc gọi API xác thực...
                } catch (JSONException e) {
                    Toast.makeText(this, "Lỗi xử lý thông tin user!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Không nhận được mã truy cập hoặc thông tin user!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}