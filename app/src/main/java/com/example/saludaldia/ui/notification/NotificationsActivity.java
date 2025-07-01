package com.example.saludaldia.ui.notification;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.saludaldia.R;
import com.example.saludaldia.adapter.NotificationAdapter;
import com.example.saludaldia.data.model.Notification;
import com.example.saludaldia.ui.toolbar.AdultToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private static final String TAG = "NotificationsActivity";
    private RecyclerView recyclerView;
    private TextView tvNoNotifications;
    private NotificationAdapter adapter;
    private List<Notification> notifications;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        AdultToolbar.setup(this);
        if (this.getSupportActionBar() != null) {
            this.getSupportActionBar().setTitle(getString(R.string.notifications_activity_title));
        }

        recyclerView = findViewById(R.id.rv_notifications_history);
        tvNoNotifications = findViewById(R.id.tv_no_notifications);

        notifications = new ArrayList<>();
        adapter = new NotificationAdapter(notifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        loadNotificationHistory();
    }

    private void loadNotificationHistory() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Debe iniciar sesión para ver el historial de notificaciones.", Toast.LENGTH_LONG).show();
            tvNoNotifications.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            return;
        }

        String userId = currentUser.getUid();

        db.collection("notifications")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    notifications.clear();
                    for (Notification notification : queryDocumentSnapshots.toObjects(Notification.class)) {
                        notifications.add(notification);
                    }
                    adapter.setNotificationList(notifications);

                    if (notifications.isEmpty()) {
                        tvNoNotifications.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        tvNoNotifications.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                    Log.d(TAG, "Historial de notificaciones cargado. Total: " + notifications.size());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar el historial de notificaciones: " + e.getMessage(), e);
                    Toast.makeText(this, "Error al cargar el historial de notificaciones.", Toast.LENGTH_SHORT).show();
                    tvNoNotifications.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                });
    }
}