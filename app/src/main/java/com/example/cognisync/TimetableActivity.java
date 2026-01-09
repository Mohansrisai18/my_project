package com.example.cognisync;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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

        // Use the default ActionBar provided by the app theme (Option A)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Your 21-Day Timetable");
        }

        // ---------------- LOAD SAVED TIMETABLE ----------------
        String json = TimetableStore.load(this);
        if (json == null) {
            Toast.makeText(this, "No timetable found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        MLPredictResponse response = new Gson().fromJson(json, MLPredictResponse.class);
        if (response == null || response.timetable == null) {
            Toast.makeText(this, "Invalid timetable", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // ---------------- RECYCLER VIEW ----------------
        RecyclerView recyclerView = findViewById(R.id.recyclerTimetable);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new TimetableListAdapter(response.timetable));

        // ---------------- DONE BUTTON ----------------
        Button btnDone = findViewById(R.id.btnDone);
        btnDone.setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });
    }

    // ---------------- MENU ----------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_timetable, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_regenerate) {
            TimetableStore.clear(this);
            Intent i = new Intent(this, TaskAActivity.class);
            i.putExtra("REGENERATE", true);
            startActivity(i);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
