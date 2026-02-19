package com.example.cognisync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

public class Intro3Fragment extends Fragment {

    @Override
    public View onCreateView(
            @NonNull android.view.LayoutInflater inflater,
            @Nullable android.view.ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_intro3, container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        ViewPager2 pager = requireActivity().findViewById(R.id.viewPager);

        // Back button → go to Intro2
        view.findViewById(R.id.btnBack).setOnClickListener(v ->
                pager.setCurrentItem(1, true)
        );

        // Finish button → Go to HomeActivity
        view.findViewById(R.id.btnFinish).setOnClickListener(v -> {

            // Optional: Save intro completed flag
            SharedPreferences sp = requireActivity()
                    .getSharedPreferences("UserPrefs", requireActivity().MODE_PRIVATE);

            sp.edit().putBoolean("intro_completed", true).apply();

            // Navigate to HomeActivity
            Intent intent = new Intent(requireActivity(), HomeActivity.class);

            // Clear intro from back stack
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
            requireActivity().finish();
        });
    }
}
