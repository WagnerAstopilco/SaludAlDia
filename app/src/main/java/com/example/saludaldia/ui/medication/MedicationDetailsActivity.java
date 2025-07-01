package com.example.saludaldia.ui.medication;

import static android.content.ContentValues.TAG;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.saludaldia.R;
import com.example.saludaldia.data.model.Medication;
import com.example.saludaldia.data.model.Reminder;
import com.example.saludaldia.data.repository.MedicationRepository;
import com.example.saludaldia.ui.ReminderActivity;
import com.example.saludaldia.ui.toolbar.AdultToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class MedicationDetailsActivity extends AppCompatActivity {

    private EditText etName, etDose, etNotes, etPresentation, etVia, etDays;
    private Button btnEditMedication, btnSaveMedication, btnCancelMedication;
    private TextView tvReminderInfo, tvStartDate , tvEndDate, tvRecurring , tvFrequency,tvHours, tvDays, tvState ;
    private Button btnEditReminder;
    private LinearLayout reminderInfoLayout;
    private MaterialCardView reminderCardView;
    private Medication medication;
    private Reminder reminderOriginal;
    private String treatmentId;
    private boolean isEditingMedication = false;

    private String medicationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medication_details);
        AdultToolbar.setup(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.medication_details_activity_title));
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
        etPresentation=findViewById(R.id.etMedicationPresentation);
        etVia=findViewById(R.id.etMedicationVia);
        etDays=findViewById(R.id.etMedicationNumber_of_days);

        btnEditMedication = findViewById(R.id.btnEditMedication);
        btnSaveMedication = findViewById(R.id.btnSaveMedication);
        btnCancelMedication = findViewById(R.id.btnCancelMedication);

        tvReminderInfo = findViewById(R.id.tvReminderInfo);
        tvState=findViewById(R.id.txtMedicationState);
        btnEditReminder = findViewById(R.id.btnEditReminder);

        tvStartDate = findViewById(R.id.tvStartDate);
        tvEndDate = findViewById(R.id.tvEndDate);
        tvRecurring = findViewById(R.id.tvRecurring);
        tvFrequency = findViewById(R.id.tvFrequency);
        tvHours = findViewById(R.id.tvHours);
        tvDays = findViewById(R.id.tvDays);

        reminderCardView = findViewById(R.id.reminderCardView);
        reminderInfoLayout = findViewById(R.id.reminderInfoLayout);


        btnEditMedication.setOnClickListener(v -> toggleMedicationEdit(true));
        btnCancelMedication.setOnClickListener(v -> {
            populateMedicationFields(medication);
            toggleMedicationEdit(false);
        });
        btnSaveMedication.setOnClickListener(v -> saveMedicationChanges());

        btnEditReminder.setOnClickListener(v -> toggleReminderEdit());
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
                    } else {
                        Toast.makeText(this, "Medicamento no encontrado.", Toast.LENGTH_SHORT).show();
                        Log.w(TAG, "Medicamento con ID " + medicationId + " no encontrado.");
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error cargando medicamento: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error cargando medicamento", e);
                });
    }

    private void populateMedicationFields(Medication med) {
        etName.setText(med.getName());
        etDose.setText(med.getDose());
        etNotes.setText(med.getNotes());
        etPresentation.setText(med.getPresentation());
        etVia.setText(med.getVia());
        etDays.setText(String.valueOf(med.getNumber_days()));
        String state=med.getIsActive()?"Activo":"Inactivo";
        tvState.setText(state);
    }

    private void displayReminderInfo(Reminder reminder) {
        if (reminder == null || reminder.getReminderId() == null) {
            tvReminderInfo.setText(getString(R.string.medication_details_activity_reminder_no_data));
            tvReminderInfo.setVisibility(View.VISIBLE);
            reminderInfoLayout.setVisibility(View.GONE);
        } else {
            tvReminderInfo.setVisibility(View.GONE);
            reminderInfoLayout.setVisibility(View.VISIBLE);

            tvStartDate.setText(getString(R.string.medication_details_activity_reminder_start_date)+" " +  formatDate(reminder.getStartDate()));
            tvEndDate.setText(getString(R.string.medication_details_activity_reminder_end_date)+" "  + formatDate(reminder.getEndDate()));
            tvRecurring.setText(getString(R.string.medication_details_activity_reminder_repetitive)+" " + (reminder.getIsRecurring() ? "Sí" : "No"));
            tvFrequency.setText(getString(R.string.medication_details_activity_reminder_frecuency)+" "  + reminder.getFrequency());
            tvHours.setText(getString(R.string.medication_details_activity_reminder_scheduled_hours)+" " + TextUtils.join(", ", Objects.requireNonNull(reminder.getScheduleTimes())));

            if (reminder.getDays() != null && !reminder.getDays().isEmpty()) {
                tvDays.setText(getString(R.string.medication_details_activity_reminder_scheduled_days)+" "  + TextUtils.join(", ", reminder.getDays()));
            } else {
                tvDays.setText(getString(R.string.medication_details_activity_reminder_scheduled_days)+" " + "TODOS");
            }
        }
    }


    private String formatDate(Date date) {
        if (date == null) {
            return "N/A";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        return sdf.format(date);
    }


    private void toggleMedicationEdit(boolean enable) {
        isEditingMedication = enable;
        etName.setEnabled(enable);
        etPresentation.setEnabled(enable);
        etDose.setEnabled(enable);
        etVia.setEnabled(enable);
        etDays.setEnabled(enable);
        etNotes.setEnabled(enable);
        btnSaveMedication.setVisibility(enable ? View.VISIBLE : View.GONE);
        btnCancelMedication.setVisibility(enable ? View.VISIBLE : View.GONE);
        btnEditMedication.setVisibility(enable ? View.GONE : View.VISIBLE);
    }

    private void saveMedicationChanges() {
        String daysString = etDays.getText().toString();
        int numberOfDays;
        try {
            numberOfDays = Integer.parseInt(daysString);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Por favor, introduce un número válido para los días.", Toast.LENGTH_SHORT).show();
            return;
        }

        medication.setName(etName.getText().toString());
        medication.setPresentation(etPresentation.getText().toString());
        medication.setVia(etVia.getText().toString());
        medication.setNumber_days(numberOfDays);
        medication.setDose(etDose.getText().toString());
        medication.setNotes(etNotes.getText().toString());

        MedicationRepository.updateMedication(medication,
                unused -> {
                    Toast.makeText(this, "Medicamento actualizado", Toast.LENGTH_SHORT).show();
                    toggleMedicationEdit(false);
                },
                e -> Toast.makeText(this, "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    private void toggleReminderEdit() {
        String currentMedicationId = medication.getMedicationId();
        String currentTreatmentId = medication.getTreatmentId();

        if (currentMedicationId == null || currentTreatmentId == null) {
            Log.e(TAG, "Medication or Treatment ID is null. Cannot open ReminderActivity.");
            Toast.makeText(this, "Error: Datos del medicamento incompletos.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("reminders")
                .whereEqualTo("medicationId", currentMedicationId)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Intent intent = new Intent(this, ReminderActivity.class);
                    intent.putExtra("medicationId", currentMedicationId);
                    intent.putExtra("treatmentId", currentTreatmentId);

                    if (!querySnapshot.isEmpty()) {
                        String reminderId = querySnapshot.getDocuments().get(0).getId();
                        intent.putExtra("reminderId", reminderId);
                        Log.d(TAG, "Existing reminder found for medication: " + currentMedicationId + ", ID: " + reminderId);
                    } else {
                        Log.d(TAG, "No existing reminder found for medication: " + currentMedicationId + ". Will create new.");
                    }

                    startActivityForResult(intent, 1001);

                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching reminder for medication: " + currentMedicationId, e);
                    Toast.makeText(this, "Error al cargar recordatorio: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001 && resultCode == RESULT_OK) {
            loadMedicationData();
        }
    }
}