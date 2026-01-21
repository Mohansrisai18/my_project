package com.example.cognisync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
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

import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private ImageButton backButton;
    private Button signOutButton;
    private LinearLayout accountSettingsOption, resetProgressOption;

    private TextView userName, userInfo;
    private TextView quoteText, quoteAuthor;

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "UserPrefs";

    // Mental-health focused quotes with clear authors only
    private final String[][] QUOTES = new String[][] {
            {"What mental health needs is more sunlight, more candor, and more unashamed conversation.", "Glenn Close"},
            {"You don't have to control your thoughts. You just have to stop letting them control you.", "Dan Millman"},
            {"Owning our story and loving ourselves through that process is the bravest thing we'll ever do.", "Brené Brown"},
            {"When we are no longer able to change a situation, we are challenged to change ourselves.", "Viktor E. Frankl"},
            {"I am not what happened to me, I am what I choose to become.", "Carl Jung"},
            {"The wound is the place where the Light enters you.", "Rumi"},
            {"You may not control all the events that happen to you, but you can decide not to be reduced by them.", "Maya Angelou"},
            {"The greatest glory in living lies not in never falling, but in rising every time we fall.", "Nelson Mandela"},
            {"Self-care is giving the world the best of you, instead of what's left of you.", "Katie Reed"},
            {"Start where you are. Use what you have. Do what you can.", "Arthur Ashe"}
    };

    private final Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Hide action bar
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // Light status bar icons (optional)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            getWindow().getDecorView()
                    .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        // Ensure status bar color matches header (safe on Lollipop+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // -------------------------------
        // INIT VIEWS (MATCH XML)
        // -------------------------------
        backButton = findViewById(R.id.backButton);
        signOutButton = findViewById(R.id.signOutButton);

        accountSettingsOption = findViewById(R.id.accountSettingsOption);
        resetProgressOption = findViewById(R.id.resetProgressOption);

        userName = findViewById(R.id.userName);
        userInfo = findViewById(R.id.userInfo);

        quoteText = findViewById(R.id.quoteText);
        quoteAuthor = findViewById(R.id.quoteAuthor);

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

        // -------------------------------
        // QUOTE: load dynamic quote on every start
        // -------------------------------
        loadRandomQuoteWithFade();

        // Optional: allow tap on quote to get a fresh one
        quoteText.setOnClickListener(v -> loadRandomQuoteWithFade());
        quoteAuthor.setOnClickListener(v -> loadRandomQuoteWithFade());
    }

    private void loadRandomQuoteWithFade() {
        int idx = random.nextInt(QUOTES.length);
        String quote = QUOTES[idx][0];
        String author = QUOTES[idx][1];

        // set text immediately with initial transparency
        quoteText.setAlpha(0f);
        quoteAuthor.setAlpha(0f);

        quoteText.setText("“" + quote + "”");
        quoteAuthor.setText(author == null || author.trim().isEmpty() ? "" : ("— " + author));

        // fade in
        quoteText.animate().alpha(1f).setDuration(420).start();
        quoteAuthor.animate().alpha(1f).setDuration(480).start();
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
