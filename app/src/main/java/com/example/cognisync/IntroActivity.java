package com.example.cognisync;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

public class IntroActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private IntroPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        viewPager = findViewById(R.id.viewPager);
        adapter = new IntroPagerAdapter(this);
        viewPager.setAdapter(adapter);

        viewPager.setOffscreenPageLimit(3);

        // 🔥 Prevent overlap completely
        viewPager.setClipToPadding(false);
        viewPager.setClipChildren(false);
        viewPager.getChildAt(0).setOverScrollMode(ViewPager2.OVER_SCROLL_NEVER);

        // 🔥 Simple clean transformer (No overlapping)
        viewPager.setPageTransformer((page, position) -> {

            float alpha = 1 - Math.abs(position);
            page.setAlpha(alpha);

            float scale = 0.9f + (1 - Math.abs(position)) * 0.1f;
            page.setScaleX(scale);
            page.setScaleY(scale);

            // IMPORTANT: Remove translation effect
            page.setTranslationX(0);
        });
    }
}
