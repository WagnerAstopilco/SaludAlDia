package com.example.saludaldia.ui.treatment;


import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.saludaldia.R;
import com.example.saludaldia.data.model.Treatment;
import com.example.saludaldia.data.repository.TreatmentRepository;
import com.example.saludaldia.ui.toolbar.AdultToolbar;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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

        Treatment treatment = new Treatment();
        treatment.setTreatmentId(null); // Se genera automáticamente en Firestore
        treatment.setUserId(FirebaseAuth.getInstance().getCurrentUser().getUid());
        treatment.setName(name);
        treatment.setStartDate(startDate);
        treatment.setEndDate(endDate);
        treatment.setDescription(description);
        treatment.setState(state);

        TreatmentRepository repository = new TreatmentRepository();
        repository.addTreatment(treatment,
                () -> {
                    Toast.makeText(this, "Tratamiento guardado", Toast.LENGTH_SHORT).show();

                    // ← ¡Aquí el cambio clave!
                    Intent resultIntent = new Intent();
                    setResult(RESULT_OK);
                    finish();
                },
                () -> Toast.makeText(this, "Error al guardar tratamiento", Toast.LENGTH_SHORT).show());
    }

}