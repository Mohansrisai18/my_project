//package com.example.cognisync;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.example.cognisync.del.ApiClient;
//import com.example.cognisync.del.ApiService;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//
//public class ForgotPasswordActivity extends AppCompatActivity {
//
//    private EditText emailInput;
//    private Button sendOtpBtn;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_forgot_password);
//
//        emailInput = findViewById(R.id.emailInput);
//        sendOtpBtn = findViewById(R.id.sendOtpBtn);
//
//        sendOtpBtn.setOnClickListener(v -> sendOtp());
//    }
//
//    private void sendOtp() {
//        String email = emailInput.getText().toString().trim();
//
//        if (email.isEmpty()) {
//            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        ApiService api = ApiClient.getClient().create(ApiService.class);
//
//        Map<String, String> data = new HashMap<>();
//        data.put("email", email);
//
//        api.sendOtp(data).enqueue(new Callback<Void>() {
//            @Override
//            public void onResponse(Call<Void> call, Response<Void> response) {
//                if (response.isSuccessful()) {
//
//                    Intent i = new Intent(ForgotPasswordActivity.this, OtpVerifyActivity.class);
//                    i.putExtra("email", email);
//                    startActivity(i);
//
//                } else {
//                    Toast.makeText(ForgotPasswordActivity.this, "Email not registered", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<Void> call, Throwable t) {
//                Toast.makeText(ForgotPasswordActivity.this, "Network error", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//}


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

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText emailInput;
    private Button sendOtpBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (getSupportActionBar() != null) getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        emailInput = findViewById(R.id.emailInput);
        sendOtpBtn = findViewById(R.id.sendOtpBtn);

        sendOtpBtn.setOnClickListener(v -> sendOtp());
    }

    // ===============================
    // SEND OTP
    // ===============================
    private void sendOtp() {

        String email = emailInput.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }

        sendOtpBtn.setEnabled(false);
        sendOtpBtn.setText("Sending OTP...");

        ApiService api = ApiClient.getClient().create(ApiService.class);

        Map<String, String> body = new HashMap<>();
        body.put("email", email);

        api.sendOtp(body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {

                sendOtpBtn.setEnabled(true);
                sendOtpBtn.setText("Send OTP");

                if (response.isSuccessful()) {

                    Toast.makeText(
                            ForgotPasswordActivity.this,
                            "OTP sent to your email",
                            Toast.LENGTH_SHORT
                    ).show();

                    Intent intent = new Intent(
                            ForgotPasswordActivity.this,
                            OtpVerifyActivity.class
                    );
                    intent.putExtra("email", email);
                    startActivity(intent);

                } else {
                    Toast.makeText(
                            ForgotPasswordActivity.this,
                            "Email not registered",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {

                sendOtpBtn.setEnabled(true);
                sendOtpBtn.setText("Send OTP");

                Toast.makeText(
                        ForgotPasswordActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }
}
