package com.example.cognisync;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import org.json.JSONObject;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {

    private EditText mailInput, passwordInput;
    private AppCompatButton btnLogin;
    private TextView signupText;

    private static final String LOGIN_URL = "http://10.0.2.2:8000/user/login/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mailInput = findViewById(R.id.mailInput);
        passwordInput = findViewById(R.id.passwordInput);
        btnLogin = findViewById(R.id.btnLogin);
        signupText = findViewById(R.id.signupText);

        btnLogin.setOnClickListener(v -> performLogin());
        signupText.setOnClickListener(v -> navigateToSignup());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    private void performLogin() {
        String email = mailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Logging in...");

        new LoginTask(email, password).execute();
    }

    private class LoginTask extends AsyncTask<Void, Void, String> {
        private final String email, password;

        LoginTask(String email, String password) {
            this.email = email;
            this.password = password;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(LOGIN_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("email", email);
                jsonParam.put("password", password);

                OutputStream os = new BufferedOutputStream(conn.getOutputStream());
                os.write(jsonParam.toString().getBytes());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        responseCode == 200 ? conn.getInputStream() : conn.getErrorStream()
                ));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                conn.disconnect();
                return response.toString();

            } catch (Exception e) {
                return "Error: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            btnLogin.setEnabled(true);
            btnLogin.setText("Login");

            if (result.contains("Login successful")) {
                Toast.makeText(LoginActivity.this, "Welcome back!", Toast.LENGTH_SHORT).show();
                navigateToHome();
            } else {
                Toast.makeText(LoginActivity.this, "Login failed: " + result, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void navigateToSignup() {
        Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToHome() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}
