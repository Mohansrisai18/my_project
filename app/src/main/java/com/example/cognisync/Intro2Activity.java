package com.example.cognisync;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class Intro2Activity extends AppCompatActivity {

    private AppCompatButton btnPreviousIntro;
    private AppCompatButton btnNextIntro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(getSupportActionBar()!=null){
            getSupportActionBar().hide();
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro2);

        btnPreviousIntro = findViewById(R.id.btnPreviousIntro);
        btnNextIntro     = findViewById(R.id.btnNextIntro);

        btnPreviousIntro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToIntro1();
            }
        });

        btnNextIntro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToIntro3();
            }
        });

        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        navigateToIntro1();
                    }
                }
        );
    }

    private void navigateToIntro1() {
        Intent intent = new Intent(Intro2Activity.this, Intro1Activity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        finish();
    }

    private void navigateToIntro3() {
        Intent intent = new Intent(Intro2Activity.this, Intro3Activity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        finish();
    }
}
