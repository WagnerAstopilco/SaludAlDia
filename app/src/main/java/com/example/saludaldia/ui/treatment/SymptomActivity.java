package com.example.saludaldia.ui.treatment;


import android.app.DatePickerDialog;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.saludaldia.R;
import com.example.saludaldia.data.model.HistoryEvent;
import com.example.saludaldia.data.model.Symptom;
import com.example.saludaldia.data.repository.HistoryRepository;
import com.example.saludaldia.ui.toolbar.AdultToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class SymptomActivity extends AppCompatActivity {

    private static final String TAG = "SymptomActivity";

    private EditText etDateReported;
    private EditText etSymptomDescription;
    private Spinner spinnerSeverity;
    private EditText etNotes;
    private Button btnSaveSymptom;
    private Button btnCancelSymptom;
    private TextView tvSymptomTitle;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private SimpleDateFormat dateFormat;

    private String treatmentId;
    private String medicationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptom);
        AdultToolbar.setup(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.symptom_activity_title));
        }

        initViews();
        setupListeners();
        initFirebase();
        getIntentData();
    }

    private void initViews() {
        tvSymptomTitle = findViewById(R.id.tvSymptomTitle);
        etDateReported = findViewById(R.id.etDateReported);
        etSymptomDescription = findViewById(R.id.etSymptomDescription);
        spinnerSeverity = findViewById(R.id.spinnerSeverity);
        etNotes = findViewById(R.id.etNotes);
        btnSaveSymptom = findViewById(R.id.btnSaveSymptom);
        btnCancelSymptom = findViewById(R.id.btnCancelSymptom);

        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        ArrayAdapter<CharSequence> severityAdapter = ArrayAdapter.createFromResource(this,
                R.array.severity_options, android.R.layout.simple_spinner_item);
        severityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSeverity.setAdapter(severityAdapter);

        etDateReported.setText(dateFormat.format(new Date()));
    }

    private void setupListeners() {
        etDateReported.setOnClickListener(v -> showDatePickerDialog());
        btnSaveSymptom.setOnClickListener(v -> saveSymptom());
        btnCancelSymptom.setOnClickListener(v -> finish());
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    private void getIntentData() {
        Intent intent = getIntent();
        treatmentId = intent.getStringExtra("treatmentId");
        medicationId = intent.getStringExtra("medicationId");

        if (treatmentId != null) {
            Log.d(TAG, "Treatment ID recibido: " + treatmentId);
        }
        if (medicationId != null) {
            Log.d(TAG, "Medication ID recibido: " + medicationId);
        }
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        if (!etDateReported.getText().toString().isEmpty()) {
            try {
                calendar.setTime(dateFormat.parse(etDateReported.getText().toString()));
            } catch (ParseException e) {
                Log.e(TAG, "Error al parsear fecha del EditText: " + e.getMessage());
            }
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    etDateReported.setText(dateFormat.format(selectedDate.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void saveSymptom() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Debe iniciar sesión para reportar un síntoma.", Toast.LENGTH_LONG).show();
            return;
        }

        String userId = currentUser.getUid();
        String symptomDescription = etSymptomDescription.getText().toString().trim();
        String severity = spinnerSeverity.getSelectedItem().toString();
        String notes = etNotes.getText().toString().trim();
        String dateReportedStr = etDateReported.getText().toString().trim();

        if (symptomDescription.isEmpty()) {
            Toast.makeText(this, "Por favor, ingresa una descripción del síntoma.", Toast.LENGTH_SHORT).show();
            etSymptomDescription.setError("Campo requerido");
            return;
        }
        if (severity.equals(getResources().getStringArray(R.array.severity_options)[0])) {
            Toast.makeText(this, "Por favor, selecciona la severidad del síntoma.", Toast.LENGTH_SHORT).show();
            ((TextView) spinnerSeverity.getSelectedView()).setError("Campo requerido");
            return;
        }
        if (dateReportedStr.isEmpty()) {
            Toast.makeText(this, "Por favor, selecciona la fecha del reporte.", Toast.LENGTH_SHORT).show();
            etDateReported.setError("Campo requerido");
            return;
        }

        Date dateReported;
        try {
            dateReported = dateFormat.parse(dateReportedStr);
        } catch (ParseException e) {
            Toast.makeText(this, "Formato de fecha inválido.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error al parsear fecha: " + e.getMessage());
            return;
        }

        String symptomId = UUID.randomUUID().toString();
        Symptom newSymptom = new Symptom(
                symptomId,
                userId,
                treatmentId,
                medicationId,
                dateReported,
                symptomDescription,
                severity,
                notes
        );

        db.collection("symptoms")
                .document(symptomId)
                .set(newSymptom)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Síntoma reportado exitosamente.", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Síntoma guardado con ID: " + symptomId);

                    String eventId = UUID.randomUUID().toString();
                    String eventDetails = "Se ha reportado un nuevo síntoma: " + symptomDescription;
                    HistoryEvent treatmentAddedEvent = new HistoryEvent();
                    treatmentAddedEvent.setEventId(eventId);
                    treatmentAddedEvent.setEventType("Reportado");
                    treatmentAddedEvent.setDetails(eventDetails);
                    HistoryRepository.addHistoryEventAndLinkToHistory(userId, treatmentAddedEvent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al reportar síntoma: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error al guardar síntoma en Firestore: " + e.getMessage(), e);
                });
    }
}
