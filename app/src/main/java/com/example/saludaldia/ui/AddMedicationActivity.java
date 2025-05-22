package com.example.saludaldia.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.saludaldia.R;

public class AddMedicationActivity extends AppCompatActivity {

    private Button btnAddMedication;
    private Button btnViewTreatments;
    private Button btnLogout; // opcional si usas login

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_add_medication);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnAddMedication = findViewById(R.id.btnAddMedication);
        btnViewTreatments = findViewById(R.id.btnViewTreatments);
        btnLogout = findViewById(R.id.btnLogout); // opcional

        btnAddMedication.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddMedicationActivity.class);
            startActivity(intent);
        });

        btnViewTreatments.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TreatmentDetailActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            // Lógica de cierre de sesión si se usa autenticación
        });
    }
}