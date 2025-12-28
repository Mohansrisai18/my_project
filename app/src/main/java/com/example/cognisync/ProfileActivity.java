package com.example.cognisync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cognisync.del.ApiClient;
import com.example.cognisync.del.ApiService;
import com.example.cognisync.model.UserProfileResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private ImageButton backButton;
    private Button signOutButton;
    private LinearLayout feedbackOption, accountSettingsOption, resetProgressOption;

    private TextView userName, userInfo;

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "UserPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Hide action bar
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // Light status bar (optional)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            getWindow().getDecorView()
                    .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // -------------------------------
        // INIT VIEWS (MATCH XML)
        // -------------------------------
        backButton = findViewById(R.id.backButton);
        signOutButton = findViewById(R.id.signOutButton);

        feedbackOption = findViewById(R.id.feedbackOption);
        accountSettingsOption = findViewById(R.id.accountSettingsOption);
        resetProgressOption = findViewById(R.id.resetProgressOption);

        userName = findViewById(R.id.userName);
        userInfo = findViewById(R.id.userInfo);

        // init shared prefs BEFORE calling network
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // --------------------------------------------------
        // ✅ BACKWARD-COMPATIBLE USER DETAILS (placeholder while loading)
        // --------------------------------------------------
        String userId = sharedPreferences.getString("user_id", null);
        if (userId == null || userId.isEmpty()) {
            userId = sharedPreferences.getString("username", "User");
        }
        userName.setText(userId);
        // set a placeholder until network loads
        userInfo.setText("Age: --, --");

        // Fetch latest profile from backend and cache locally
        fetchProfileFromBackend();

        // -------------------------------
        // BACK BUTTON
        // -------------------------------
        backButton.setOnClickListener(v -> onBackPressed());

        // -------------------------------
        // FEEDBACK
        // -------------------------------
        feedbackOption.setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, FeedbackActivity.class))
        );

        // -------------------------------
        // ACCOUNT SETTINGS
        // -------------------------------
        accountSettingsOption.setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, UpdateInfoActivity.class))
        );

        // -------------------------------
        // RESET PROGRESS
        // -------------------------------
        resetProgressOption.setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, ResetAccountActivity.class))
        );

        // -------------------------------
        // SIGN OUT
        // -------------------------------
        signOutButton.setOnClickListener(v -> {

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            Toast.makeText(ProfileActivity.this,
                    "Signed out", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    // ----------------------------------------------------------------
    // Network: fetch profile from backend, update UI and cache locally
    // ----------------------------------------------------------------
    private void fetchProfileFromBackend() {

        String email = sharedPreferences.getString("email", "");

        if (email == null || email.isEmpty()) {
            // no email saved — keep local fallback values
            return;
        }

        ApiService api = ApiClient.getClient().create(ApiService.class);

        Call<UserProfileResponse> call = api.getUserProfile(email);
        call.enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(Call<UserProfileResponse> call,
                                   Response<UserProfileResponse> response) {

                if (!response.isSuccessful() || response.body() == null) {
                    // keep local cached values if any
                    String cachedAge = sharedPreferences.getString("age", "--");
                    String cachedGender = sharedPreferences.getString("gender", "--");
                    userInfo.setText("Age: " + cachedAge + ", " + cachedGender);
                    return;
                }

                UserProfileResponse user = response.body();

                String name = user.getName() != null ? user.getName() : sharedPreferences.getString("user_id", "User");
                String age = user.getAge() != null ? user.getAge() : sharedPreferences.getString("age", "--");
                String gender = user.getGender() != null ? user.getGender() : sharedPreferences.getString("gender", "--");

                // Update UI
                userName.setText(name);
                userInfo.setText("Age: " + age + ", " + gender);

                // Cache locally
                sharedPreferences.edit()
                        .putString("user_id", name)
                        .putString("age", age)
                        .putString("gender", gender)
                        .apply();
            }

            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                // network fail — show cached values if present
                String cachedAge = sharedPreferences.getString("age", "--");
                String cachedGender = sharedPreferences.getString("gender", "--");
                userInfo.setText("Age: " + cachedAge + ", " + cachedGender);
                // optionally show a toast (comment/uncomment as you prefer)
                // Toast.makeText(ProfileActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
