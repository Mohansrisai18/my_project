package com.example.cognisync;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class IntroPagerAdapter extends FragmentStateAdapter {

    public IntroPagerAdapter(@NonNull FragmentActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new Intro1Fragment();
            case 1: return new Intro2Fragment();
            default: return new Intro3Fragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
