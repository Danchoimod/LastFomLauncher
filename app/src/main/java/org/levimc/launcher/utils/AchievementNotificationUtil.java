package org.levimc.launcher.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;

import org.levimc.launcher.R;

/**
 * Utility class to show achievement-style notifications
 * with slide-in animation from right to left
 */
public class AchievementNotificationUtil {

    private static final long ANIMATION_DURATION = 500; // milliseconds
    private static final long DISPLAY_DURATION = 3000; // milliseconds
    private static final int MARGIN_TOP = 80; // dp
    private static final int MARGIN_END = 16; // dp (changed from MARGIN_START)

    /**
     * Show an achievement notification with custom icon, title, and description
     *
     * @param activity    The activity to show the notification on
     * @param iconResId   The drawable resource ID for the icon
     * @param title       The title text
     * @param description The description text
     */
    public static void showNotification(Activity activity, @DrawableRes int iconResId, String title, String description) {
        if (activity == null || activity.isFinishing()) {
            return;
        }

        activity.runOnUiThread(() -> {
            // Inflate the notification layout
            LayoutInflater inflater = LayoutInflater.from(activity);
            View notificationView = inflater.inflate(R.layout.notification, null);

            // Set up the views
            ImageView iconView = notificationView.findViewById(R.id.achievement_icon);
            TextView titleView = notificationView.findViewById(R.id.achievement_title);
            TextView descriptionView = notificationView.findViewById(R.id.achievement_description);

            iconView.setImageResource(iconResId);
            titleView.setText(title);
            descriptionView.setText(description);

            // Get the root view of the activity
            ViewGroup rootView = activity.findViewById(android.R.id.content);
            FrameLayout container = new FrameLayout(activity);

            // Set up layout parameters for positioning at top-right (góc trên bên phải)
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            );
            params.gravity = Gravity.TOP | Gravity.END; // Changed to END (right side)
            params.topMargin = dpToPx(activity, MARGIN_TOP);
            params.rightMargin = dpToPx(activity, MARGIN_END);

            container.setLayoutParams(params);
            container.addView(notificationView);

            // Add to root view
            rootView.addView(container);

            // Measure the view to get its width for animation
            notificationView.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            );

            // Get the notification width
            int viewWidth = notificationView.getMeasuredWidth();

            // Start position: off-screen to the right (ẩn ngoài màn hình bên phải)
            // Container sẽ bắt đầu từ vị trí +viewWidth (ngoài màn hình)
            container.setTranslationX(viewWidth);

            // Create slide-in animation from right to left
            // Trượt từ vị trí +viewWidth về 0 (vị trí góc phải cố định)
            ObjectAnimator slideIn = ObjectAnimator.ofFloat(
                    container,
                    "translationX",
                    viewWidth,  // Bắt đầu: ẩn bên phải
                    0f          // Kết thúc: vị trí góc phải
            );
            slideIn.setDuration(ANIMATION_DURATION);
            slideIn.setInterpolator(new DecelerateInterpolator());

            // Create fade-in animation
            ObjectAnimator fadeIn = ObjectAnimator.ofFloat(notificationView, "alpha", 0f, 1f);
            fadeIn.setDuration(ANIMATION_DURATION / 2);

            // Start animations
            slideIn.start();
            fadeIn.start();

            // Auto-dismiss after display duration
            container.postDelayed(() -> {
                dismissNotification(activity, container, notificationView);
            }, DISPLAY_DURATION);

            // Optional: Click to dismiss
            notificationView.setOnClickListener(v -> {
                dismissNotification(activity, container, notificationView);
            });
        });
    }

    /**
     * Show notification with default icon (Java icon)
     */
    public static void showNotification(Activity activity, String title, String description) {
        showNotification(activity, R.drawable.java, title, description);
    }

    /**
     * Dismiss the notification with slide-out animation
     */
    private static void dismissNotification(Activity activity, ViewGroup container, View notificationView) {
        if (activity == null || activity.isFinishing()) {
            return;
        }

        // Measure view width to slide out properly
        int viewWidth = notificationView.getMeasuredWidth();

        // Create slide-out animation to the right (ra ngoài màn hình bên phải)
        ObjectAnimator slideOut = ObjectAnimator.ofFloat(
                container,
                "translationX",
                0f,         // Từ vị trí hiện tại (góc phải)
                viewWidth   // Ra ngoài màn hình bên phải
        );
        slideOut.setDuration(ANIMATION_DURATION);
        slideOut.setInterpolator(new DecelerateInterpolator());

        // Create fade-out animation
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(notificationView, "alpha", 1f, 0f);
        fadeOut.setDuration(ANIMATION_DURATION / 2);
        fadeOut.setStartDelay(ANIMATION_DURATION / 2);

        slideOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Remove the view from parent after animation
                ViewGroup rootView = activity.findViewById(android.R.id.content);
                if (rootView != null) {
                    rootView.removeView(container);
                }
            }
        });

        slideOut.start();
        fadeOut.start();
    }

    /**
     * Convert dp to pixels
     */
    private static int dpToPx(Activity activity, int dp) {
        float density = activity.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    /**
     * Builder pattern for more flexibility
     */
    public static class Builder {
        private final Activity activity;
        private int iconResId = R.drawable.java;
        private String title = "Achievement";
        private String description = "Unlocked!";
        private long displayDuration = DISPLAY_DURATION;
        private int marginTop = MARGIN_TOP;
        private int marginEnd = MARGIN_END;

        public Builder(Activity activity) {
            this.activity = activity;
        }

        public Builder setIcon(@DrawableRes int iconResId) {
            this.iconResId = iconResId;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setDisplayDuration(long durationMillis) {
            this.displayDuration = durationMillis;
            return this;
        }

        public Builder setMarginTop(int marginTopDp) {
            this.marginTop = marginTopDp;
            return this;
        }

        public Builder setMarginEnd(int marginEndDp) {
            this.marginEnd = marginEndDp;
            return this;
        }

        public void show() {
            showNotification(activity, iconResId, title, description);
        }
    }
}

