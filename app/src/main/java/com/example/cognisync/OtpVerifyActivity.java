package com.example.cognisync;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cognisync.del.ApiClient;
import com.example.cognisync.del.ApiService;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OtpVerifyActivity extends AppCompatActivity {

    private EditText otpInput;
    private Button verifyBtn;
    private TextView resendOtp;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (getSupportActionBar() != null) getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verify);

        otpInput = findViewById(R.id.otpInput);
        verifyBtn = findViewById(R.id.verifyBtn);
        resendOtp = findViewById(R.id.resendOtp);

        email = getIntent().getStringExtra("email");

        if (email == null) {
            Toast.makeText(this, "Invalid request", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        verifyBtn.setOnClickListener(v -> verifyOtp());
        resendOtp.setOnClickListener(v -> resendOtp());
    }

    // ===============================
    // VERIFY OTP
    // ===============================
    private void verifyOtp() {

        String otp = otpInput.getText().toString().trim();

        if (TextUtils.isEmpty(otp)) {
            Toast.makeText(this, "Please enter OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        verifyBtn.setEnabled(false);
        verifyBtn.setText("Verifying...");

        ApiService api = ApiClient.getClient().create(ApiService.class);

        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("otp", otp);

        api.verifyOtp(body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {

                verifyBtn.setEnabled(true);
                verifyBtn.setText("Verify");

                if (response.code() == 200) {

                    // 🔥 VERY IMPORTANT
                    resendOtp.setEnabled(false);

                    Toast.makeText(
                            OtpVerifyActivity.this,
                            "OTP verified",
                            Toast.LENGTH_SHORT
                    ).show();

                    Intent intent = new Intent(
                            OtpVerifyActivity.this,
                            ResetPasswordActivity.class
                    );
                    intent.putExtra("email", email);
                    intent.putExtra("otp", otp); // ✅ PASS SAME OTP
                    startActivity(intent);
                    finish();

                } else {
                    Toast.makeText(
                            OtpVerifyActivity.this,
                            "Invalid OTP",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {

                verifyBtn.setEnabled(true);
                verifyBtn.setText("Verify");

                Toast.makeText(
                        OtpVerifyActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    // ===============================
    // RESEND OTP
    // ===============================
    private void resendOtp() {

        ApiService api = ApiClient.getClient().create(ApiService.class);

        Map<String, String> body = new HashMap<>();
        body.put("email", email);

        api.sendOtp(body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {

                if (response.code() == 200) {
                    Toast.makeText(
                            OtpVerifyActivity.this,
                            "OTP resent to your email",
                            Toast.LENGTH_SHORT
                    ).show();
                } else {
                    Toast.makeText(
                            OtpVerifyActivity.this,
                            "Unable to resend OTP",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(
                        OtpVerifyActivity.this,
                        "Network error",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }
}
