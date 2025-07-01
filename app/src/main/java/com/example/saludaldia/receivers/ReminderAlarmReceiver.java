package com.example.saludaldia.receivers;

// Importaciones necesarias para el funcionamiento del BroadcastReceiver
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.example.saludaldia.data.model.Notification;
import com.example.saludaldia.utils.NotificationHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.UUID;

/**
 * ReminderAlarmReceiver es un BroadcastReceiver que maneja las alarmas de recordatorios.
 * Este receptor se activa cuando se recibe una alarma para mostrar un recordatorio,
 * y se encarga de mostrar la notificación y registrar la información en Firestore.
 */
public class ReminderAlarmReceiver extends BroadcastReceiver {
    // Etiqueta para el registro de logs
    private static final String TAG = "ReminderAlarmReceiver";

    /**
     * Método que se llama cuando se recibe un Intent.
     *
     * @param context El contexto de la aplicación.
     * @param intent El Intent que contiene la acción y los datos necesarios.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: BroadcastReceiver activado.");

        // Verifica si el Intent no es nulo y si la acción es la correcta
        if (intent != null && NotificationHelper.ACTION_SHOW_REMINDER.equals(intent.getAction())) {
            // Extrae los datos del Intent
            String title = intent.getStringExtra(NotificationHelper.EXTRA_REMINDER_TITLE);
            String message = intent.getStringExtra(NotificationHelper.EXTRA_REMINDER_MESSAGE);
            int notificationId = intent.getIntExtra(NotificationHelper.EXTRA_NOTIFICATION_ID, 0);
            String reminderId = intent.getStringExtra(NotificationHelper.EXTRA_REMINDER_ID);
            String medicationId = intent.getStringExtra(NotificationHelper.EXTRA_MEDICATION_ID);
            String treatmentId = intent.getStringExtra(NotificationHelper.EXTRA_TREATMENT_ID);
            long triggerTimeMillis = intent.getLongExtra(NotificationHelper.EXTRA_TRIGGER_TIME_MILLIS, 0);

            // Verifica que el título y el mensaje no sean nulos
            if (title != null && message != null) {
                Log.d(TAG, "onReceive: Recibida alarma para notificación. Title: '" + title + "', Message: '" + message + "', ID: " + notificationId);

                // Muestra la notificación utilizando el helper
                NotificationHelper.showNotification(context, title, message, notificationId,
                        reminderId, medicationId, treatmentId);

                // Obtiene el ID del usuario actual
                String userId = FirebaseAuth.getInstance().getCurrentUser () != null ? FirebaseAuth.getInstance().getCurrentUser ().getUid() : "anonymous";
                if (!userId.equals("anonymous")) {
                    // Crea una instancia de Firestore
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    Notification notification = new Notification();
                    // Asigna valores al objeto Notification
                    notification.setId(UUID.randomUUID().toString());
                    notification.setTitle(title);
                    notification.setMessage(message);
                    notification.setRelatedReminderId(reminderId);
                    notification.setRelatedMedicationId(medicationId);
                    notification.setRelatedTreatmentId(treatmentId);
                    notification.setNotificationTriggerTimeMillis(triggerTimeMillis);
                    notification.setUserId(userId);

                    // Guarda la notificación en la colección "notifications" de Firestore
                    db.collection("notifications")
                            .add(notification)
                            .addOnSuccessListener(documentReference -> {
                                Log.d(TAG, "Notification guardada con ID: " + documentReference.getId());
                                // Opcional: guarda el ID de Firestore en el objeto local
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error al guardar el log de notificación: " + e.getMessage(), e);
                            });
                } else {
                    Log.w(TAG, "Usuario no logueado. No se guarda el log de notificación.");
                }

            } else {
                Log.w(TAG, "onReceive: Datos de notificación incompletos (title o message es nulo).");
            }
        }
    }
}
