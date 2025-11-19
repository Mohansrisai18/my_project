package com.example.cognisync;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScoreUploader {

    public static void uploadScore(Context ctx,
                                   String email,
                                   float score,
                                   String endpointName,
                                   Call<Void> apiCall) {

        apiCall.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Log.d("SCORE_UPLOAD", endpointName + " success: " + response.code());
                Toast.makeText(ctx, endpointName + " saved!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("SCORE_UPLOAD", endpointName + " FAILED: " + t.getMessage());
                Toast.makeText(ctx, "Failed to save score: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
