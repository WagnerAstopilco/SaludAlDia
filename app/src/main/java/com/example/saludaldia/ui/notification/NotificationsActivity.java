package com.example.saludaldia.ui.notification;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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
    private TextView tvStatsTitle;
    private LinearLayout llProgressBarContainer;
    private TextView tvComplianceSummary;

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
        tvStatsTitle = findViewById(R.id.tv_stats_title);
        llProgressBarContainer = findViewById(R.id.ll_progress_bar_container);
        tvComplianceSummary = findViewById(R.id.tv_compliance_summary);

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
            tvStatsTitle.setVisibility(View.GONE);
            llProgressBarContainer.setVisibility(View.GONE);
            tvComplianceSummary.setVisibility(View.GONE);
            return;
        }

        String userId = currentUser.getUid();

        db.collection("notifications")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    notifications.clear();
                    int completedCount = 0;
                    int dismissedCount = 0;
                    int totalNotifications = 0;

                    for (Notification notification : queryDocumentSnapshots.toObjects(Notification.class)) {
                        notifications.add(notification);
                        totalNotifications++;

                        if (notification.getCompleted()) {
                            completedCount++;
                        } else if (notification.getDismissed()) {
                            dismissedCount++;
                        }
                    }

                    adapter.setNotificationList(notifications);

                    if (notifications.isEmpty()) {
                        tvNoNotifications.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        tvStatsTitle.setVisibility(View.GONE);
                        llProgressBarContainer.setVisibility(View.GONE);
                        tvComplianceSummary.setVisibility(View.GONE);
                    } else {
                        tvNoNotifications.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        tvStatsTitle.setVisibility(View.VISIBLE);
                        llProgressBarContainer.setVisibility(View.VISIBLE);
                        tvComplianceSummary.setVisibility(View.VISIBLE);

                        updateComplianceGraph(completedCount, dismissedCount, totalNotifications);
                    }
                    Log.d(TAG, "Historial de notificaciones cargado. Total: " + notifications.size() +
                            ", Completadas: " + completedCount + ", Ignoradas: " + dismissedCount);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar el historial de notificaciones: " + e.getMessage(), e);
                    Toast.makeText(this, "Error al cargar el historial de notificaciones.", Toast.LENGTH_SHORT).show();
                    tvNoNotifications.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    tvStatsTitle.setVisibility(View.GONE);
                    llProgressBarContainer.setVisibility(View.GONE);
                    tvComplianceSummary.setVisibility(View.GONE);
                });
    }

    private void updateComplianceGraph(int completedCount, int dismissedCount, int totalNotifications) {
        llProgressBarContainer.removeAllViews();

        if (totalNotifications == 0) {
            tvComplianceSummary.setText("No hay notificaciones para mostrar estadísticas.");
            return;
        }

        float completedPercentage = (float) completedCount / totalNotifications * 100;
        float dismissedPercentage = (float) dismissedCount / totalNotifications * 100;
        float pendingOrUnansweredPercentage = 100f - completedPercentage - dismissedPercentage;

        if (completedPercentage < 0) completedPercentage = 0;
        if (dismissedPercentage < 0) dismissedPercentage = 0;
        if (pendingOrUnansweredPercentage < 0) pendingOrUnansweredPercentage = 0;

        float totalActualPercentage = completedPercentage + dismissedPercentage + pendingOrUnansweredPercentage;
        if (totalActualPercentage > 100.001f || totalActualPercentage < 99.999f) {
            Log.w(TAG, "Advertencia: La suma de porcentajes no es 100%: " + totalActualPercentage);
            pendingOrUnansweredPercentage = 100f - completedPercentage - dismissedPercentage;
            if (pendingOrUnansweredPercentage < 0) pendingOrUnansweredPercentage = 0;
        }

        if (completedPercentage > 0) {
            View completedView = new View(this);
            LinearLayout.LayoutParams completedParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, completedPercentage);
            completedView.setLayoutParams(completedParams);
            completedView.setBackgroundColor(ContextCompat.getColor(this, R.color.green_completed));
            llProgressBarContainer.addView(completedView);
        }

        if (dismissedPercentage > 0) {
            View dismissedView = new View(this);
            LinearLayout.LayoutParams dismissedParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, dismissedPercentage);
            dismissedView.setLayoutParams(dismissedParams);
            dismissedView.setBackgroundColor(ContextCompat.getColor(this, R.color.red_dismissed));
            llProgressBarContainer.addView(dismissedView);
        }

        if (pendingOrUnansweredPercentage > 0) {
            View pendingView = new View(this);
            LinearLayout.LayoutParams pendingParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, pendingOrUnansweredPercentage);
            pendingView.setLayoutParams(pendingParams);
            pendingView.setBackgroundColor(ContextCompat.getColor(this, R.color.grey_pending));
            llProgressBarContainer.addView(pendingView);
        }

        String summary = String.format("Completadas: %.0f%% | Ignoradas: %.0f%% | Sin responder: %.0f%%",
                completedPercentage, dismissedPercentage, pendingOrUnansweredPercentage);
        tvComplianceSummary.setText(summary);
    }
}