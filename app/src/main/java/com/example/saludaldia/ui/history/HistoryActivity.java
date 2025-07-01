package com.example.saludaldia.ui.history;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.saludaldia.R;
import com.example.saludaldia.adapter.HistoryEventsAdapter;
import com.example.saludaldia.data.model.History;
import com.example.saludaldia.data.model.HistoryEvent;
import com.example.saludaldia.data.repository.HistoryRepository;
import com.example.saludaldia.ui.toolbar.AdultToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private static final String TAG = "HistoryActivity";
    private RecyclerView recyclerView;
    private TextView tvNoHistoryEvents;
    private HistoryEventsAdapter adapter;
    private List<HistoryEvent> historyEventsList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        AdultToolbar.setup(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.history_activity_title));
        }

        recyclerView = findViewById(R.id.rv_history_events);
        tvNoHistoryEvents = findViewById(R.id.tv_no_history_events);

        historyEventsList = new ArrayList<>();
        adapter = new HistoryEventsAdapter(historyEventsList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        loadHistoryEvents();
    }

    private void loadHistoryEvents() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Debe iniciar sesión para ver el historial de eventos.", Toast.LENGTH_LONG).show();
            tvNoHistoryEvents.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            return;
        }

        String userId = currentUser.getUid();

        HistoryRepository.getHistoryByUserId(userId, new HistoryRepository.OnHistoryLoadedListener() {
            @Override
            public void onSuccess(@Nullable History history) {
                if (history != null && history.getEventsIds() != null && !history.getEventsIds().isEmpty()) {
                    List<String> eventIds = history.getEventsIds();
                    Log.d(TAG, "Found " + eventIds.size() + " event IDs in history for user: " + userId);
                    fetchAndDisplayHistoryEvents(userId);

                } else {
                    Log.d(TAG, "No event IDs found in history for user: " + userId);
                    tvNoHistoryEvents.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    adapter.setEventList(new ArrayList<>()); // Limpia la lista si no hay eventos
                }
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Error fetching History document for user " + userId + ": " + e.getMessage(), e);
                Toast.makeText(HistoryActivity.this, "Error al cargar historial principal.", Toast.LENGTH_SHORT).show();
                tvNoHistoryEvents.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        });
    }

    private void fetchAndDisplayHistoryEvents(String userId) {
        db.collection("historyEvents")
                .whereEqualTo("historyId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    historyEventsList.clear();
                    for (HistoryEvent event : queryDocumentSnapshots.toObjects(HistoryEvent.class)) {
                        Log.d(TAG,"EVENTO: "+event.getHistoryId());
                        historyEventsList.add(event);
                    }
                    Log.d(TAG,"EVENTO lissta: "+historyEventsList);
                    adapter.setEventList(historyEventsList); // Actualiza el adaptador
                    Log.d(TAG,"EVENTO lissta: "+historyEventsList);

                    if (historyEventsList.isEmpty()) {
                        tvNoHistoryEvents.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        Log.d(TAG, "No se encontraron eventos detallados en historyEvents para el usuario.");
                    } else {
                        tvNoHistoryEvents.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        Log.d(TAG, "Eventos detallados cargados. Total: " + historyEventsList.size());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar eventos detallados de historial: " + e.getMessage(), e);
                    Toast.makeText(this, "Error al cargar eventos de historial.", Toast.LENGTH_SHORT).show();
                    tvNoHistoryEvents.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                });
    }
}