package com.example.saludaldia.ui.caregiver;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.example.saludaldia.R;
import com.example.saludaldia.adapter.TreatmentAdapter;
import com.example.saludaldia.data.model.Medication;
import com.example.saludaldia.data.model.Treatment;
import com.example.saludaldia.data.repository.MedicationRepository;
import com.example.saludaldia.data.repository.TreatmentRepository;
import com.example.saludaldia.ui.toolbar.CaregiverToolbar;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LinkedUserTreatmentsActivity extends AppCompatActivity {
    private TreatmentAdapter adapter;
    private RecyclerView recyclerView;
    private String linkedUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linked_user_treatments);
        CaregiverToolbar.setup(this);
        if (getIntent().hasExtra("linkedUserId")) {
            linkedUserId = getIntent().getStringExtra("linkedUserId");
            String linkedUserName = getIntent().getStringExtra("linkedUserName");

            if (getSupportActionBar() != null) {
                if (linkedUserName != null && !linkedUserName.isEmpty()) {
                    getSupportActionBar().setTitle("Tratamientos de " + linkedUserName);
                } else {
                    getSupportActionBar().setTitle("Tratamientos del Usuario");
                }
            }

            recyclerView = findViewById(com.example.saludaldia.R.id.treatmentRecyclerView);
            Map<String, List<Medication>> medicationsMap = new HashMap<>();

            adapter = new TreatmentAdapter(new ArrayList<>(), medicationsMap, false);
            recyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);

            loadTreatments(linkedUserId);

        } else {
            Toast.makeText(this, "Error: No se recibió el ID del usuario vinculado para cargar tratamientos.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void loadTreatments(String userId) {
        TreatmentRepository.getTreatmentsForLinkedUser(userId, treatments -> {
            Map<String, List<Medication>> medicationsMap = new HashMap<>();

            if (treatments.isEmpty()) {
                adapter.updateList(treatments, medicationsMap);
                return;
            }

            final int total = treatments.size();
            final int[] loadedCount = {0};

            for (Treatment treatment : treatments) {
                MedicationRepository.getMedicationsByTreatmentId(treatment.getTreatmentId(),
                        meds -> {
                            medicationsMap.put(treatment.getTreatmentId(), meds);
                            loadedCount[0]++;
                            if (loadedCount[0] == total) {
                                adapter.updateList(treatments, medicationsMap);
                            }
                        },
                        e -> {
                            loadedCount[0]++;
                            if (loadedCount[0] == total) {
                                adapter.updateList(treatments, medicationsMap);
                            }
                            Log.e("loadTreatments", "Error al cargar medicamentos para tratamiento " + treatment.getTreatmentId(), e);
                        }
                );
            }
        }, e -> {
            Toast.makeText(this, "Error al cargar tratamientos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("loadTreatments", "Error al cargar tratamientos generales", e);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (linkedUserId != null) {
            loadTreatments(linkedUserId);
        }
    }
}


