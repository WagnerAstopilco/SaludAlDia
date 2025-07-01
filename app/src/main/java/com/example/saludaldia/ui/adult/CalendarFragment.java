package com.example.saludaldia.ui.adult;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.saludaldia.R;
import com.example.saludaldia.data.model.Medication;
import com.example.saludaldia.data.model.Reminder;
import com.example.saludaldia.utils.CalendarServiceManager;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.shrikanthravi.collapsiblecalendarview.data.Day;
import com.shrikanthravi.collapsiblecalendarview.widget.CollapsibleCalendar;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CalendarFragment extends Fragment implements CollapsibleCalendar.CalendarListener {

    private static final String TAG = "CalendarFragment";

    private CollapsibleCalendar customCalendarView;
    private TextView eventsListTextView;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final Map<Date, List<Event>> eventsByDay = new HashMap<>();

    private CalendarServiceManager.CalendarServiceListener calendarServiceListener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        customCalendarView = view.findViewById(R.id.customCalendarView);
        eventsListTextView = view.findViewById(R.id.eventsListTextView);
        customCalendarView.setCalendarListener(this);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        calendarServiceListener = new CalendarServiceManager.CalendarServiceListener() {
            @Override
            public void onCalendarServiceReady(Calendar calendarService, String calendarId) {
                Log.d(TAG, "onCalendarServiceReady (via Singleton): Calendar service and ID received in Fragment.");
                loadCalendarEvents();
            }

            @Override
            public void onCalendarServiceError(String message) {
                Log.e(TAG, "onCalendarServiceError (via Singleton): " + message);
                mainHandler.post(() -> {
                    Toast.makeText(requireContext(), "Error en el servicio de calendario: " + message, Toast.LENGTH_LONG).show();
                    eventsListTextView.setText("No se pudo cargar el calendario. " + message);
                });
            }
        };

        CalendarServiceManager.getInstance().addCalendarServiceListener(calendarServiceListener);

        if (CalendarServiceManager.getInstance().getCalendarService() != null &&
                CalendarServiceManager.getInstance().getAppCalendarId() != null) {
            Log.d(TAG, "Calendar service and ID already available from Singleton.");
            loadCalendarEvents();
        } else {
            Log.d(TAG, "Waiting for Calendar service to be initialized by Singleton.");
            eventsListTextView.setText("Cargando calendario...");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (CalendarServiceManager.getInstance().getCalendarService() != null &&
                CalendarServiceManager.getInstance().getAppCalendarId() != null) {
            loadCalendarEvents();
        }
    }

    private void loadCalendarEvents() {
        Calendar mCalendarService = CalendarServiceManager.getInstance().getCalendarService();
        String appCalendarId = CalendarServiceManager.getInstance().getAppCalendarId();

        if (mCalendarService == null || appCalendarId == null) {
            Log.e(TAG, "Calendar service or appCalendarId not initialized in Singleton. Cannot load events.");
            mainHandler.post(() -> eventsListTextView.setText("Servicio de calendario no disponible."));
            return;
        }

        executorService.execute(() -> {
            eventsByDay.clear();
            try {
                long now = System.currentTimeMillis();
                long sixMonthsAgo = now - (1000L * 60 * 60 * 24 * 30 * 6);
                long sixMonthsLater = now + (1000L * 60 * 60 * 24 * 30 * 6);
                DateTime timeMin = new DateTime(sixMonthsAgo);
                DateTime timeMax = new DateTime(sixMonthsLater);

                List<Event> googleEvents = mCalendarService.events().list(appCalendarId)
                        .setTimeMin(timeMin)
                        .setTimeMax(timeMax)
                        .setOrderBy("startTime")
                        .setSingleEvents(true)
                        .execute()
                        .getItems();

                mainHandler.post(() -> {
                    eventsByDay.clear();
                    ViewGroup parent = (ViewGroup) customCalendarView.getParent();
                    int index = parent.indexOfChild(customCalendarView);
                    parent.removeView(customCalendarView);

                    CollapsibleCalendar newCalendarView = new CollapsibleCalendar(requireContext());
                    newCalendarView.setSelectedItemTextColor(Color.parseColor("#2196F3"));
                    newCalendarView.setTodayItemTextColor(Color.parseColor("#4CAF50"));
                    newCalendarView.setSelectedItemBackgroundDrawable(ContextCompat.getDrawable(requireContext(), com.shrikanthravi.collapsiblecalendarview.R.drawable.circle_white_stroke_background));
                    newCalendarView.setTodayItemBackgroundDrawable(ContextCompat.getDrawable(requireContext(), com.shrikanthravi.collapsiblecalendarview.R.drawable.circle_white_stroke_background));
                    newCalendarView.setButtonLeftDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.arrow_left));
                    newCalendarView.setButtonLeftDrawableTintColor(ContextCompat.getColor(requireContext(), android.R.color.black));
                    newCalendarView.setButtonRightDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.arrow_right));
                    newCalendarView.setButtonRightDrawableTintColor(ContextCompat.getColor(requireContext(), android.R.color.black));
                    newCalendarView.setExpandIconColor(ContextCompat.getColor(requireContext(), android.R.color.black));
                    newCalendarView.setCalendarListener(this);
                    newCalendarView.expand(0);
                    customCalendarView = newCalendarView;
                    parent.addView(customCalendarView, index);

                    if (googleEvents != null && !googleEvents.isEmpty()) {
                        int eventDotColor = ContextCompat.getColor(requireContext(), R.color.your_event_dot_color);
                        for (Event googleEvent : googleEvents) {
                            DateTime start = googleEvent.getStart() != null ? googleEvent.getStart().getDateTime() : googleEvent.getStart().getDate();
                            if (start != null) {
                                java.util.Calendar cal = java.util.Calendar.getInstance();
                                cal.setTimeInMillis(start.getValue());
                                java.util.Calendar normalizedCal = (java.util.Calendar) cal.clone();
                                normalizedCal.set(java.util.Calendar.HOUR_OF_DAY, 0);
                                normalizedCal.set(java.util.Calendar.MINUTE, 0);
                                normalizedCal.set(java.util.Calendar.SECOND, 0);
                                normalizedCal.set(java.util.Calendar.MILLISECOND, 0);
                                Date normalizedDate = normalizedCal.getTime();
                                eventsByDay.computeIfAbsent(normalizedDate, k -> new ArrayList<>()).add(googleEvent);
                                customCalendarView.addEventTag(cal.get(java.util.Calendar.YEAR),
                                        cal.get(java.util.Calendar.MONTH),
                                        cal.get(java.util.Calendar.DAY_OF_MONTH),
                                        eventDotColor);
                            }
                        }
                        Day today = customCalendarView.getSelectedDay();
                        if (today != null) {
                            displayEventsForSelectedDay(today.getYear(), today.getMonth(), today.getDay());
                        } else {
                            java.util.Calendar currentCal = java.util.Calendar.getInstance();
                            displayEventsForSelectedDay(currentCal.get(java.util.Calendar.YEAR),
                                    currentCal.get(java.util.Calendar.MONTH),
                                    currentCal.get(java.util.Calendar.DAY_OF_MONTH));
                        }
                    } else {
                        eventsListTextView.setText("No hay eventos próximos en el calendario de SaludAlDia.");
                    }
                });
            } catch (IOException e) {
                Log.e(TAG, "Error loading calendar events from appCalendarId: " + e.getMessage(), e);
                mainHandler.post(() -> Toast.makeText(requireContext(), "Error al cargar eventos del calendario.", Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    public void onDaySelect() {
        Day selectedDay = customCalendarView.getSelectedDay();
        if (selectedDay != null) {
            Log.i(TAG, "Selected Day: " + selectedDay.getYear() + "/" + (selectedDay.getMonth() + 1) + "/" + selectedDay.getDay());
            displayEventsForSelectedDay(selectedDay.getYear(), selectedDay.getMonth(), selectedDay.getDay());
        }
    }

    @Override
    public void onItemClick(View view) {
    }

    @Override
    public void onDataUpdate() {}

    @Override
    public void onMonthChange() {
        Log.d(TAG, "Month Changed to: " + customCalendarView.getMonth() + "/" + customCalendarView.getYear());
    }

    @Override
    public void onWeekChange(int i) {
        Log.d(TAG, "Week Changed: " + i);
    }

    @Override
    public void onClickListener() {
        // customCalendarView.changeToToday();
    }

    private void displayEventsForSelectedDay(int year, int month, int dayOfMonth) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(year, month, dayOfMonth);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        Date normalizedSelectedDate = cal.getTime();

        List<Event> eventsForSelectedDay = eventsByDay.get(normalizedSelectedDate);
        if (eventsForSelectedDay != null && !eventsForSelectedDay.isEmpty()) {
            StringBuilder eventDetails = new StringBuilder();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            eventDetails.append("Eventos para ").append(sdf.format(normalizedSelectedDate)).append(":\n");
            for (Event event : eventsForSelectedDay) {
                eventDetails.append("- ").append(event.getSummary());
                if (event.getStart() != null && event.getStart().getDateTime() != null) {
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    eventDetails.append(" (").append(timeFormat.format(new Date(event.getStart().getDateTime().getValue()))).append(")");
                } else if (event.getStart() != null && event.getStart().getDate() != null) {
                    eventDetails.append(" (Todo el día)");
                }
                eventDetails.append("\n");
            }
            eventsListTextView.setText(eventDetails.toString());
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            eventsListTextView.setText("No hay eventos para el " + sdf.format(normalizedSelectedDate));
        }
    }

    public void addCalendarEvent(String summary, String description, Date startTime, Date endTime) {
        final Calendar mCalendarService = CalendarServiceManager.getInstance().getCalendarService();
        final String appCalendarId = CalendarServiceManager.getInstance().getAppCalendarId();

        if (mCalendarService == null || appCalendarId == null) {
            Toast.makeText(requireContext(), "Servicio de calendario no disponible para añadir evento.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Calendar service or appCalendarId not initialized in Singleton. Cannot add event.");
            return;
        }

        executorService.execute(() -> {
            Event event = new Event()
                    .setSummary(summary)
                    .setDescription(description);

            DateTime startDateTime = new DateTime(startTime);
            EventDateTime start = new EventDateTime().setDateTime(startDateTime).setTimeZone(java.util.TimeZone.getDefault().getID());
            event.setStart(start);

            DateTime endDateTime = new DateTime(endTime);
            EventDateTime end = new EventDateTime().setDateTime(endDateTime).setTimeZone(java.util.TimeZone.getDefault().getID());
            event.setEnd(end);

            try {
                Event createdEvent = mCalendarService.events().insert(appCalendarId, event).execute();
                Log.d(TAG, "Event created: " + createdEvent.getHtmlLink());
                mainHandler.post(() -> {
                    Toast.makeText(requireContext(), "Evento '" + summary + "' añadido al calendario.", Toast.LENGTH_LONG).show();
                    loadCalendarEvents();
                });
            } catch (IOException e) {
                Log.e(TAG, "Error adding event to calendar: " + e.getMessage(), e);
                mainHandler.post(() -> Toast.makeText(requireContext(), "Error al añadir evento al calendario: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        CalendarServiceManager.getInstance().removeCalendarServiceListener(calendarServiceListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }

    @Override
    public void onDayChanged() {
    }

    public interface OnMedicationEventsAddedListener {
        void onSuccess(List<String> createdEventIds);
        void onFailure(Exception e);
    }

    public void addMedicationRemindersToGoogleCalendar(
            Reminder reminder,
            Medication medication,
            @NonNull OnMedicationEventsAddedListener listener) {

        final Calendar mCalendarService = CalendarServiceManager.getInstance().getCalendarService();
        final String appCalendarId = CalendarServiceManager.getInstance().getAppCalendarId();

        if (mCalendarService == null || appCalendarId == null) {
            Log.e(TAG, "ERROR: mCalendarService o appCalendarId es NULL al inicio de addMedicationRemindersToGoogleCalendar.");
            mainHandler.post(() -> listener.onFailure(new IllegalStateException("Servicio de calendario o ID no inicializado.")));
            return;
        }
        Log.d(TAG, "addMedicationRemindersToGoogleCalendar: Servicio de calendario y ID disponibles. appCalendarId: " + appCalendarId);
        if (reminder == null || medication == null) {
            mainHandler.post(() -> listener.onFailure(new IllegalArgumentException("Recordatorio y medicación no pueden ser nulos.")));
            return;
        }
        if (reminder.getScheduleTimes() == null || reminder.getScheduleTimes().isEmpty()) {
            mainHandler.post(() -> listener.onFailure(new IllegalArgumentException("Los tiempos de programación del recordatorio no pueden estar vacíos.")));
            return;
        }

        executorService.execute(() -> {
            List<String> createdEventIds = new ArrayList<>();
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            TimeZone deviceTimeZone = TimeZone.getDefault();

            try {
                java.util.Calendar treatmentStartCal = java.util.Calendar.getInstance();
                treatmentStartCal.setTimeInMillis(reminder.getStartDate().getTime());
                treatmentStartCal.set(java.util.Calendar.HOUR_OF_DAY, 0);
                treatmentStartCal.set(java.util.Calendar.MINUTE, 0);
                treatmentStartCal.set(java.util.Calendar.SECOND, 0);
                treatmentStartCal.set(java.util.Calendar.MILLISECOND, 0);
                Log.d(TAG, "addMedicationRemindersToGoogleCalendar: Duración de la medicación (días): " + medication.getNumber_days());
                Log.d(TAG, "addMedicationRemindersToGoogleCalendar: Fecha de inicio del tratamiento: " + reminder.getStartDate());

                for (int dayOffset = 0; dayOffset < medication.getNumber_days(); dayOffset++) {
                    java.util.Calendar currentEventDayCal = (java.util.Calendar) treatmentStartCal.clone();
                    currentEventDayCal.add(java.util.Calendar.DATE, dayOffset);

                    Log.d(TAG, "addMedicationRemindersToGoogleCalendar: Procesando día: " + currentEventDayCal.getTime());

                    for (String scheduleTime : reminder.getScheduleTimes()) {
                        try {
                            Date parsedTime = timeFormat.parse(scheduleTime);
                            java.util.Calendar eventDateTimeCal = (java.util.Calendar) currentEventDayCal.clone();
                            eventDateTimeCal.set(java.util.Calendar.HOUR_OF_DAY, parsedTime.getHours());
                            eventDateTimeCal.set(java.util.Calendar.MINUTE, parsedTime.getMinutes());
                            eventDateTimeCal.set(java.util.Calendar.SECOND, 0);
                            eventDateTimeCal.set(java.util.Calendar.MILLISECOND, 0);

                            long beginTimeMillis = eventDateTimeCal.getTimeInMillis();
                            long endTimeMillis = beginTimeMillis + (10 * 60 * 1000);

                            Event event = new Event()
                                    .setSummary("Tomar " + medication.getName())
                                    .setDescription("Recordatorio de medicamento: " + medication.getName());

                            EventDateTime start = new EventDateTime()
                                    .setDateTime(new DateTime(beginTimeMillis))
                                    .setTimeZone(deviceTimeZone.getID());
                            event.setStart(start);

                            EventDateTime end = new EventDateTime()
                                    .setDateTime(new DateTime(endTimeMillis))
                                    .setTimeZone(deviceTimeZone.getID());
                            event.setEnd(end);

                            Log.d(TAG, "addMedicationRemindersToGoogleCalendar: Intentando insertar evento: " + event.getSummary() + " en " + new Date(beginTimeMillis));

                            Event createdEvent = mCalendarService.events().insert(appCalendarId, event).execute();
                            createdEventIds.add(createdEvent.getId());
                            Log.d(TAG, "Evento creado: " + createdEvent.getSummary() + " en " + new Date(beginTimeMillis) + " ID: " + createdEvent.getId());
                            Log.d(TAG, "addMedicationRemindersToGoogleCalendar: Evento creado exitosamente. ID: " + createdEvent.getId());

                        } catch (ParseException e) {
                            Log.e(TAG, "Error al parsear hora de recordatorio '" + scheduleTime + "': " + e.getMessage());
                            mainHandler.post(() -> listener.onFailure(new ParseException("Formato de hora inválido: " + scheduleTime, 0)));
                            return;
                        }
                    }
                }
                mainHandler.post(() -> {
                    Log.d(TAG, "addMedicationRemindersToGoogleCalendar: Llamando onSuccess con IDs: " + createdEventIds.size() + " eventos. " + createdEventIds.toString());
                    listener.onSuccess(createdEventIds);
                    loadCalendarEvents();
                });

            } catch (IOException e) {
                Log.e(TAG, "Error adding events to Google Calendar: " + e.getMessage(), e);
                mainHandler.post(() -> listener.onFailure(e));
            } catch (Exception e) {
                Log.e(TAG, "Error inesperado al añadir eventos al calendario: " + e.getMessage(), e);
                mainHandler.post(() -> listener.onFailure(e));
            }
        });
    }

    public interface OnCalendarEventsDeletedListener {
        void onSuccess();
        void onFailure(Exception e);
    }

    public void deleteCalendarEvents(List<String> eventIdsToDelete, @NonNull OnCalendarEventsDeletedListener listener) {
        final Calendar mCalendarService = CalendarServiceManager.getInstance().getCalendarService();
        final String appCalendarId = CalendarServiceManager.getInstance().getAppCalendarId();

        if (mCalendarService == null || appCalendarId == null) {
            mainHandler.post(() -> listener.onFailure(new IllegalStateException("Servicio de calendario o ID no inicializado.")));
            return;
        }
        if (eventIdsToDelete == null || eventIdsToDelete.isEmpty()) {
            mainHandler.post(() -> listener.onSuccess());
            return;
        }

        executorService.execute(() -> {
            try {
                for (String eventId : eventIdsToDelete) {
                    mCalendarService.events().delete(appCalendarId, eventId).execute();
                    Log.d(TAG, "Evento eliminado: " + eventId);
                }
                mainHandler.post(() -> {
                    listener.onSuccess();
                    loadCalendarEvents();
                });
            } catch (IOException e) {
                Log.e(TAG, "Error deleting calendar events: " + e.getMessage(), e);
                mainHandler.post(() -> listener.onFailure(e));
            } catch (Exception e) {
                Log.e(TAG, "Error inesperado al eliminar eventos del calendario: " + e.getMessage(), e);
                mainHandler.post(() -> listener.onFailure(e));
            }
        });
    }
}