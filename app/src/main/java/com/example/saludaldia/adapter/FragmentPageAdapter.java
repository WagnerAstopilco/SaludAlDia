package com.example.saludaldia.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.saludaldia.ui.adult.CalendarFragment;
import com.example.saludaldia.ui.adult.TreatmentListFragment;

public class FragmentPageAdapter extends FragmentStateAdapter {
    public FragmentPageAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new TreatmentListFragment();
            case 1:
                return new CalendarFragment();
            default:
                return new TreatmentListFragment(); // Fallback
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
