package com.example.cognisync;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cognisync.del.ApiClient;
import com.example.cognisync.del.ApiService;
import com.example.cognisync.model.TimetableResponse;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TimetableActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable);

        recyclerView = findViewById(R.id.recyclerTimetable);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        apiService = ApiClient.getClient().create(ApiService.class);

        Map<String, Float> scores = new HashMap<>();
        scores.put("maas", 32f);
        scores.put("panas_pos", 21f);
        scores.put("panas_neg", 29f);
        scores.put("dass", 44f);
        scores.put("erq", 27f);
        scores.put("phlms", 30f);

        apiService.getTimetable(scores)
                .enqueue(new Callback<TimetableResponse>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<TimetableResponse> call,
                            @NonNull Response<TimetableResponse> response
                    ) {
                        if (response.isSuccessful() && response.body() != null) {
                            TimetableAdapter adapter =
                                    new TimetableAdapter(
                                            response.body().getTimetable()
                                    );
                            recyclerView.setAdapter(adapter);
                        }
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<TimetableResponse> call,
                            @NonNull Throwable t
                    ) {
                        t.printStackTrace();
                    }
                });
    }
}
