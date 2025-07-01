package com.example.saludaldia.ui.adult;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.saludaldia.R;
import com.example.saludaldia.adapter.TreatmentAdapter;
import com.example.saludaldia.data.model.Medication;
import com.example.saludaldia.data.model.Treatment;
import com.example.saludaldia.data.repository.MedicationRepository;
import com.example.saludaldia.data.repository.TreatmentRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TreatmentListFragment extends Fragment {
    private TreatmentAdapter adapter;

    public TreatmentListFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_treatment_list, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.treatmentRecyclerView);
        Map<String, List<Medication>> medicationsMap = new HashMap<>();
        adapter = new TreatmentAdapter(new ArrayList<>(), medicationsMap, true);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext())); // Usar getContext() para el fragmento

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTreatments();
    }

    private void loadTreatments() {
        TreatmentRepository.getActiveTreatmentsForCurrentUser(treatments -> {
            Map<String, List<Medication>> medicationsMap = new HashMap<>();

            if (treatments.isEmpty()) {
                if (adapter != null) {
                    adapter.updateList(treatments, medicationsMap);
                }
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
                                // Cuando se han cargado todos los medicamentos
                                if (adapter != null) {
                                    adapter.updateList(treatments, medicationsMap);
                                }
                            }
                        },
                        e -> {
                            loadedCount[0]++;
                            if (loadedCount[0] == total) {
                                if (adapter != null) {
                                    adapter.updateList(treatments, medicationsMap);
                                }
                            }
                            Log.e("TreatmentListFragment", "Error al cargar medicamentos", e);
                        }
                );
            }

        }, e -> {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error al cargar tratamientos", Toast.LENGTH_SHORT).show();
            }
            Log.e("TreatmentListFragment", "Error al cargar tratamientos", e);
        });
    }
}
