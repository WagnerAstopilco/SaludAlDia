package com.example.saludaldia.ui.notification;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;


import com.example.saludaldia.R;
import com.example.saludaldia.ui.toolbar.AdultToolbar;

public class NotificationsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        AdultToolbar.setup(this);
        if (this.getSupportActionBar() != null) {
            this.getSupportActionBar().setTitle("Notificaciones");
        }
    }
}