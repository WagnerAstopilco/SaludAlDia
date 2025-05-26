package com.example.saludaldia.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.saludaldia.R;
import com.example.saludaldia.ui.toolbar.AdultToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.*;

public class ReminderActivity extends AppCompatActivity {

    private EditText txtStartDate, txtEndDate;
    private CheckBox chkRecurring;
    private Spinner spnFrequency;
    private TextView tvScheduleTimes;
    private Button btnAddHour, btnSaveReminder;
    private ChipGroup chipGroupDaysOfWeek;
    private Chip chipMonday, chipTuesday, chipWednesday, chipThursday, chipFriday, chipSaturday, chipSunday;

    private String treatmentId, medicationId, reminderId;
    private Date treatmentStartDate;
    private final List<String> selectedTimes = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final Calendar calendar = Calendar.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);
        AdultToolbar.setup(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Recordatorio");
        }

        treatmentId = getIntent().getStringExtra("treatmentId");
        medicationId = getIntent().getStringExtra("medicationId");
        reminderId = getIntent().getStringExtra("reminderId"); // Puede ser null

        // Inicializar vistas
        txtStartDate = findViewById(R.id.txtStartDate);
        txtEndDate = findViewById(R.id.txtEndDate);
        chkRecurring = findViewById(R.id.chkRecurring);
        spnFrequency = findViewById(R.id.spnFrequency);
        tvScheduleTimes = findViewById(R.id.tvScheduleTimes);
        btnAddHour = findViewById(R.id.btnAddHour);
        btnSaveReminder = findViewById(R.id.btnSaveReminder);
        findViewById(R.id.btnCancel).setOnClickListener(v -> finish());

        chipGroupDaysOfWeek = findViewById(R.id.chipGroupDays);
        chipMonday = findViewById(R.id.chipMonday);
        chipTuesday = findViewById(R.id.chipTuesday);
        chipWednesday = findViewById(R.id.chipWednesday);
        chipThursday = findViewById(R.id.chipThursday);
        chipFriday = findViewById(R.id.chipFriday);
        chipSaturday = findViewById(R.id.chipSaturday);
        chipSunday = findViewById(R.id.chipSunday);

        // Configurar spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.frequency_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnFrequency.setAdapter(adapter);

        // Mostrar días solo si corresponde
        spnFrequency.setEnabled(false);
        chipGroupDaysOfWeek.setVisibility(View.GONE);

        chkRecurring.setOnCheckedChangeListener((buttonView, isChecked) -> {
            spnFrequency.setEnabled(isChecked);
            updateDaysOfWeekVisibility(isChecked, spnFrequency.getSelectedItem().toString());
        });

        spnFrequency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                updateDaysOfWeekVisibility(chkRecurring.isChecked(), parent.getItemAtPosition(pos).toString());
            }
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        txtEndDate.setOnClickListener(v -> {
            int y = calendar.get(Calendar.YEAR), m = calendar.get(Calendar.MONTH), d = calendar.get(Calendar.DAY_OF_MONTH);
            new DatePickerDialog(this, (view, year, month, day) -> {
                calendar.set(year, month, day);
                txtEndDate.setText(dateFormat.format(calendar.getTime()));
            }, y, m, d).show();
        });

        btnAddHour.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            new TimePickerDialog(this, (tp, h, m) -> {
                String time = String.format(Locale.getDefault(), "%02d:%02d", h, m);
                if (!selectedTimes.contains(time)) {
                    selectedTimes.add(time);
                    Collections.sort(selectedTimes);
                    tvScheduleTimes.setText(TextUtils.join(", ", selectedTimes));
                }
            }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show();
        });

        btnSaveReminder.setOnClickListener(v -> saveReminder());

        // Obtener fecha de inicio del tratamiento
        db.collection("treatments").document(treatmentId).get().addOnSuccessListener(doc -> {
            treatmentStartDate = doc.getDate("startDate");
            if (treatmentStartDate != null) txtStartDate.setText(dateFormat.format(treatmentStartDate));
        });

        if (reminderId != null) loadReminderForEdit();
    }

    private void loadReminderForEdit() {
        db.collection("reminders").document(reminderId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                txtStartDate.setText(dateFormat.format(doc.getDate("startDate")));
                txtEndDate.setText(dateFormat.format(doc.getDate("endDate")));
                chkRecurring.setChecked(doc.getBoolean("isRecurring") != null && doc.getBoolean("isRecurring"));
                spnFrequency.setSelection(getFrequencyIndex(doc.getString("frequency")));
                selectedTimes.clear();
                List<String> times = (List<String>) doc.get("scheduleTimes");
                if (times != null) selectedTimes.addAll(times);
                tvScheduleTimes.setText(TextUtils.join(", ", selectedTimes));

                List<String> days = (List<String>) doc.get("days");
                if (days != null) {
                    chipMonday.setChecked(days.contains("Lunes"));
                    chipTuesday.setChecked(days.contains("Martes"));
                    chipWednesday.setChecked(days.contains("Miércoles"));
                    chipThursday.setChecked(days.contains("Jueves"));
                    chipFriday.setChecked(days.contains("Viernes"));
                    chipSaturday.setChecked(days.contains("Sábado"));
                    chipSunday.setChecked(days.contains("Domingo"));
                }
            }
        });
    }

    private int getFrequencyIndex(String frequency) {
        String[] options = getResources().getStringArray(R.array.frequency_options);
        for (int i = 0; i < options.length; i++) {
            if (options[i].equalsIgnoreCase(frequency)) return i;
        }
        return 0;
    }

    private void saveReminder() {
        String endDateStr = txtEndDate.getText().toString().trim();
        boolean isRecurring = chkRecurring.isChecked();
        String frequency = spnFrequency.getSelectedItem().toString();

        if (treatmentStartDate == null || endDateStr.isEmpty() || selectedTimes.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        Date endDate;
        try {
            endDate = dateFormat.parse(endDateStr);
        } catch (Exception e) {
            Toast.makeText(this, "Fecha de fin inválida", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> reminder = new HashMap<>();
        reminder.put("medicationId", medicationId);
        reminder.put("startDate", treatmentStartDate);
        reminder.put("endDate", endDate);
        reminder.put("isRecurring", isRecurring);
        reminder.put("frequency", frequency);
        reminder.put("scheduleTimes", selectedTimes);
        reminder.put("isActive", true);

        if (isRecurring && frequency.equalsIgnoreCase("Días específicos")) {
            List<String> selectedDays = new ArrayList<>();
            if (chipMonday.isChecked()) selectedDays.add("Lunes");
            if (chipTuesday.isChecked()) selectedDays.add("Martes");
            if (chipWednesday.isChecked()) selectedDays.add("Miércoles");
            if (chipThursday.isChecked()) selectedDays.add("Jueves");
            if (chipFriday.isChecked()) selectedDays.add("Viernes");
            if (chipSaturday.isChecked()) selectedDays.add("Sábado");
            if (chipSunday.isChecked()) selectedDays.add("Domingo");

            if (selectedDays.isEmpty()) {
                Toast.makeText(this, "Selecciona al menos un día", Toast.LENGTH_SHORT).show();
                return;
            }
            reminder.put("days", selectedDays);
        }

        if (reminderId != null) {
            db.collection("reminders").document(reminderId).set(reminder)
                    .addOnSuccessListener(aVoid -> {
                        // Aquí agregamos la actualización del medicamento con el reminder actualizado
                        db.collection("medications").document(medicationId)
                                .update("reminder", reminder)
                                .addOnSuccessListener(aVoid2 -> {
                                    Toast.makeText(this, "Recordatorio actualizado y vinculado", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Recordatorio actualizado, pero fallo vincular en medicamento", Toast.LENGTH_SHORT).show();
                                    finish();
                                });
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show());
        } else {
            db.collection("reminders").add(reminder)
                    .addOnSuccessListener(documentReference -> {
                        reminderId = documentReference.getId();

                        // Añadir el ID al mismo objeto reminder
                        reminder.put("reminderId", reminderId);

                        // Guardar el reminder completo embebido en el medicamento
                        db.collection("medications").document(medicationId)
                                .update("reminder", reminder)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Recordatorio creado y vinculado", Toast.LENGTH_SHORT).show();
                                    finish();
                                }).addOnFailureListener(e -> {
                                    Toast.makeText(this, "Recordatorio creado, pero fallo vincular", Toast.LENGTH_SHORT).show();
                                    finish();
                                });
                    }).addOnFailureListener(e -> Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show());
        }
    }

    private void updateDaysOfWeekVisibility(boolean isRecurring, String frequency) {
        chipGroupDaysOfWeek.setVisibility(
                isRecurring && frequency.equalsIgnoreCase("Días específicos")
                        ? View.VISIBLE
                        : View.GONE
        );
    }
}
