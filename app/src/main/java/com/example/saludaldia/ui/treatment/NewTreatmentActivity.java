package com.example.saludaldia.ui.treatment;


import static android.content.ContentValues.TAG;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.saludaldia.R;
import com.example.saludaldia.data.model.HistoryEvent;
import com.example.saludaldia.data.model.Treatment;
import com.example.saludaldia.data.repository.HistoryRepository;
import com.example.saludaldia.data.repository.TreatmentRepository;
import com.example.saludaldia.ui.toolbar.AdultToolbar;
import com.google.firebase.auth.FirebaseAuth;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class NewTreatmentActivity extends AppCompatActivity {

    private EditText editTextName, editTextDescription;
    private Button buttonStartDate, buttonEndDate, buttonSaveTreatment;
    private Spinner spinnerState;

    private Date startDate, endDate;

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_treatment);
        AdultToolbar.setup(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.new_treatment_activity_title));
        }
        editTextName = findViewById(R.id.editTextName);
        editTextDescription = findViewById(R.id.editTextDescription);
        buttonStartDate = findViewById(R.id.buttonStartDate);
        buttonEndDate = findViewById(R.id.buttonEndDate);
        spinnerState = findViewById(R.id.spinnerState);
        buttonSaveTreatment = findViewById(R.id.buttonSaveTreatment);

        buttonStartDate.setOnClickListener(v -> showDatePicker(true));
        buttonEndDate.setOnClickListener(v -> showDatePicker(false));

        buttonSaveTreatment.setOnClickListener(v -> saveTreatment());
    }

    private void showDatePicker(boolean isStartDate) {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            Date selectedDate = calendar.getTime();

            if (isStartDate) {
                startDate = selectedDate;
                buttonStartDate.setText(sdf.format(selectedDate));
            } else {
                endDate = selectedDate;
                buttonEndDate.setText(sdf.format(selectedDate));
            }
        };

        new DatePickerDialog(this,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveTreatment() {
        String name = editTextName.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        String state = spinnerState.getSelectedItem().toString();

        if (name.isEmpty()) {
            editTextName.setError("Debe ingresar el nombre");
            editTextName.requestFocus();
            return;
        }
        if (startDate == null) {
            Toast.makeText(this, "Seleccione la fecha de inicio", Toast.LENGTH_SHORT).show();
            return;
        }
        if (endDate == null) {
            Toast.makeText(this, "Seleccione la fecha de fin", Toast.LENGTH_SHORT).show();
            return;
        }
        if (startDate.after(endDate)) {
            Toast.makeText(this, "La fecha de inicio no puede ser mayor que la de fin", Toast.LENGTH_SHORT).show();
            return;
        }

        String treatmentId = UUID.randomUUID().toString();
        Treatment treatment = new Treatment();
        treatment.setTreatmentId(treatmentId);
        treatment.setUserId(FirebaseAuth.getInstance().getCurrentUser().getUid());
        treatment.setName(name);
        treatment.setStartDate(startDate);
        treatment.setEndDate(endDate);
        treatment.setDescription(description);
        treatment.setState(state);

        TreatmentRepository repository = new TreatmentRepository();
        repository.addTreatment(treatment,
                () -> {
                    Toast.makeText(this, getString(R.string.new_treatment_ativity_treatment_save), Toast.LENGTH_SHORT).show();

                    String eventId = UUID.randomUUID().toString(); // Generar un ID único para el HistoryEvent
                    String eventDetails = "Se ha añadido un nuevo tratamiento: " + name + " (ID: " + treatmentId + ")";
                    String userId=FirebaseAuth.getInstance().getCurrentUser().getUid();
                    HistoryEvent treatmentAddedEvent = new HistoryEvent();
                    treatmentAddedEvent.setEventId(eventId);
                    treatmentAddedEvent.setEventType("Agregado");
                    treatmentAddedEvent.setDetails(eventDetails);
                    HistoryRepository.addHistoryEventAndLinkToHistory(userId, treatmentAddedEvent)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "HistoryEvent for treatment added successfully: " + eventId);
                                Toast.makeText(this, "Tratamiento y evento de historial guardados.", Toast.LENGTH_SHORT).show();
                                Intent resultIntent = new Intent();
                                setResult(RESULT_OK);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to add history event for treatment: " + e.getMessage(), e);
                                Toast.makeText(this, "Error al guardar evento de historial.", Toast.LENGTH_LONG).show();
                                Intent resultIntent = new Intent();
                                setResult(RESULT_OK);
                                finish();
                            });
                    Intent resultIntent = new Intent();
                    setResult(RESULT_OK);
                    finish();
                },
                () -> Toast.makeText(this, "Error al guardar tratamiento", Toast.LENGTH_SHORT).show());
    }

}