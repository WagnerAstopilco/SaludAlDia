package com.example.saludaldia.ui.treatment;

import static android.content.ContentValues.TAG;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.saludaldia.R;
import com.example.saludaldia.data.model.Medication;
import com.example.saludaldia.data.model.Treatment;
import com.example.saludaldia.data.repository.TreatmentRepository;
import com.example.saludaldia.adapter.MedicationAdapter;
import com.example.saludaldia.ui.medication.AddMedicationActivity;
import com.example.saludaldia.ui.toolbar.AdultToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TreatmentDetailsActivity extends AppCompatActivity {

    private EditText editTextName, editTextStartDate, editTextEndDate, editTextDescription;
    private Button btnEdit, btnSave, btnCancel;
    private RecyclerView recyclerMedications;
    private FloatingActionButton fabAddMedication;
    private Switch switchState;

    private Treatment currentTreatment;
    private List<Medication> medicationList = new ArrayList<>();
    private MedicationAdapter medicationAdapter;

    private boolean isEditing = false;

    private FirebaseFirestore db;
    private TreatmentRepository treatmentRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_treatment_details);

        AdultToolbar.setup(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.treatment_details_activity_title));
        }

        db = FirebaseFirestore.getInstance();
        treatmentRepository = new TreatmentRepository();

        initViews();
        setupListeners();

        String treatmentId = getIntent().getStringExtra("treatmentId");
        if (treatmentId != null) {
            loadTreatmentData(treatmentId);
        } else {
            Toast.makeText(this, "ID de tratamiento no proporcionado", Toast.LENGTH_SHORT).show();
            finish();
        }

        setFieldsEditable(false);
    }

    private void initViews() {
        editTextName = findViewById(R.id.editTextName);
        editTextStartDate = findViewById(R.id.editTextStartDate);
        editTextEndDate = findViewById(R.id.editTextEndDate);
        editTextDescription = findViewById(R.id.editTextDescription);
        switchState = findViewById(R.id.switchState);
        btnEdit = findViewById(R.id.btnEdit);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        recyclerMedications = findViewById(R.id.recyclerMedications);
        recyclerMedications.setLayoutManager(new LinearLayoutManager(this));

        fabAddMedication = findViewById(R.id.fabAddMedication);
        medicationAdapter = new MedicationAdapter(medicationList);
        recyclerMedications.setAdapter(medicationAdapter);
    }

    private void setupListeners() {
        btnEdit.setOnClickListener(v -> {
            setFieldsEditable(true);
            btnEdit.setVisibility(View.GONE);
            btnSave.setVisibility(View.VISIBLE);
            btnCancel.setVisibility(View.VISIBLE);
        });

        btnCancel.setOnClickListener(v -> {
            setFieldsEditable(false);
            btnEdit.setVisibility(View.VISIBLE);
            btnSave.setVisibility(View.GONE);
            btnCancel.setVisibility(View.GONE);
        });

        btnSave.setOnClickListener(v -> {
            updateTreatment();
            setFieldsEditable(false);
            btnEdit.setVisibility(View.VISIBLE);
            btnSave.setVisibility(View.GONE);
            btnCancel.setVisibility(View.GONE);
        });

        fabAddMedication.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddMedicationActivity.class);
            intent.putExtra("treatmentId", currentTreatment.getTreatmentId());
            startActivity(intent);
        });
        switchState.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                switchState.setText("Activo");
            } else {
                switchState.setText("Inactivo");
            }
        });
    }

    private void setFieldsEditable(boolean enabled) {
        editTextName.setEnabled(enabled);
        editTextStartDate.setEnabled(enabled);
        editTextEndDate.setEnabled(enabled);
        editTextDescription.setEnabled(enabled);
        switchState.setEnabled(enabled);
        isEditing = enabled;
    }

    private void loadTreatmentData(String treatmentId) {
        db.collection("treatments").document(treatmentId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentTreatment = documentSnapshot.toObject(Treatment.class);
                        if (currentTreatment != null) {
                            currentTreatment.setTreatmentId(documentSnapshot.getId());
                            populateTreatmentData();
                            loadMedications();
                        }
                    } else {
                        Toast.makeText(this, "Tratamiento no encontrado", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar tratamiento", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void populateTreatmentData() {
        editTextName.setText(currentTreatment.getName());

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        if (currentTreatment.getStartDate() != null) {
            editTextStartDate.setText(dateFormat.format(currentTreatment.getStartDate()));
        }

        if (currentTreatment.getEndDate() != null) {
            editTextEndDate.setText(dateFormat.format(currentTreatment.getEndDate()));
        }

        editTextDescription.setText(currentTreatment.getDescription());

        boolean isActive = "activo".equals(currentTreatment.getState());
        switchState.setChecked(isActive);
        Log.d(TAG,"estado del treat: "+isActive);
        switchState.setText(isActive ? "Activo" : "Inactivo");
    }

    private void updateTreatment() {
        String name = editTextName.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        String startDateStr = editTextStartDate.getText().toString().trim();
        String endDateStr = editTextEndDate.getText().toString().trim();
        String state = switchState.isChecked() ? "Activo" : "Inactivo";

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        try {
            Date startDate = dateFormat.parse(startDateStr);
            Date endDate = dateFormat.parse(endDateStr);

            currentTreatment.setName(name);
            currentTreatment.setDescription(description);
            currentTreatment.setStartDate(startDate);
            currentTreatment.setEndDate(endDate);
            currentTreatment.setState(state);

            db.collection("treatments").document(currentTreatment.getTreatmentId())
                    .set(currentTreatment)
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(this, "Tratamiento actualizado", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error al actualizar tratamiento", Toast.LENGTH_SHORT).show());

        } catch (ParseException e) {
            Toast.makeText(this, "Formato de fecha inválido. Usa dd/MM/yyyy", Toast.LENGTH_LONG).show();
        }
    }


    private void loadMedications() {
        db.collection("medications")
                .whereEqualTo("treatmentId", currentTreatment.getTreatmentId())
                .orderBy("name")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Error al cargar medicamentos", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    medicationList.clear();
                    for (var document : queryDocumentSnapshots) {
                        Medication medication = document.toObject(Medication.class);
                        medication.setMedicationId(document.getId());
                        medicationList.add(medication);
                    }

                    medicationAdapter.notifyDataSetChanged();

                });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentTreatment != null) {
            loadMedications();
        }
    }
}
