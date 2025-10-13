package com.example.cognisync;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.components.AxisBase;

import java.util.ArrayList;
import java.util.List;

public class ProgressDashboardActivity extends AppCompatActivity {

    private TextView tvAttentionScore, tvMemoryScore, tvEmotionScore;
    private LineChart progressChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress_dashboard);

        tvAttentionScore = findViewById(R.id.tvAttentionScore);
        tvMemoryScore = findViewById(R.id.tvMemoryScore);
        tvEmotionScore = findViewById(R.id.tvEmotionScore);
        progressChart = findViewById(R.id.progressChart);

        displayDashboardScores();
        setupProgressChart();
    }

    // Calculates and shows each dashboard "aspect" from module-level scores, as required
    private void displayDashboardScores() {
        SharedPreferences sp = getSharedPreferences("AssessmentScores", MODE_PRIVATE);

        float focusedScore = sp.getFloat("focused_attention_score", 0f);
        float presentScore = sp.getFloat("present_moment_score", 0f);
        float workingScore = sp.getFloat("working_memory_score", 0f);
        float cognitiveScore = sp.getFloat("cognitive_integration_score", 0f);
        float emotionalScore = sp.getFloat("emotional_regulation_score", 0f);

        // Logic: categories are averages of listed modules
        float attention = avgNonZero(focusedScore, presentScore);
        float memory = avgNonZero(workingScore, cognitiveScore);
        float emotion = emotionalScore; // Only one module for emotional

        tvAttentionScore.setText(String.format("%.1f/7", attention));
        tvMemoryScore.setText(String.format("%.1f/7", memory));
        tvEmotionScore.setText(String.format("%.1f/7", emotion));
    }

    // Average (ignoring zero/unset scores)
    private float avgNonZero(float... vals) {
        float sum = 0;
        int count = 0;
        for (float v : vals) {
            if (v > 0) {
                sum += v;
                count++;
            }
        }
        return count > 0 ? sum / count : 0;
    }

    // Simple hardcoded monthly data demo for the line chart; replace with history!
    private void setupProgressChart() {
        List<Entry> attEntries = new ArrayList<>();
        attEntries.add(new Entry(1, 5.2f));
        attEntries.add(new Entry(2, 5.7f));
        attEntries.add(new Entry(3, 6.2f));

        List<Entry> memEntries = new ArrayList<>();
        memEntries.add(new Entry(1, 4.8f));
        memEntries.add(new Entry(2, 5.5f));
        memEntries.add(new Entry(3, 6.3f));

        List<Entry> emoEntries = new ArrayList<>();
        emoEntries.add(new Entry(1, 5.0f));
        emoEntries.add(new Entry(2, 5.5f));
        emoEntries.add(new Entry(3, 5.8f));

        LineDataSet attSet = new LineDataSet(attEntries, "Attention");
        attSet.setColor(0xFFAE8DF6); attSet.setCircleColor(0xFFAE8DF6); attSet.setLineWidth(2f);

        LineDataSet memSet = new LineDataSet(memEntries, "Memory");
        memSet.setColor(0xFF72D277); memSet.setCircleColor(0xFF72D277); memSet.setLineWidth(2f);

        LineDataSet emoSet = new LineDataSet(emoEntries, "Emotion");
        emoSet.setColor(0xFFF6AEDC); emoSet.setCircleColor(0xFFF6AEDC); emoSet.setLineWidth(2f);

        LineData lineData = new LineData(attSet, memSet, emoSet);
        progressChart.setData(lineData);

        XAxis xAxis = progressChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                return "mth " + ((int) value);
            }
        });

        Legend legend = progressChart.getLegend();
        legend.setEnabled(true);

        progressChart.getDescription().setEnabled(false);
        progressChart.invalidate();
    }
}
