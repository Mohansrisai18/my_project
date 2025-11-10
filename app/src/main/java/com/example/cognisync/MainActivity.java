package com.example.cognisync;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageButton;

public class MainActivity extends Activity {

    private ImageButton arrowButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        initializeViews();

        // Set click listeners
        setClickListeners();

        // Add entrance animation (optional)
        addEntranceAnimation();
    }

    private void initializeViews() {
        arrowButton = findViewById(R.id.arrowButton);
    }

    private void setClickListeners() {
        arrowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add button press animation
                animateButtonPress(v);

                // Navigate to login after animation
                v.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        navigateToLogin();
                    }
                }, 150); // Small delay for animation
            }
        });
    }

    private void animateButtonPress(View view) {
        // Scale animation for button press effect
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                1.0f, 0.95f, 1.0f, 0.95f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleAnimation.setDuration(150);
        scaleAnimation.setRepeatCount(1);
        scaleAnimation.setRepeatMode(Animation.REVERSE);
        view.startAnimation(scaleAnimation);
    }

    private void addEntranceAnimation() {
        // Optional: Add fade in animation for the button
        arrowButton.setAlpha(0f);
        arrowButton.animate()
                .alpha(1f)
                .setDuration(1000)
                .setStartDelay(500)
                .start();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);

        // Add transition animation
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reset button state when returning to this activity
        if (arrowButton != null) {
            arrowButton.setEnabled(true);
            arrowButton.clearAnimation();
        }
    }
}
