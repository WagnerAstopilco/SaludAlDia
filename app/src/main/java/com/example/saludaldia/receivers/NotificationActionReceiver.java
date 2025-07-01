package com.example.saludaldia.receivers;

// Importaciones necesarias para el funcionamiento del BroadcastReceiver
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import androidx.core.app.NotificationManagerCompat;

import com.example.saludaldia.utils.NotificationHelper;

/**
 * NotificationActionReceiver es un BroadcastReceiver que maneja las acciones de las notificaciones.
 * Este receptor se activa cuando el usuario interactúa con una notificación, permitiendo
 * realizar acciones como marcar un medicamento como completado o posponer la notificación.
 */
public class NotificationActionReceiver extends BroadcastReceiver {

    // Etiqueta para el registro de logs
    private static final String TAG = "NotificationActionReceiver";

    // Clave para extraer el ID de la notificación del Intent
    public static final String EXTRA_NOTIFICATION_ID = "extra_notification_id";

    /**
     * Metodo que se llama cuando se recibe un Intent.
     *
     * @param context El contexto de la aplicación.
     * @param intent El Intent que contiene la acción y los datos necesarios.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        // Verifica si el Intent o la acción son nulos
        if (intent == null || intent.getAction() == null) {
            Log.w(TAG, "onReceive: Intent nulo o acción nula.");
            return;
        }

        // Extrae el ID de la notificación del Intent
        int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1);
        if (notificationId == -1) {
            Log.e(TAG, "onReceive: No se encontró el ID de la notificación en el Intent.");
            return;
        }

        // Obtiene la acción del Intent
        String action = intent.getAction();
        Log.d(TAG, "onReceive: Acción recibida: " + action + ", ID de Notificación: " + notificationId);

        // Crea una instancia de NotificationManagerCompat para manejar las notificaciones
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        // Cancela la notificación utilizando el ID extraído
        notificationManager.cancel(notificationId);

        // Maneja las diferentes acciones de la notificación
        switch (action) {
            case NotificationHelper.ACTION_COMPLETE:
                // Muestra un Toast indicando que el medicamento ha sido marcado como completado
                Toast.makeText(context, "Medicamento marcado como completado.", Toast.LENGTH_SHORT).show();
                break;
            case NotificationHelper.ACTION_SNOOZE:
                // Muestra un Toast indicando que el medicamento ha sido pospuesto
                Toast.makeText(context, "Medicamento pospuesto por 15 minutos.", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
