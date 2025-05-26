package com.example.saludaldia.ui.medication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.saludaldia.R;
import com.example.saludaldia.data.model.Medication;
import com.example.saludaldia.data.model.Reminder;
import com.example.saludaldia.data.repository.MedicationRepository;
import com.example.saludaldia.ui.toolbar.AdultToolbar;

import java.util.UUID;

public class AddMedicationActivity extends AppCompatActivity {

    private EditText etName, etDose, etNotes;
    private Switch switchActive;
    private Reminder reminder = null;

    private String treatmentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medication);
        AdultToolbar.setup(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Nuevo medicamento");
        }
        etName = findViewById(R.id.etMedicationName);
        etDose = findViewById(R.id.etMedicationDose);
        etNotes = findViewById(R.id.etMedicationNotes);
        switchActive = findViewById(R.id.switchActive);
        Button btnSave = findViewById(R.id.btnSaveMedication);
        Button btnCancel = findViewById(R.id.btnCancelMedication);

        treatmentId = getIntent().getStringExtra("treatmentId");
        if (treatmentId == null) {
            Toast.makeText(this, "Error: ID de tratamiento no encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }



        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String dose = etDose.getText().toString().trim();
            String notes = etNotes.getText().toString().trim();
            boolean isActive = switchActive.isChecked();

            if (name.isEmpty() || dose.isEmpty()) {
                Toast.makeText(this, "Nombre y dosis son obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            Medication medication = new Medication();
            medication.setMedicationId(UUID.randomUUID().toString());
            medication.setTreatmentId(treatmentId);
            medication.setName(name);
            medication.setDose(dose);
            medication.setNotes(notes);
            medication.setActive(isActive);
            medication.setReminder(reminder);

            MedicationRepository.addMedication(
                    treatmentId,
                    medication,
                    aVoid -> {
                        Toast.makeText(this, "Medicamento guardado", Toast.LENGTH_SHORT).show();
                        finish();
                    },
                    e -> Toast.makeText(this, "Error al guardar medicamento", Toast.LENGTH_SHORT).show()
            );

        });

        btnCancel.setOnClickListener(v -> finish());
    }
}
