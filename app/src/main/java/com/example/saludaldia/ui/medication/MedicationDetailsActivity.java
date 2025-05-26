package com.example.saludaldia.ui.medication;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.saludaldia.R;
import com.example.saludaldia.data.model.Medication;
import com.example.saludaldia.data.model.Reminder;
import com.example.saludaldia.data.repository.MedicationRepository;
import com.example.saludaldia.ui.ReminderActivity;
import com.example.saludaldia.ui.toolbar.AdultToolbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class MedicationDetailsActivity extends AppCompatActivity {

    private EditText etName, etDose, etNotes;
    private Button btnEditMedication, btnSaveMedication, btnCancelMedication;
    private TextView tvReminderInfo, tvStartDate , tvEndDate, tvRecurring , tvFrequency,tvHours, tvDays ;
    private Button btnEditReminder, btnSaveReminder, btnCancelReminder;

    private Medication medication;
    private Reminder reminderOriginal;
    private String treatmentId;
    private String reminderId;

    private boolean isEditingMedication = false;
    private boolean isEditingReminder = false;

    private String medicationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medication_details);
        AdultToolbar.setup(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Detalles del medicamento");
        }

        medicationId = getIntent().getStringExtra("medicationId");
        treatmentId = getIntent().getStringExtra("treatmentId");

        initViews();
        loadMedicationData();
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etDose = findViewById(R.id.etDose);
        etNotes = findViewById(R.id.etNotes);

        btnEditMedication = findViewById(R.id.btnEditMedication);
        btnSaveMedication = findViewById(R.id.btnSaveMedication);
        btnCancelMedication = findViewById(R.id.btnCancelMedication);

        tvReminderInfo = findViewById(R.id.tvReminderInfo);
        btnEditReminder = findViewById(R.id.btnEditReminder);
        tvStartDate = findViewById(R.id.tvStartDate);
        tvEndDate = findViewById(R.id.tvEndDate);
        tvRecurring = findViewById(R.id.tvRecurring);
        tvFrequency = findViewById(R.id.tvFrequency);
        tvHours = findViewById(R.id.tvHours);
        tvDays = findViewById(R.id.tvDays);

        btnEditMedication.setOnClickListener(v -> toggleMedicationEdit(true));
        btnCancelMedication.setOnClickListener(v -> {
            populateMedicationFields(medication);
            toggleMedicationEdit(false);
        });
        btnSaveMedication.setOnClickListener(v -> saveMedicationChanges());

        btnEditReminder.setOnClickListener(v -> toggleReminderEdit(true));
    }

    private void loadMedicationData() {
        FirebaseFirestore.getInstance().collection("medications")
                .document(medicationId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    medication = snapshot.toObject(Medication.class);
                    if (medication != null) {
                        populateMedicationFields(medication);
                        reminderOriginal = medication.getReminder();
                        displayReminderInfo(reminderOriginal);

                        if (reminderOriginal != null) {
                            reminderId = reminderOriginal.getReminderId();
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error cargando medicamento", Toast.LENGTH_SHORT).show()
                );
    }

    private void populateMedicationFields(Medication med) {
        etName.setText(med.getName());
        etDose.setText(med.getDose());
        etNotes.setText(med.getNotes());
    }

    private void displayReminderInfo(Reminder reminder) {
        if (reminder == null) {
            tvStartDate.setText("Sin recordatorio");
            tvEndDate.setText("");
            tvRecurring.setText("");
            tvFrequency.setText("");
            tvHours.setText("");
            tvDays.setText("");
            return;
        }

        tvStartDate.setText("Inicio: " + formatDate(reminder.getStartDate()));
        tvEndDate.setText("Fin: " + formatDate(reminder.getEndDate()));
        tvRecurring.setText("Recurrente: " + (reminder.isRecurring() ? "Sí" : "No"));
        tvFrequency.setText("Frecuencia: " + reminder.getFrequency());
        tvHours.setText("Horas: " + TextUtils.join(", ", reminder.getScheduleTimes()));

        if (reminder.getDays() != null && !reminder.getDays().isEmpty()) {
            tvDays.setText("Días: " + TextUtils.join(", ", reminder.getDays()));
        } else {
            tvDays.setText("Días: N/A");
        }
    }


    // Helper para formatear fechas (ajústalo según cómo almacenas las fechas)
    private String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        return sdf.format(date);
    }


    private void toggleMedicationEdit(boolean enable) {
        isEditingMedication = enable;
        etName.setEnabled(enable);
        etDose.setEnabled(enable);
        etNotes.setEnabled(enable);
        btnSaveMedication.setVisibility(enable ? View.VISIBLE : View.GONE);
        btnCancelMedication.setVisibility(enable ? View.VISIBLE : View.GONE);
        btnEditMedication.setVisibility(enable ? View.GONE : View.VISIBLE);
    }

    private void saveMedicationChanges() {
        medication.setName(etName.getText().toString());
        medication.setDose(etDose.getText().toString());
        medication.setNotes(etNotes.getText().toString());

        MedicationRepository.updateMedication(medication,
                unused -> {
                    Toast.makeText(this, "Medicamento actualizado", Toast.LENGTH_SHORT).show();
                    toggleMedicationEdit(false);
                },
                e -> Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show()
        );
    }

    private void toggleReminderEdit(boolean enable) {
            Intent intent = new Intent(this, ReminderActivity.class);
            intent.putExtra("treatmentId", medication.getTreatmentId());
            intent.putExtra("medicationId", medication.getMedicationId());
            intent.putExtra("reminderId", medication.getReminder().getReminderId());
            startActivityForResult(intent, 1001);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001 && resultCode == RESULT_OK) {
            // Volvemos de ReminderActivity, recargamos datos
            loadMedicationData(); // Esto volverá a traer `medication` y su `reminder`
        }
    }
    private void saveReminderChanges() {
        // Aquí puedes implementar la lógica para guardar cambios en el recordatorio si es necesario.
        // Actualmente solo actualiza la UI y desactiva la edición.

        if (reminderOriginal == null) {
            Toast.makeText(this, "No hay recordatorio para guardar", Toast.LENGTH_SHORT).show();
            return;
        }

        medication.setReminder(reminderOriginal);

        FirebaseFirestore.getInstance()
                .collection("medications")
                .document(medication.getMedicationId())
                .set(medication)
                .addOnSuccessListener(unused -> {
                    FirebaseFirestore.getInstance()
                            .collection("reminders")
                            .whereEqualTo("medicationId", medication.getMedicationId())
                            .get()
                            .addOnSuccessListener(query -> {
                                for (DocumentSnapshot doc : query.getDocuments()) {
                                    doc.getReference().set(reminderOriginal);
                                }
                                Toast.makeText(this, "Recordatorio actualizado", Toast.LENGTH_SHORT).show();
                                toggleReminderEdit(false);
                                displayReminderInfo(reminderOriginal);
                            });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al actualizar recordatorio", Toast.LENGTH_SHORT).show()
                );
    }
}
