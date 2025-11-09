package com.example.cognisync;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.*;

public class TaskSwitchActivity extends AppCompatActivity {

    private TextView tvNumber, tvInstruction, tvResult;
    private Button btnLeft, btnRight;

    private Random random = new Random();
    private Handler handler = new Handler();

    private int trial = 0;
    private static final int TOTAL_TRIALS = 12;

    private long startTime;
    private long prevColor = -1;
    private List<Long> sameRuleTimes = new ArrayList<>();
    private List<Long> switchRuleTimes = new ArrayList<>();

    private int currentNumber;
    private int currentColor; // 0 = red (<5/>5), 1 = blue (odd/even)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(getSupportActionBar()!=null){
            getSupportActionBar().hide();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_switch);

        tvInstruction = findViewById(R.id.tvInstruction);
        tvNumber = findViewById(R.id.tvNumber);
        tvResult = findViewById(R.id.tvResult);
        btnLeft = findViewById(R.id.btnLeft);
        btnRight = findViewById(R.id.btnRight);

        btnLeft.setOnClickListener(v -> handleResponse(true));
        btnRight.setOnClickListener(v -> handleResponse(false));

        nextTrial();
    }

    private void nextTrial() {
        if (trial >= TOTAL_TRIALS) {
            showResult();
            return;
        }

        currentNumber = random.nextInt(9) + 1; // 1-9
        currentColor = random.nextBoolean() ? 0 : 1; // 0 = red, 1 = blue

        tvNumber.setText(String.valueOf(currentNumber));
        tvNumber.setTextColor(currentColor == 0 ? 0xFFFF0000 : 0xFF2196F3);

        startTime = System.currentTimeMillis();
        trial++;
    }

    private void handleResponse(boolean leftPressed) {
        long rt = System.currentTimeMillis() - startTime;

        // Determine if this trial is a "switch" from the previous color
        if (prevColor != -1) {
            if (prevColor == currentColor) sameRuleTimes.add(rt);
            else switchRuleTimes.add(rt);
        }

        prevColor = currentColor;

        // Basic feedback rule validation (optional)
        boolean isCorrect = false;
        if (currentColor == 0) { // RED rule
            isCorrect = (currentNumber < 5 && leftPressed) || (currentNumber > 5 && !leftPressed);
        } else { // BLUE rule
            isCorrect = ((currentNumber % 2 == 0) && leftPressed) || ((currentNumber % 2 != 0) && !leftPressed);
        }

        if (!isCorrect) {
            tvResult.setText("Incorrect!");
        } else {
            tvResult.setText("");
        }

        handler.postDelayed(this::nextTrial, 700);
    }

    private void showResult() {
        long avgSame = average(sameRuleTimes);
        long avgSwitch = average(switchRuleTimes);
        long switchCost = Math.abs(avgSwitch - avgSame);

        String interpretation;
        if (switchCost < 100)
            interpretation = "Excellent flexibility";
        else if (switchCost <= 250)
            interpretation = "Average";
        else
            interpretation = "Low flexibility";

        tvResult.setText("Switch Cost: " + switchCost + " ms\n" + interpretation);

        // TODO: Save `switchCost` → SharedPreferences as "switch_cost_ms"
    }

    private long average(List<Long> list) {
        if (list.isEmpty()) return 0;
        long sum = 0;
        for (long l : list) sum += l;
        return sum / list.size();
    }
}
