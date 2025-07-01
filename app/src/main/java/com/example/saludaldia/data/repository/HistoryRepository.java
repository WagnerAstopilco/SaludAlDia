package com.example.saludaldia.data.repository;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.saludaldia.data.model.History;
import com.example.saludaldia.data.model.HistoryEvent;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

public class HistoryRepository {
    private static final String TAG = "HistoryRepository";
    private static final String HISTORY_COLLECTION_NAME = "histories";
    private static final String HISTORY_EVENTS_COLLECTION_NAME = "historyEvents";
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final CollectionReference historyRef = db.collection(HISTORY_COLLECTION_NAME);
    private static final CollectionReference historyEventsRef = db.collection(HISTORY_EVENTS_COLLECTION_NAME);

    public static void createHistoryForUser(String userId, OnHistoryCreatedListener listener) {
        String historyId = userId;
        History history = new History(historyId, userId, new ArrayList<>());
        historyRef.document(historyId).set(history)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "History document created for user: " + userId);
                    listener.onSuccess(history);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating history document for user " + userId + ": " + e.getMessage(), e);
                    listener.onFailure(e);
                });
    }
    public static void getHistoryByUserId(String userId, OnHistoryLoadedListener listener) {
        historyRef.document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        History history = documentSnapshot.toObject(History.class);
                        Log.d(TAG, "History loaded for user: " + userId);
                        listener.onSuccess(history);
                    } else {
                        Log.d(TAG, "No history found for user: " + userId);
                        listener.onSuccess(null); // No hay historial
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading history for user " + userId + ": " + e.getMessage(), e);
                    listener.onFailure(e);
                });
    }
    public static Task<Void> addHistoryEventAndLinkToHistory(String userId, HistoryEvent newEvent) {
        if (newEvent.getEventId() == null || newEvent.getEventId().isEmpty()) {
            newEvent.setEventId(UUID.randomUUID().toString());
        }
        newEvent.setHistoryId(userId);
        Task<Void> addEventTask = historyEventsRef.document(newEvent.getEventId()).set(newEvent)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "HistoryEvent saved: " + newEvent.getEventId()))
                .addOnFailureListener(e -> Log.e(TAG, "Error saving HistoryEvent " + newEvent.getEventId() + ": " + e.getMessage(), e));
        Task<Void> updateHistoryTask = historyRef.document(userId)
                .update("eventsIds", FieldValue.arrayUnion(newEvent.getEventId()))
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Event ID " + newEvent.getEventId() + " added to history for user: " + userId))
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null && e.getMessage().contains("NOT_FOUND")) {
                        Log.w(TAG, "History document for user " + userId + " not found. Attempting to create it and add event ID.");
                        History newHistory = new History(userId, userId, Collections.singletonList(newEvent.getEventId()));
                        historyRef.document(userId).set(newHistory)
                                .addOnSuccessListener(createAclVoid -> Log.d(TAG, "New History document created with event ID for user: " + userId))
                                .addOnFailureListener(createError -> Log.e(TAG, "Failed to create new History document and add event ID for user " + userId + ": " + createError.getMessage(), createError));
                    } else {
                        Log.e(TAG, "Error updating history eventsIds for user " + userId + ": " + e.getMessage(), e);
                    }
                });
        return Tasks.whenAll(addEventTask, updateHistoryTask);
    }
    public static void updateHistory(History updatedHistory, OnHistoryUpdatedListener listener) {
        historyRef.document(updatedHistory.getHistoryId()).set(updatedHistory)
                .addOnSuccessListener(aVoid -> listener.onSuccess(updatedHistory))
                .addOnFailureListener(listener::onFailure);
    }
    public interface OnHistoryCreatedListener {
        void onSuccess(History history);
        void onFailure(@NonNull Exception e);
    }
    public interface OnHistoryLoadedListener {
        void onSuccess(@Nullable History history);
        void onFailure(@NonNull Exception e);
    }
    public interface OnHistoryEventLoadedListener {
        void onSuccess(@Nullable HistoryEvent event);
        void onFailure(@NonNull Exception e);
    }
    public interface OnHistoryUpdatedListener {
        void onSuccess(History history);
        void onFailure(@NonNull Exception e);
    }
}