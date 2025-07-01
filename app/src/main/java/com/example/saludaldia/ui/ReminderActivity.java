package com.example.saludaldia.ui;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.saludaldia.R;
import com.example.saludaldia.data.model.Reminder;
import com.example.saludaldia.data.model.Medication;
import com.example.saludaldia.receivers.ReminderAlarmReceiver;
import com.example.saludaldia.ui.toolbar.AdultToolbar;
import com.example.saludaldia.utils.CalendarServiceManager;
import com.example.saludaldia.utils.NotificationHelper;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReminderActivity extends AppCompatActivity {

    private static final String TAG = "ReminderActivity";
    private EditText txtStartDate, txtEndDate;
    private CheckBox chkRecurring;
    private Spinner spnFrequency;
    private TextView tvScheduleTimes;
    private Button btnAddHour, btnSaveReminder;
    private ChipGroup chipGroupDaysOfWeek;
    private Chip chipMonday, chipTuesday, chipWednesday, chipThursday, chipFriday, chipSaturday, chipSunday;
    private String treatmentId, medicationId;
    private String reminderId;
    private Date treatmentStartDate;
    private Medication currentMedication;
    private final List<String> selectedTimes = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final java.util.Calendar calendar = java.util.Calendar.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private Reminder currentReminder;
    private Calendar mCalendarService;
    private String appCalendarId;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private static final int PERMISSION_REQUEST_CODE_POST_NOTIFICATIONS = 1001;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<Intent> exactAlarmPermissionLauncher;
    private CalendarServiceManager.CalendarServiceListener calendarServiceListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);
        AdultToolbar.setup(this);

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Log.d(TAG, "Permiso POST_NOTIFICATIONS concedido.");
                        Toast.makeText(this, "Permiso de notificaciones concedido.", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.w(TAG, "Permiso POST_NOTIFICATIONS denegado.");
                        Toast.makeText(this, "Permiso de notificaciones denegado. No podrás recibir recordatorios.", Toast.LENGTH_LONG).show();
                    }
                });

        exactAlarmPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (checkCanScheduleExactAlarms()) { // Verifica de nuevo si ahora tienes el permiso
                            Log.d(TAG, "Permiso de alarma exacta concedido por el usuario.");
                            Toast.makeText(this, "Permiso de alarma exacta concedido.", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.w(TAG, "Permiso de alarma exacta denegado o no concedido.");
                            Toast.makeText(this, "Permiso de alarma exacta denegado. Los recordatorios podrían no dispararse a tiempo.", Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );
        NotificationHelper.createNotificationChannel(this);
        checkAndRequestNotificationPermission();
        requestExactAlarmPermission();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.reminder_activity_title));
        }

        treatmentId = getIntent().getStringExtra("treatmentId");
        medicationId = getIntent().getStringExtra("medicationId");
        reminderId = getIntent().getStringExtra("reminderId");

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

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.frequency_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnFrequency.setAdapter(adapter);

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
            int y = calendar.get(java.util.Calendar.YEAR), m = calendar.get(java.util.Calendar.MONTH), d = calendar.get(java.util.Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, day) -> {
                calendar.set(year, month, day);
                txtEndDate.setText(dateFormat.format(calendar.getTime()));
            }, y, m, d);
            datePickerDialog.show();
        });

        btnAddHour.setOnClickListener(v -> {
            java.util.Calendar now = java.util.Calendar.getInstance();
            new TimePickerDialog(this, (tp, h, m) -> {
                String time = String.format(Locale.getDefault(), "%02d:%02d", h, m);
                if (!selectedTimes.contains(time)) {
                    selectedTimes.add(time);
                    Collections.sort(selectedTimes);
                    updateScheduleTimesTextView();
                } else {
                    Toast.makeText(this, "Esta hora ya ha sido añadida.", Toast.LENGTH_SHORT).show();
                }
            }, now.get(java.util.Calendar.HOUR_OF_DAY), now.get(java.util.Calendar.MINUTE), true).show();
        });

        setupDayChips();
        loadTreatmentAndMedicationData();

        calendarServiceListener = new CalendarServiceManager.CalendarServiceListener() {
            @Override
            public void onCalendarServiceReady(Calendar calendarService, String calendarId) {
                mCalendarService = calendarService;
                appCalendarId = calendarId;
                Log.d(TAG, "ReminderActivity: Servicio de calendario y ID recibidos del Singleton.");
            }

            @Override
            public void onCalendarServiceError(String message) {
                Log.e(TAG, "ReminderActivity: Error al obtener servicio de calendario del Singleton: " + message);
                mainHandler.post(() -> Toast.makeText(ReminderActivity.this, "Error al obtener servicio de calendario: " + message, Toast.LENGTH_LONG).show());
            }
        };

        CalendarServiceManager.getInstance().addCalendarServiceListener(calendarServiceListener);

        if (CalendarServiceManager.getInstance().getCalendarService() != null &&
                CalendarServiceManager.getInstance().getAppCalendarId() != null) {
            mCalendarService = CalendarServiceManager.getInstance().getCalendarService();
            appCalendarId = CalendarServiceManager.getInstance().getAppCalendarId();
            Log.d(TAG, "ReminderActivity: Servicio de calendario y ID ya disponibles en el Singleton.");
        } else {
            Log.d(TAG, "ReminderActivity: Esperando que el servicio de calendario se inicialice en el Singleton.");
            Toast.makeText(this, "Cargando servicio de calendario...", Toast.LENGTH_SHORT).show();
        }

        btnSaveReminder.setOnClickListener(v -> saveReminder());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (calendarServiceListener != null) {
            CalendarServiceManager.getInstance().removeCalendarServiceListener(calendarServiceListener);
        }
        executorService.shutdown();
    }
    private boolean checkCanScheduleExactAlarms() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            return alarmManager != null && alarmManager.canScheduleExactAlarms();
        }
        return true;
    }
    private void requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!checkCanScheduleExactAlarms()) {
                new AlertDialog.Builder(this)
                        .setTitle("Permiso de Alarmas Exactas Necesario")
                        .setMessage("Para que los recordatorios de medicamentos se disparen a la hora exacta, necesitamos tu permiso especial para programar alarmas. Por favor, concédelo en la siguiente pantalla.")
                        .setPositiveButton("Ir a Configuración", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                            intent.setData(Uri.fromParts("package", getPackageName(), null));
                            exactAlarmPermissionLauncher.launch(intent);
                        })
                        .setNegativeButton("Cancelar", (dialog, which) -> {
                            Toast.makeText(this, "Permiso de alarma exacta denegado. Los recordatorios pueden retrasarse.", Toast.LENGTH_LONG).show();
                        })
                        .show();
            }
        }
    }
    private void checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // TIRAMISU es API 33
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permiso POST_NOTIFICATIONS ya concedido.");
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                new AlertDialog.Builder(this)
                        .setTitle("Permiso de Notificaciones Necesario")
                        .setMessage("Esta aplicación necesita permiso para mostrar recordatorios de medicamentos. Por favor, concédeselo.")
                        .setPositiveButton("Conceder", (dialog, which) -> {
                            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                        })
                        .setNegativeButton("Cancelar", (dialog, which) -> {
                            Toast.makeText(this, "Permiso de notificaciones denegado.", Toast.LENGTH_SHORT).show();
                        })
                        .show();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            Log.d(TAG, "POST_NOTIFICATIONS no requerido para API < 33.");
        }
    }
    private void updateScheduleTimesTextView() {
        if (selectedTimes.isEmpty()) {
            tvScheduleTimes.setText(R.string.reminder_activity_no_schedules);
        } else {
            StringBuilder sb = new StringBuilder(getString(R.string.reminder_activity_schedules) + ": ");
            for (int i = 0; i < selectedTimes.size(); i++) {
                sb.append(selectedTimes.get(i));
                if (i < selectedTimes.size() - 1) {
                    sb.append(", ");
                }
            }
            tvScheduleTimes.setText(sb.toString());
        }
    }

    private void setupDayChips() {
        chipGroupDaysOfWeek.setOnCheckedStateChangeListener((group, checkedIds) -> {
            Log.d(TAG, "Checked chip IDs: " + checkedIds);
        });
    }

    private void updateDaysOfWeekVisibility(boolean isRecurring, String frequency) {
        chipGroupDaysOfWeek.setVisibility(
                isRecurring && frequency.equalsIgnoreCase("Días específicos")
                        ? View.VISIBLE
                        : View.GONE
        );
        if (chipGroupDaysOfWeek.getVisibility() == View.GONE) {
            chipGroupDaysOfWeek.clearCheck();
        }
    }

    private List<String> getSelectedDaysOfWeek() {
        List<String> selectedDays = new ArrayList<>();
        if (chipMonday.isChecked()) selectedDays.add("Lunes");
        if (chipTuesday.isChecked()) selectedDays.add("Martes");
        if (chipWednesday.isChecked()) selectedDays.add("Miércoles");
        if (chipThursday.isChecked()) selectedDays.add("Jueves");
        if (chipFriday.isChecked()) selectedDays.add("Viernes");
        if (chipSaturday.isChecked()) selectedDays.add("Sábado");
        if (chipSunday.isChecked()) selectedDays.add("Domingo");
        return selectedDays;
    }

    private void loadTreatmentAndMedicationData() {
        if (treatmentId != null && !treatmentId.isEmpty()) {
            db.collection("treatments").document(treatmentId).get().addOnSuccessListener(doc -> {
                treatmentStartDate = doc.getDate("startDate");
                if (treatmentStartDate != null) {
                    txtStartDate.setText(dateFormat.format(treatmentStartDate));
                    if (reminderId != null && !reminderId.isEmpty()) {
                        loadReminderForEdit();
                    } else {
                        currentReminder = new Reminder();
                        currentReminder.setMedicationId(medicationId);
                        currentReminder.setIsActive(true);
                    }
                } else {
                    Log.e(TAG, "Fecha de inicio del tratamiento es nula para el ID: " + treatmentId);
                    Toast.makeText(this, "Error: No se pudo obtener la fecha de inicio del tratamiento.", Toast.LENGTH_LONG).show();
                    finish();
                }
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Error al cargar la fecha de inicio del tratamiento", e);
                Toast.makeText(this, "Error al cargar la fecha de inicio del tratamiento.", Toast.LENGTH_LONG).show();
                finish();
            });
        } else {
            Log.e(TAG, "treatmentId es nulo o vacío.");
            Toast.makeText(this, "Error: ID de tratamiento no proporcionado.", Toast.LENGTH_LONG).show();
            finish();
        }

        if (medicationId != null && !medicationId.isEmpty()) {
            db.collection("medications").document(medicationId).get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    currentMedication = doc.toObject(Medication.class);
                    if (currentMedication == null) {
                        Log.e(TAG, "Error: Medication object is null after converting document for ID: " + medicationId);
                        Toast.makeText(this, "Error al cargar datos del medicamento.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Log.w(TAG, "Medicamento no encontrado para el ID: " + medicationId);
                    Toast.makeText(this, "Medicamento no encontrado.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Error al cargar datos del medicamento", e);
                Toast.makeText(this, "Error al cargar datos del medicamento.", Toast.LENGTH_LONG).show();
                finish();
            });
        } else {
            Log.e(TAG, "medicationId es nulo o vacío.");
            Toast.makeText(this, "Error: ID de medicamento no proporcionado.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void loadReminderForEdit() {
        db.collection("reminders").document(reminderId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                currentReminder = doc.toObject(Reminder.class);
                if (currentReminder != null) {
                    if (currentReminder.getStartDate() != null) {
                        txtStartDate.setText(dateFormat.format(currentReminder.getStartDate()));
                    }
                    if (currentReminder.getEndDate() != null) {
                        txtEndDate.setText(dateFormat.format(currentReminder.getEndDate()));
                    }
                    chkRecurring.setChecked(Boolean.TRUE.equals(currentReminder.getIsRecurring()));
                    spnFrequency.setSelection(getFrequencyIndex(currentReminder.getFrequency()));
                    selectedTimes.clear();
                    List<String> times = currentReminder.getScheduleTimes();
                    if (times != null) {
                        selectedTimes.addAll(times);
                        Collections.sort(selectedTimes);
                    }
                    updateScheduleTimesTextView();

                    spnFrequency.setEnabled(chkRecurring.isChecked());
                    updateDaysOfWeekVisibility(chkRecurring.isChecked(), currentReminder.getFrequency());

                    List<String> days = currentReminder.getDays();
                    if (days != null) {
                        chipMonday.setChecked(days.contains("Lunes"));
                        chipTuesday.setChecked(days.contains("Martes"));
                        chipWednesday.setChecked(days.contains("Miércoles"));
                        chipThursday.setChecked(days.contains("Jueves"));
                        chipFriday.setChecked(days.contains("Viernes"));
                        chipSaturday.setChecked(days.contains("Sábado"));
                        chipSunday.setChecked(days.contains("Domingo"));
                    }
                } else {
                    Log.e(TAG, "Error: Reminder object is null after converting document.");
                    Toast.makeText(this, "Error al cargar recordatorio.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                Log.w(TAG, "Recordatorio no encontrado para el ID: " + reminderId);
                Toast.makeText(this, "Recordatorio no encontrado.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error al cargar recordatorio para edición", e);
            Toast.makeText(this, "Error al cargar recordatorio para edición.", Toast.LENGTH_SHORT).show();
            finish();
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

    if (treatmentStartDate == null) {
        Toast.makeText(this, "Error: La fecha de inicio del tratamiento no ha sido cargada.", Toast.LENGTH_LONG).show();
        return;
    }
    if (endDateStr.isEmpty()) {
        Toast.makeText(this, "Por favor, selecciona una fecha de fin.", Toast.LENGTH_SHORT).show();
        return;
    }
    if (selectedTimes.isEmpty()) {
        Toast.makeText(this, "Por favor, añade al menos una hora de recordatorio.", Toast.LENGTH_SHORT).show();
        return;
    }
    if (currentMedication == null) {
        Toast.makeText(this, "Error: Los datos del medicamento no han sido cargados.", Toast.LENGTH_LONG).show();
        return;
    }

    Date endDate;
    try {
        endDate = dateFormat.parse(endDateStr);
    } catch (ParseException e) {
        Toast.makeText(this, "Formato de fecha de fin inválido.", Toast.LENGTH_SHORT).show();
        return;
    }

    currentReminder.setMedicationId(medicationId);
    currentReminder.setStartDate(treatmentStartDate);
    currentReminder.setEndDate(endDate);
    currentReminder.setIsRecurring(isRecurring);
    currentReminder.setFrequency(frequency);
    currentReminder.setScheduleTimes(selectedTimes);
    currentReminder.setIsActive(true);

    if (isRecurring && frequency.equalsIgnoreCase("Días específicos")) {
        List<String> selectedDays = getSelectedDaysOfWeek();
        if (selectedDays.isEmpty()) {
            Toast.makeText(this, "Selecciona al menos un día para la frecuencia de 'Días específicos'.", Toast.LENGTH_SHORT).show();
            return;
        }
        currentReminder.setDays(selectedDays);
    } else {
        currentReminder.setDays(null);
    }

    if (reminderId != null && !reminderId.isEmpty()) {
        // Eliminar eventos de calendario existentes antes de recrearlos
        deleteExistingCalendarEvents(currentReminder.getCalendarEventIds(), new CalendarServiceManager.OnCalendarEventsDeletedListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Eventos de calendario previos eliminados exitosamente (si existían).");
                // Después de eliminar, actualiza y crea nuevos eventos y alarmas
                updateReminderInFirestoreAndCreateNewEvents(currentReminder, currentMedication);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error al eliminar eventos de calendario previos: " + e.getMessage(), e);
                Toast.makeText(ReminderActivity.this, "Error al limpiar eventos antiguos del calendario. Intenta guardar de nuevo.", Toast.LENGTH_LONG).show();
                // Aunque haya un error al eliminar, permitir que continúe para intentar recrear.
                updateReminderInFirestoreAndCreateNewEvents(currentReminder, currentMedication);
            }
        });
    } else {
        DocumentReference newReminderRef = db.collection("reminders").document();
        reminderId = newReminderRef.getId();
        currentReminder.setReminderId(reminderId);

        newReminderRef.set(currentReminder)
                .addOnSuccessListener(aVoid -> {
                    db.collection("medications").document(medicationId)
                            .update("reminder", currentReminder)
                            .addOnSuccessListener(aVoid2 -> {
                                Toast.makeText(this, "Recordatorio creado y vinculado correctamente.", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error al vincular el nuevo recordatorio al medicamento", e);
                                Toast.makeText(this, "Recordatorio creado, pero hubo un fallo al vincularlo al medicamento.", Toast.LENGTH_LONG).show();
                            });
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Error al guardar el nuevo recordatorio en Firestore", e);
                    Toast.makeText(this, "Error al guardar el recordatorio: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}

    private void updateReminderInFirestoreAndCreateNewEvents(Reminder reminder, Medication medication) {
        db.collection("reminders").document(reminder.getReminderId())
                .set(reminder)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Recordatorio actualizado en Firestore: " + reminder.getReminderId());
                    createCalendarEventsForReminder(reminder, medication);
                    Toast.makeText(ReminderActivity.this, "Recordatorio actualizado correctamente.", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al actualizar el recordatorio en Firestore: " + e.getMessage(), e);
                    Toast.makeText(ReminderActivity.this, "Error al actualizar el recordatorio: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void deleteExistingCalendarEvents(List<String> eventIdsToDelete, CalendarServiceManager.OnCalendarEventsDeletedListener listener) {
        CalendarServiceManager.getInstance().deleteCalendarEvents(eventIdsToDelete, listener);
    }

    private String getDayName(int dayOfWeek) {
        switch (dayOfWeek) {
            case java.util.Calendar.MONDAY: return "Lunes";
            case java.util.Calendar.TUESDAY: return "Martes";
            case java.util.Calendar.WEDNESDAY: return "Miércoles";
            case java.util.Calendar.THURSDAY: return "Jueves";
            case java.util.Calendar.FRIDAY: return "Viernes";
            case java.util.Calendar.SATURDAY: return "Sábado";
            case java.util.Calendar.SUNDAY: return "Domingo";
            default: return "";
        }
    }

    private void createCalendarEventsForReminder(Reminder reminder, Medication medication) {
        if (mCalendarService == null || appCalendarId == null) {
            mainHandler.post(() -> Toast.makeText(ReminderActivity.this, "Servicio de calendario no disponible. No se pudieron crear eventos.", Toast.LENGTH_LONG).show());
            Log.e(TAG, "Calendar service or appCalendarId not initialized in ReminderActivity. Cannot create events.");
            return;
        }
        if (reminder.getScheduleTimes() == null || reminder.getScheduleTimes().isEmpty()) {
            mainHandler.post(() -> Toast.makeText(ReminderActivity.this, "No hay horas programadas para crear eventos.", Toast.LENGTH_SHORT).show());
            return;
        }
        if (reminder.getStartDate() == null) { // Solo necesitamos validar start date para el cálculo
            mainHandler.post(() -> Toast.makeText(ReminderActivity.this, "Fecha de inicio del recordatorio es nula. No se pudieron crear eventos.", Toast.LENGTH_SHORT).show());
            Log.e(TAG, "Reminder start date is null. Cannot create events.");
            return;
        }
        if (medication.getNumber_days() <= 0) {
            mainHandler.post(() -> Toast.makeText(ReminderActivity.this, "Duración de la medicación inválida. No se pudieron crear eventos.", Toast.LENGTH_SHORT).show());
            Log.e(TAG, "Medication duration days is 0 or less. Cannot create events.");
            return;
        }

        executorService.execute(() -> {
            List<String> createdEventIds = new ArrayList<>();
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            TimeZone deviceTimeZone = TimeZone.getDefault();

            try {
                java.util.Calendar currentDayCal = java.util.Calendar.getInstance();
                currentDayCal.setTime(reminder.getStartDate());
                java.util.Calendar calculatedEndDateCal = (java.util.Calendar) currentDayCal.clone();
                calculatedEndDateCal.add(java.util.Calendar.DATE, medication.getNumber_days() - 1);

                currentDayCal.set(java.util.Calendar.HOUR_OF_DAY, 0);
                currentDayCal.set(java.util.Calendar.MINUTE, 0);
                currentDayCal.set(java.util.Calendar.SECOND, 0);
                currentDayCal.set(java.util.Calendar.MILLISECOND, 0);

                calculatedEndDateCal.set(java.util.Calendar.HOUR_OF_DAY, 0);
                calculatedEndDateCal.set(java.util.Calendar.MINUTE, 0);
                calculatedEndDateCal.set(java.util.Calendar.SECOND, 0);
                calculatedEndDateCal.set(java.util.Calendar.MILLISECOND, 0);

                while (!currentDayCal.after(calculatedEndDateCal)) {
                    boolean shouldCreateEventsForThisDay = true;
                    if (reminder.getIsRecurring() && "Días específicos".equalsIgnoreCase(reminder.getFrequency())) {
                        String dayOfWeekName = getDayName(currentDayCal.get(java.util.Calendar.DAY_OF_WEEK));
                        if (reminder.getDays() == null || !reminder.getDays().contains(dayOfWeekName)) {
                            shouldCreateEventsForThisDay = false;
                        }
                    }

                    if (shouldCreateEventsForThisDay) {
                        for (String scheduleTime : reminder.getScheduleTimes()) {
                            try {
                                Date parsedTime = timeFormat.parse(scheduleTime);
                                java.util.Calendar eventDateTimeCal = (java.util.Calendar) currentDayCal.clone();
                                eventDateTimeCal.set(java.util.Calendar.HOUR_OF_DAY, parsedTime.getHours());
                                eventDateTimeCal.set(java.util.Calendar.MINUTE, parsedTime.getMinutes());
                                eventDateTimeCal.set(java.util.Calendar.SECOND, 0);
                                eventDateTimeCal.set(java.util.Calendar.MILLISECOND, 0);

                                long beginTimeMillis = eventDateTimeCal.getTimeInMillis();
                                long endTimeMillis = beginTimeMillis + (15 * 60 * 1000);

                                Event event = new Event()
                                        .setSummary("Tomar " + medication.getName())
                                        .setDescription("Recordatorio de medicamento: " + medication.getName() +
                                                "\nFrecuencia: " + reminder.getFrequency() +
                                                (reminder.getIsRecurring() && reminder.getDays() != null && !reminder.getDays().isEmpty() ? "\nDías: " + String.join(", ", reminder.getDays()) : ""));

                                EventDateTime start = new EventDateTime()
                                        .setDateTime(new DateTime(beginTimeMillis))
                                        .setTimeZone(deviceTimeZone.getID());
                                event.setStart(start);

                                EventDateTime end = new EventDateTime()
                                        .setDateTime(new DateTime(endTimeMillis))
                                        .setTimeZone(deviceTimeZone.getID());
                                event.setEnd(end);

                                Event createdEvent = mCalendarService.events().insert(appCalendarId, event).execute();
                                createdEventIds.add(createdEvent.getId());

                                programNotificationAlarm(
                                        getApplicationContext(),
                                        "Recordatorio de Medicamento",
                                        "Es hora de tomar " + medication.getName() + " a las " + scheduleTime,
                                        (int) (beginTimeMillis / 1000L),
                                        beginTimeMillis,currentReminder.getReminderId(), currentMedication.getMedicationId(), currentMedication.getTreatmentId() // Tiempo de disparo de la alarma
                                );

                                Log.d(TAG, "Evento creado: " + createdEvent.getSummary() + " en " + dateFormat.format(new Date(beginTimeMillis)) + " " + scheduleTime + " ID: " + createdEvent.getId());

                            } catch (ParseException e) {
                                Log.e(TAG, "Error al parsear hora de recordatorio '" + scheduleTime + "': " + e.getMessage());
                                mainHandler.post(() -> Toast.makeText(ReminderActivity.this, "Error en formato de hora: " + scheduleTime, Toast.LENGTH_LONG).show());
                            }
                        }
                    }
                    currentDayCal.add(java.util.Calendar.DATE, 1);
                }

                if (!createdEventIds.isEmpty()) {
                    db.collection("reminders").document(reminder.getReminderId())
                            .update("calendarEventIds", createdEventIds)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "IDs de eventos de calendario guardados en Firestore para recordatorio: " + reminder.getReminderId());
                                mainHandler.post(() -> Toast.makeText(ReminderActivity.this, "Eventos de calendario creados y guardados (" + createdEventIds.size() + ").", Toast.LENGTH_LONG).show());
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error al guardar IDs de eventos de calendario en Firestore: " + e.getMessage(), e);
                                mainHandler.post(() -> Toast.makeText(ReminderActivity.this, "Eventos de calendario creados, pero falló el guardado de IDs.", Toast.LENGTH_LONG).show());
                            });
                } else {
                    mainHandler.post(() -> Toast.makeText(ReminderActivity.this, "No se crearon eventos de calendario.", Toast.LENGTH_SHORT).show());
                }

            } catch (IOException e) {
                Log.e(TAG, "Error general al crear eventos de calendario: " + e.getMessage(), e);
                mainHandler.post(() -> Toast.makeText(ReminderActivity.this, "Error al crear eventos de calendario: " + e.getMessage(), Toast.LENGTH_LONG).show());
            } catch (Exception e) {
                Log.e(TAG, "Error inesperado al crear eventos de calendario: " + e.getMessage(), e);
                mainHandler.post(() -> Toast.makeText(ReminderActivity.this, "Error inesperado al crear eventos de calendario: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }
    private void programNotificationAlarm(Context context, String title, String message, int notificationId, long triggerTimeMillis,String reminderId,String medicationId,String treatmentId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!checkCanScheduleExactAlarms()) {
                Log.w(TAG, "No se puede programar alarma exacta. Solicitando permiso.");
                mainHandler.post(() -> Toast.makeText(context, "No se pudo programar el recordatorio exacto. Verifica los permisos de alarmas.", Toast.LENGTH_LONG).show());
                return;
            }
        }

        Intent notificationIntent = new Intent(context, ReminderAlarmReceiver.class);
        notificationIntent.setAction(NotificationHelper.ACTION_SHOW_REMINDER);
        notificationIntent.putExtra(NotificationHelper.EXTRA_REMINDER_TITLE, title);
        notificationIntent.putExtra(NotificationHelper.EXTRA_REMINDER_MESSAGE, message);
        notificationIntent.putExtra(NotificationHelper.EXTRA_NOTIFICATION_ID, notificationId);
        notificationIntent.putExtra(NotificationHelper.EXTRA_REMINDER_ID, reminderId);
        notificationIntent.putExtra(NotificationHelper.EXTRA_MEDICATION_ID, medicationId);
        notificationIntent.putExtra(NotificationHelper.EXTRA_TREATMENT_ID, treatmentId);
        notificationIntent.putExtra(NotificationHelper.EXTRA_TRIGGER_TIME_MILLIS, triggerTimeMillis);

        PendingIntent pendingIntentForAlarm = PendingIntent.getBroadcast(
                context,
                notificationId,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntentForAlarm);
                Log.d(TAG, "Alarma EXACTA programada para API 31+: ID " + notificationId + " a " + new Date(triggerTimeMillis));
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntentForAlarm);
                Log.d(TAG, "Alarma EXACTA programada para API 23+: ID " + notificationId + " a " + new Date(triggerTimeMillis));
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntentForAlarm);
                Log.d(TAG, "Alarma EXACTA programada para API < 23: ID " + notificationId + " a " + new Date(triggerTimeMillis));
            }
        }
    }
}