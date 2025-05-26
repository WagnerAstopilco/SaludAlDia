package com.example.saludaldia.ui.toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.saludaldia.R;


public class AdultToolbar extends AppCompatActivity {
    public static void setup(AppCompatActivity activity) {
        Toolbar toolbar = activity.findViewById(R.id.toolbar_adult);
        activity.setSupportActionBar(toolbar);
//        if (activity.getSupportActionBar() != null) {
//            activity.getSupportActionBar().setTitle("Bienvenido");
//        }
    }

}
