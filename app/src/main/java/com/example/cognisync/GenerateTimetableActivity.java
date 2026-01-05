package com.example.cognisync;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cognisync.del.ApiClient;
import com.example.cognisync.del.ApiService;
import com.example.cognisync.model.MLPredictRequest;
import com.example.cognisync.model.MLPredictResponse;
import com.example.cognisync.util.TimetableStore;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GenerateTimetableActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // No UI needed — this is a logic-only screen
        generateTimetable();
    }

    private void generateTimetable() {

        // 🔹 TODO: Replace these with REAL scores from Tasks A & B
        MLPredictRequest request = new MLPredictRequest(
                62,   // maas
                30,   // panas_pos
                12,   // panas_neg
                18,   // dass
                26,   // erq
                40    // phlms
        );

        ApiService apiService =
                ApiClient.getClient().create(ApiService.class);

        apiService.predictMentalState(request)
                .enqueue(new Callback<MLPredictResponse>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<MLPredictResponse> call,
                            @NonNull Response<MLPredictResponse> response
                    ) {
                        if (response.isSuccessful() && response.body() != null) {

                            // 1️⃣ Convert response → JSON
                            String json =
                                    new Gson().toJson(response.body());

                            // 2️⃣ Save timetable locally
                            TimetableStore.save(
                                    GenerateTimetableActivity.this,
                                    json
                            );

                            // 3️⃣ Open timetable screen
                            startActivity(
                                    new Intent(
                                            GenerateTimetableActivity.this,
                                            TimetableActivity.class
                                    )
                            );

                            finish();

                        } else {
                            Toast.makeText(
                                    GenerateTimetableActivity.this,
                                    "Failed to generate timetable",
                                    Toast.LENGTH_SHORT
                            ).show();
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<MLPredictResponse> call,
                            @NonNull Throwable t
                    ) {
                        Toast.makeText(
                                GenerateTimetableActivity.this,
                                "Network error",
                                Toast.LENGTH_SHORT
                        ).show();
                        t.printStackTrace();
                        finish();
                    }
                });
    }
}
