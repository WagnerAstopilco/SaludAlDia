package com.example.saludaldia.utils;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.google.api.services.calendar.Calendar;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CalendarServiceManager {

    private static final String TAG = "CalendarServiceManager"; // Para logs
    private static CalendarServiceManager instance;
    private Calendar mCalendarService;
    private String appCalendarId;
    private final List<CalendarServiceListener> listeners = new ArrayList<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private CalendarServiceManager() {
    }

    public static synchronized CalendarServiceManager getInstance() {
        if (instance == null) {
            instance = new CalendarServiceManager();
        }
        return instance;
    }

    public void setCalendarService(Calendar calendarService, String calendarId) {
        this.mCalendarService = calendarService;
        this.appCalendarId = calendarId;
        if (calendarService != null && calendarId != null) {
            Log.d(TAG, "CalendarService inicializado y listo.");
            for (CalendarServiceListener listener : listeners) {
                listener.onCalendarServiceReady(calendarService, calendarId);
            }
        } else {
            String errorMessage = "Servicio de calendario o ID es nulo. Falló la inicialización.";
            Log.e(TAG, errorMessage);
            for (CalendarServiceListener listener : listeners) {
                listener.onCalendarServiceError(errorMessage);
            }
        }
    }
    public Calendar getCalendarService() {
        return mCalendarService;
    }

    public String getAppCalendarId() {
        return appCalendarId;
    }

    public void addCalendarServiceListener(CalendarServiceListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
            if (mCalendarService != null && appCalendarId != null) {
                listener.onCalendarServiceReady(mCalendarService, appCalendarId);
            } else {
                listener.onCalendarServiceError("Servicio de calendario aún no inicializado o con errores previos.");
            }
        }
    }

    public void removeCalendarServiceListener(CalendarServiceListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    public interface CalendarServiceListener {
        void onCalendarServiceReady(Calendar calendarService, String calendarId);

        void onCalendarServiceError(String message);
    }

    public interface OnCalendarEventsDeletedListener {
        void onSuccess();

        void onFailure(Exception e);
    }

    public void deleteCalendarEvents(List<String> eventIdsToDelete, OnCalendarEventsDeletedListener listener) {
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
                    if (eventId != null && !eventId.trim().isEmpty()) {
                        mCalendarService.events().delete(appCalendarId, eventId).execute();
                        Log.d(TAG, "Evento eliminado: " + eventId);
                    }
                }
                mainHandler.post(() -> listener.onSuccess());
            } catch (IOException e) {
                Log.e(TAG, "Error al eliminar eventos del calendario: " + e.getMessage(), e);
                mainHandler.post(() -> listener.onFailure(e));
            } catch (Exception e) {
                Log.e(TAG, "Error inesperado al eliminar eventos del calendario: " + e.getMessage(), e);
                mainHandler.post(() -> listener.onFailure(e));
            }
        });
    }

    public void shutdownExecutor() {
        if (!executorService.isShutdown()) {
            executorService.shutdown();
            Log.d(TAG, "CalendarServiceManager executor service shut down.");
        }
    }
}