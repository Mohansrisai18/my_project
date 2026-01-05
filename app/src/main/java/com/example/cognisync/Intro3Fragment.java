package com.example.cognisync;

import android.content.Intent;
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

        view.findViewById(R.id.btnBack).setOnClickListener(v ->
                pager.setCurrentItem(1, true)
        );

        view.findViewById(R.id.btnFinish).setOnClickListener(v -> {
            // ✅ START TASK FLOW (NOT HOME)
            startActivity(new Intent(requireActivity(), TaskAActivity.class));
            requireActivity().finish();
        });
    }
}
