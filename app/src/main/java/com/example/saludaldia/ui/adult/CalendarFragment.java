package com.example.saludaldia.ui.adult;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.saludaldia.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.Calendar;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.CalendarScopes;


import com.shrikanthravi.collapsiblecalendarview.data.Day;
import com.shrikanthravi.collapsiblecalendarview.widget.CollapsibleCalendar;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CalendarFragment extends Fragment implements CollapsibleCalendar.CalendarListener {

    private static final String TAG = "CalendarFragment";

    private CollapsibleCalendar customCalendarView;
    private TextView eventsListTextView;

    private GoogleSignInAccount mGoogleAccount;
    private Calendar mCalendarService;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final Map<Date, List<com.google.api.services.calendar.model.Event>> eventsByDay = new HashMap<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Infla el layout del fragmento. 'view' es la vista raíz.
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        // AHORA sí puedes usar findViewById en la vista 'view' que acabas de inflar.
        customCalendarView = view.findViewById(R.id.customCalendarView); // Asegúrate que el ID sea customCalendarView, no calendarView
        eventsListTextView = view.findViewById(R.id.eventsListTextView);

        // Establece el listener de la librería en tu instancia de CollapsibleCalendar
        // 'this' se refiere a este mismo CalendarFragment, que implementa CalendarListener
        customCalendarView.setCalendarListener(this);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeGoogleCalendarAPI();
    }

    private void initializeGoogleCalendarAPI() {
        if (getActivity() != null) {
            String googleAccountId = getActivity().getIntent().getStringExtra("google_account_id");
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(requireContext());

            if (account != null && account.getId() != null && account.getId().equals(googleAccountId)) {
                mGoogleAccount = account;
                Log.d(TAG, "Google Account found for Calendar API: " + mGoogleAccount.getEmail());

                GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                        requireContext(), Collections.singletonList(CalendarScopes.CALENDAR_EVENTS));
                credential.setSelectedAccount(mGoogleAccount.getAccount());

                mCalendarService = new Calendar.Builder(
                        new NetHttpTransport(), JacksonFactory.getDefaultInstance(), credential)
                        .setApplicationName(getString(com.example.saludaldia.R.string.app_name))
                        .build();

                loadCalendarEvents();
            } else {
                Log.e(TAG, "Google Account not available for Calendar API.");
                eventsListTextView.setText("No se pudo cargar el calendario. Por favor, inicie sesión con Google.");
            }
        }
    }

    private void loadCalendarEvents() {
        if (mCalendarService == null) {
            Log.e(TAG, "Calendar service is not initialized.");
            return;
        }

        executorService.execute(() -> {
            try {
                long now = System.currentTimeMillis();
                long sixMonthsAgo = now - (1000L * 60 * 60 * 24 * 30 * 6);
                long sixMonthsLater = now + (1000L * 60 * 60 * 24 * 30 * 6);

                DateTime timeMin = new DateTime(sixMonthsAgo);
                DateTime timeMax = new DateTime(sixMonthsLater);

                List<com.google.api.services.calendar.model.Event> googleEvents = mCalendarService.events().list("primary")
                        .setTimeMin(timeMin)
                        .setTimeMax(timeMax)
                        .setOrderBy("startTime")
                        .setSingleEvents(true)
                        .execute()
                        .getItems();

                if (googleEvents != null && !googleEvents.isEmpty()) {
                    eventsByDay.clear();

                    requireActivity().runOnUiThread(() -> {
                        // Limpiar tags de eventos existentes antes de añadir los nuevos
                        // Asumo que CollapsibleCalendar tiene un método clearEventTags() o similar.
                        // Si no lo tiene, es un punto a considerar para la UX.
                        // customCalendarView.clearEventTags(); // Descomenta si existe.

                        int eventDotColor = getResources().getColor(R.color.your_event_dot_color);

                        for (com.google.api.services.calendar.model.Event googleEvent : googleEvents) {
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
                    });
                } else {
                    requireActivity().runOnUiThread(() -> eventsListTextView.setText("No hay eventos próximos en tu calendario."));
                }
            } catch (IOException e) {
                Log.e(TAG, "Error loading calendar events: " + e.getMessage(), e);
                requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Error al cargar eventos del calendario.", Toast.LENGTH_SHORT).show());
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
    public void onDataUpdate() {

    }

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
    }

    private void displayEventsForSelectedDay(int year, int month, int dayOfMonth) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(year, month, dayOfMonth);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        Date normalizedSelectedDate = cal.getTime();

        List<com.google.api.services.calendar.model.Event> eventsForSelectedDay = eventsByDay.get(normalizedSelectedDate);
        if (eventsForSelectedDay != null && !eventsForSelectedDay.isEmpty()) {
            StringBuilder eventDetails = new StringBuilder();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            eventDetails.append("Eventos para ").append(sdf.format(normalizedSelectedDate)).append(":\n");
            for (com.google.api.services.calendar.model.Event event : eventsForSelectedDay) {
                eventDetails.append("- ").append(event.getSummary());
                if (event.getStart() != null && event.getStart().getDateTime() != null) {
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    eventDetails.append(" (").append(timeFormat.format(new Date(event.getStart().getDateTime().getValue()))).append(")");
                }
                eventDetails.append("\n");
            }
            eventsListTextView.setText(eventDetails.toString());
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            eventsListTextView.setText("No hay eventos para el " + sdf.format(normalizedSelectedDate));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }

    @Override
    public void onDayChanged() {

    }
}