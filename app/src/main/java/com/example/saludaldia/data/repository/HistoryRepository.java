package com.example.saludaldia.data.repository;

import androidx.annotation.NonNull;

import com.example.saludaldia.data.model.History;
import com.example.saludaldia.data.model.HistoryEvent;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class HistoryRepository {

    private static final String COLLECTION_NAME = "histories";
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final CollectionReference historyRef = db.collection(COLLECTION_NAME);

    // Crear historial vacío para un usuario
    public static void createHistoryForUser(String userId, OnHistoryCreatedListener listener) {
        String historyId = UUID.randomUUID().toString();
        History history = new History(historyId, userId, null); // eventos nulos inicialmente
        historyRef.document(historyId).set(history)
                .addOnSuccessListener(aVoid -> listener.onSuccess(history))
                .addOnFailureListener(listener::onFailure);
    }

    // Obtener historial de un usuario
    public static void getHistoryByUserId(String userId, OnHistoryLoadedListener listener) {
        historyRef.whereEqualTo("userId", userId).get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        History history = querySnapshot.getDocuments().get(0).toObject(History.class);
                        listener.onSuccess(history);
                    } else {
                        listener.onSuccess(null); // No hay historial
                    }
                })
                .addOnFailureListener(listener::onFailure);
    }

    // Agregar un evento al historial
    public static void addEventToHistory(String historyId, HistoryEvent event, OnEventAddedListener listener) {
        DocumentReference docRef = historyRef.document(historyId);
        String eventId = UUID.randomUUID().toString();
        event.setEventId(eventId);
        event.setHistoryId(historyId);
        event.setTimestamp(new Date());

        docRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                History history = snapshot.toObject(History.class);
                List<HistoryEvent> events = history.getEvents();
                if (events != null) {
                    events.add(event);
                } else {
                    events = List.of(event);
                }
                history.setEvents(events);
                docRef.set(history)
                        .addOnSuccessListener(unused -> listener.onSuccess(event))
                        .addOnFailureListener(listener::onFailure);
            } else {
                listener.onFailure(new Exception("Historial no encontrado"));
            }
        }).addOnFailureListener(listener::onFailure);
    }

    // Actualizar historial completo
    public static void updateHistory(History updatedHistory, OnHistoryUpdatedListener listener) {
        historyRef.document(updatedHistory.getHistoryId()).set(updatedHistory)
                .addOnSuccessListener(aVoid -> listener.onSuccess(updatedHistory))
                .addOnFailureListener(listener::onFailure);
    }

    // Interfaces de callback
    public interface OnHistoryCreatedListener {
        void onSuccess(History history);

        void onFailure(@NonNull Exception e);
    }

    public interface OnHistoryLoadedListener {
        void onSuccess(History history);

        void onFailure(@NonNull Exception e);
    }

    public interface OnEventAddedListener {
        void onSuccess(HistoryEvent event);

        void onFailure(@NonNull Exception e);
    }

    public interface OnHistoryUpdatedListener {
        void onSuccess(History history);

        void onFailure(@NonNull Exception e);
    }
}
