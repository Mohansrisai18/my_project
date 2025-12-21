package com.example.cognisync;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cognisync.del.ApiClient;
import com.example.cognisync.del.ApiService;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText newPassInput, confirmPassInput;
    private Button resetBtn;

    private String email, otp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (getSupportActionBar() != null) getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        newPassInput = findViewById(R.id.newPasswordInput);
        confirmPassInput = findViewById(R.id.confirmPasswordInput);
        resetBtn = findViewById(R.id.resetBtn);

        email = getIntent().getStringExtra("email");
        otp = getIntent().getStringExtra("otp");

        if (email == null || otp == null) {
            Toast.makeText(this, "Invalid reset request", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        resetBtn.setOnClickListener(v -> resetPassword());
    }

    private void resetPassword() {

        String pass1 = newPassInput.getText().toString().trim();
        String pass2 = confirmPassInput.getText().toString().trim();

        if (TextUtils.isEmpty(pass1) || TextUtils.isEmpty(pass2)) {
            Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!pass1.equals(pass2)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        resetBtn.setEnabled(false);
        resetBtn.setText("Updating...");

        ApiService api = ApiClient.getClient().create(ApiService.class);

        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("otp", otp);               // ✅ REQUIRED
        body.put("new_password", pass1);

        api.resetPassword(body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {

                resetBtn.setEnabled(true);
                resetBtn.setText("Reset Password");

                if (response.code() == 200) {

                    Toast.makeText(
                            ResetPasswordActivity.this,
                            "Password updated successfully",
                            Toast.LENGTH_SHORT
                    ).show();

                    Intent i = new Intent(
                            ResetPasswordActivity.this,
                            LoginActivity.class
                    );
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    finish();

                } else {
                    Toast.makeText(
                            ResetPasswordActivity.this,
                            "Reset failed (" + response.code() + ")",
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {

                resetBtn.setEnabled(true);
                resetBtn.setText("Reset Password");

                Toast.makeText(
                        ResetPasswordActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }
}
