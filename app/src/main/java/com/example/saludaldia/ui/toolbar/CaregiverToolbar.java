package com.example.saludaldia.ui.toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.saludaldia.R;

public class CaregiverToolbar {
    public static void setup(AppCompatActivity activity) {
        Toolbar toolbar = activity.findViewById(R.id.toolbar_caregiver);
        activity.setSupportActionBar(toolbar);
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setTitle("Bienvenido");
        }
    }
}
