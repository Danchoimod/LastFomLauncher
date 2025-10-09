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

import com.bumptech.glide.Glide;

import org.levimc.launcher.R;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends BaseActivity {

    ImageView imgLeaf;
    ImageView tvAppName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        imgLeaf = findViewById(R.id.imgLeaf);
        tvAppName = findViewById(R.id.tvAppName);
        ImageView imgLoadingIcon = findViewById(R.id.imgLoadingIcon);

        startLeafAnimation();
        startAppNameAnimation();

        tvAppName.postDelayed(() -> {
            checkLogin();
        }, 3000);
        Glide.with(this) // hoặc getApplicationContext()
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
    private void checkLogin() {
        SharedPreferences prefs = getSharedPreferences("user_info", MODE_PRIVATE);

        if (prefs.getString("username", null) != null) {
            Intent intent = new Intent(this, MainLauncher.class);
            startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent(this, WelcomeActivity.class);
            startActivity(intent);
            finish();
        }
    }
}