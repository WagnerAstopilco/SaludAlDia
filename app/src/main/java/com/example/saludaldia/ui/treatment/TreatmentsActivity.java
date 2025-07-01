package com.example.saludaldia.ui.treatment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.saludaldia.R;
import com.example.saludaldia.adapter.TreatmentAdapter;
import com.example.saludaldia.data.model.Medication;
import com.example.saludaldia.data.model.Treatment;
import com.example.saludaldia.data.repository.MedicationRepository;
import com.example.saludaldia.data.repository.TreatmentRepository;
import com.example.saludaldia.ui.toolbar.AdultToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TreatmentsActivity extends AppCompatActivity {

    private static final int NEW_TREATMENT_REQUEST_CODE = 100;
    private TreatmentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_treatments);
        AdultToolbar.setup(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.treatments_activity_title));
        }
        RecyclerView recyclerView = findViewById(R.id.treatmentRecyclerView);

        Map<String, List<Medication>> medicationsMap = new HashMap<>();

        adapter = new TreatmentAdapter(new ArrayList<>(), medicationsMap, false);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton fab = findViewById(R.id.fabAddTreatment);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(TreatmentsActivity.this, NewTreatmentActivity.class);
            startActivityForResult(intent, NEW_TREATMENT_REQUEST_CODE);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTreatments();
    }

    private void loadTreatments() {
        TreatmentRepository.getTreatmentsForCurrentUser(treatments -> {
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
                            Log.e("loadTreatments", "Error al cargar medicamentos", e);
                        }
                );
            }
        }, e -> {
            Toast.makeText(this, "Error al cargar tratamientos", Toast.LENGTH_SHORT).show();
        });
    }

}
