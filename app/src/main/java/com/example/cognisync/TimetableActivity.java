package com.example.cognisync;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cognisync.model.MLPredictResponse;
import com.example.cognisync.util.TimetableStore;
import com.google.gson.Gson;

public class TimetableActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable);

        // -------------------------
        // RecyclerView setup
        // -------------------------
        RecyclerView recyclerView = findViewById(R.id.recyclerTimetable);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        String json = TimetableStore.load(this);

        if (json == null) {
            Toast.makeText(this, "No timetable found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        MLPredictResponse response =
                new Gson().fromJson(json, MLPredictResponse.class);

        recyclerView.setAdapter(
                new TimetableListAdapter(response.timetable)
        );

        // -------------------------
        // ✅ DONE BUTTON HANDLER
        // -------------------------
        Button btnDone = findViewById(R.id.btnDone);
        btnDone.setOnClickListener(v -> {
            startActivity(new Intent(TimetableActivity.this, HomeActivity.class));
            finish();
        });
    }
}
