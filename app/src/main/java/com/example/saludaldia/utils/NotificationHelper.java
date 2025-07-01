package com.example.saludaldia.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import com.example.saludaldia.R;
import com.example.saludaldia.ui.ReminderActivity;
import com.example.saludaldia.receivers.NotificationActionReceiver;

public class NotificationHelper {

    private static final String CHANNEL_ID = "medication_reminder_channel";
    private static final String CHANNEL_NAME = "Recordatorios de Medicamentos";
    private static final String CHANNEL_DESCRIPTION = "Notificaciones para recordatorios de medicamentos";
    private static final String TAG = "NotificationHelper";
    public static final String ACTION_COMPLETE = "com.example.saludaldia.ACTION_COMPLETE_MEDICATION";
    public static final String ACTION_SNOOZE = "com.example.saludaldia.ACTION_SNOOZE_MEDICATION";
    public static final String ACTION_SHOW_REMINDER = "com.example.saludaldia.ACTION_SHOW_REMINDER";
    public static final String EXTRA_REMINDER_TITLE = "extra_reminder_title";
    public static final String EXTRA_REMINDER_MESSAGE = "extra_reminder_message";
    public static final String EXTRA_NOTIFICATION_ID = "extra_notification_id";
    public static final String EXTRA_REMINDER_ID = "extra_reminder_id";
    public static final String EXTRA_MEDICATION_ID = "extra_medication_id";
    public static final String EXTRA_TREATMENT_ID = "extra_treatment_id";
    public static final String EXTRA_TRIGGER_TIME_MILLIS = "extra_trigger_time_millis";
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public static void showNotification(Context context, String title, String message, int notificationId,String reminderId, String medicationId, String treatmentId) {
        Log.d(TAG, "showNotification: Intentando mostrar notificación para ID: " + notificationId + ", Título: '" + title + "'");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "showNotification: FALLO - Permiso POST_NOTIFICATIONS no concedido. No se puede mostrar la notificación.");
                return;
            } else {
                Log.d(TAG, "showNotification: Permiso POST_NOTIFICATIONS concedido. Continuando...");
            }
        }

        Intent notificationClickIntent = new Intent(context, ReminderActivity.class);
        notificationClickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        notificationClickIntent.putExtra("EXTRA_TREATMENT_ID", treatmentId); // Reemplaza esto
        notificationClickIntent.putExtra("EXTRA_REMINDER_ID", reminderId); // Si ReminderActivity lo necesita
        notificationClickIntent.putExtra("EXTRA_MEDICATION_ID", medicationId); // Si ReminderActivity lo necesita

        PendingIntent pendingIntentForClick = PendingIntent.getActivity(
                context,
                notificationId, // Usar el ID de la notificación como requestCode
                notificationClickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent completeIntent = new Intent(context, NotificationActionReceiver.class);
        completeIntent.setAction(ACTION_COMPLETE);
        completeIntent.putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId);
        PendingIntent completePendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId + 1, // **requestCode diferente** para 'Completar'
                completeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent snoozeIntent = new Intent(context, NotificationActionReceiver.class);
        snoozeIntent.setAction(ACTION_SNOOZE);
        snoozeIntent.putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId);
        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId + 2,
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.notification) // Tu ícono pequeño para la notificación
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntentForClick)
                .setAutoCancel(true)
                .addAction(R.drawable.check_circle, "Completar", completePendingIntent)
                .addAction(R.drawable.snooze, "Posponer", snoozePendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            notificationManager.notify(notificationId, builder.build());
            Log.d(TAG, "showNotification: ÉXITO - Notificación mostrada para ID: " + notificationId);
        } catch (SecurityException e) {
            Log.e(TAG, "showNotification: FALLO - SecurityException al mostrar notificación: " + e.getMessage(), e);
        } catch (Exception e) {
            Log.e(TAG, "showNotification: FALLO - Error inesperado al mostrar notificación: " + e.getMessage(), e);
        }
    }
}