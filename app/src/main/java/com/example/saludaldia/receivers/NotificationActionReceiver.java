//package com.example.saludaldia.receivers;
//
//// Importaciones necesarias para el funcionamiento del BroadcastReceiver
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.util.Log;
//import android.widget.Toast;
//import androidx.core.app.NotificationManagerCompat;
//
//import com.example.saludaldia.utils.NotificationHelper;
//
///**
// * NotificationActionReceiver es un BroadcastReceiver que maneja las acciones de las notificaciones.
// * Este receptor se activa cuando el usuario interactúa con una notificación, permitiendo
// * realizar acciones como marcar un medicamento como completado o posponer la notificación.
// */
//public class NotificationActionReceiver extends BroadcastReceiver {
//
//    // Etiqueta para el registro de logs
//    private static final String TAG = "NotificationActionReceiver";
//
//    // Clave para extraer el ID de la notificación del Intent
//    public static final String EXTRA_NOTIFICATION_ID = "extra_notification_id";
//
//    /**
//     * Metodo que se llama cuando se recibe un Intent.
//     *
//     * @param context El contexto de la aplicación.
//     * @param intent El Intent que contiene la acción y los datos necesarios.
//     */
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        // Verifica si el Intent o la acción son nulos
//        if (intent == null || intent.getAction() == null) {
//            Log.w(TAG, "onReceive: Intent nulo o acción nula.");
//            return;
//        }
//
//        // Extrae el ID de la notificación del Intent
//        int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1);
//        if (notificationId == -1) {
//            Log.e(TAG, "onReceive: No se encontró el ID de la notificación en el Intent.");
//            return;
//        }
//
//        // Obtiene la acción del Intent
//        String action = intent.getAction();
//        Log.d(TAG, "onReceive: Acción recibida: " + action + ", ID de Notificación: " + notificationId);
//
//        // Crea una instancia de NotificationManagerCompat para manejar las notificaciones
//        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
//        // Cancela la notificación utilizando el ID extraído
//        notificationManager.cancel(notificationId);
//
//        // Maneja las diferentes acciones de la notificación
//        switch (action) {
//            case NotificationHelper.ACTION_COMPLETE:
//                // Muestra un Toast indicando que el medicamento ha sido marcado como completado
//                Toast.makeText(context, "Medicamento marcado como completado.", Toast.LENGTH_SHORT).show();
//                break;
//            case NotificationHelper.ACTION_SNOOZE:
//                // Muestra un Toast indicando que el medicamento ha sido pospuesto
//                Toast.makeText(context, "Medicamento pospuesto por 15 minutos.", Toast.LENGTH_SHORT).show();
//                break;
//        }
//    }
//}

//
//package com.example.saludaldia.receivers;
//
//import android.app.NotificationManager;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.util.Log;
//import android.widget.Toast;
//
//import com.example.saludaldia.utils.NotificationHelper; // Importa tu NotificationHelper
//import com.google.firebase.firestore.DocumentReference;
//import com.google.firebase.firestore.FirebaseFirestore;
//
//import java.util.HashMap;
//import java.util.Map;
//
//public class NotificationActionReceiver extends BroadcastReceiver {
//
//    private static final String TAG = "NotifActionReceiver";
//    public static final String EXTRA_NOTIFICATION_ID = "extra_notification_id";
//    public static final String EXTRA_REMINDER_ID = "extra_reminder_id";
//    public static final String EXTRA_MEDICATION_ID = "extra_medication_id";
//    public static final String EXTRA_TREATMENT_ID = "extra_treatment_id";
//
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        if (intent == null || intent.getAction() == null) {
//            Log.w(TAG, "Intent o acción nulos en NotificationActionReceiver.");
//            return;
//        }
//
//        int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1);
//        String reminderId = intent.getStringExtra(EXTRA_REMINDER_ID);
//        String medicationId = intent.getStringExtra(EXTRA_MEDICATION_ID);
//        String treatmentId = intent.getStringExtra(EXTRA_TREATMENT_ID);
//        String action = intent.getAction();
//
//        Log.d(TAG, "Acción de notificación recibida: " + action +
//                ", Notif ID: " + notificationId +
//                ", Reminder ID: " + reminderId +
//                ", Medication ID: " + medicationId +
//                ", Treatment ID: " + treatmentId);
//
//        // Cierra la notificación de la barra de estado
//        if (notificationId != -1) {
//            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//            if (notificationManager != null) {
//                notificationManager.cancel(notificationId);
//            }
//        }
//
//        // Verifica que tenemos al menos un ID para el recordatorio
//        if (reminderId == null || reminderId.isEmpty()) {
//            Log.e(TAG, "No se proporcionó un Reminder ID para la acción de notificación.");
//            Toast.makeText(context, "Error: No se pudo identificar el recordatorio.", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        DocumentReference reminderRef;
//
//        reminderRef = db.collection("reminders").document(reminderId);
//
//
//        Map<String, Object> updates = new HashMap<>();
//
//        if (NotificationHelper.ACTION_COMPLETE.equals(action)) {
//            updates.put("completed", true);
//            updates.put("dismissed", false);
//            updates.put("completedAt", System.currentTimeMillis());
//            Log.d(TAG, "Acción: Completar recordatorio " + reminderId);
//            Toast.makeText(context, "Recordatorio completado.", Toast.LENGTH_SHORT).show();
//        } else if (NotificationHelper.ACTION_DISMISS.equals(action)) {
//            updates.put("dismissed", true);
//            updates.put("completed", false);
//            updates.put("dismissedAt", System.currentTimeMillis());
//            Log.d(TAG, "Acción: Ignorar recordatorio " + reminderId);
//            Toast.makeText(context, "Recordatorio ignorado.", Toast.LENGTH_SHORT).show();
//        } else {
//            Log.w(TAG, "Acción de notificación desconocida: " + action);
//            return;
//        }
//
//        // Actualizar el documento en Firestore
//        reminderRef.update(updates)
//                .addOnSuccessListener(aVoid -> Log.d(TAG, "Recordatorio " + reminderId + " actualizado en Firestore."))
//                .addOnFailureListener(e -> {
//                    Log.e(TAG, "Error al actualizar recordatorio " + reminderId + " en Firestore: " + e.getMessage(), e);
//                    Toast.makeText(context, "Error al actualizar el recordatorio en la nube.", Toast.LENGTH_LONG).show();
//                });
//    }
//}

//package com.example.saludaldia.receivers;
//
//import android.app.NotificationManager;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.util.Log;
//import android.widget.Toast;
//
//import com.example.saludaldia.utils.NotificationHelper;
//import com.google.firebase.firestore.DocumentReference;
//import com.google.firebase.firestore.FirebaseFirestore;
//
//import java.util.HashMap;
//import java.util.Map;
//
//public class NotificationActionReceiver extends BroadcastReceiver {
//
//    private static final String TAG = "NotifActionReceiverNotifActionReceiver";
//    public static final String EXTRA_NOTIFICATION_ID = "extra_notification_id";
//    public static final String EXTRA_REMINDER_ID = "extra_reminder_id"; // Este ID ahora es el ID del documento en Firestore
//    public static final String EXTRA_MEDICATION_ID = "extra_medication_id";
//    public static final String EXTRA_TREATMENT_ID = "extra_treatment_id";
//
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        if (intent == null || intent.getAction() == null) {
//            Log.w(TAG, "Intent o acción nulos en NotificationActionReceiver.");
//            return;
//        }
//
//        int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1);
//        String documentIdToUpdate = intent.getStringExtra(EXTRA_REMINDER_ID); // Usamos EXTRA_REMINDER_ID como el ID del documento
//        String medicationId = intent.getStringExtra(EXTRA_MEDICATION_ID); // Podría no ser necesario si el ID del documento es suficiente
//        String treatmentId = intent.getStringExtra(EXTRA_TREATMENT_ID); // Podría no ser necesario si el ID del documento es suficiente
//        String action = intent.getAction();
//
//        Log.d(TAG, "Acción de notificación recibida: " + action +
//                ", Notif ID: " + notificationId +
//                ", Document ID (from EXTRA_REMINDER_ID): " + documentIdToUpdate +
//                ", Medication ID: " + medicationId +
//                ", Treatment ID: " + treatmentId);
//
//        // Cierra la notificación de la barra de estado
//        if (notificationId != -1) {
//            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//            if (notificationManager != null) {
//                notificationManager.cancel(notificationId);
//            }
//        }
//
//        // Verifica que tenemos el ID del documento
//        if (documentIdToUpdate == null || documentIdToUpdate.isEmpty()) {
//            Log.e(TAG, "No se proporcionó un ID de documento para la acción de notificación.");
//            Toast.makeText(context, "Error: No se pudo identificar el recordatorio para actualizar.", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        DocumentReference docRef;
//
//        // CAMBIO CRÍTICO AQUÍ:
//        // Si tus documentos de notificación se guardan en la colección "notifications"
//        // y el ID del documento es el valor del campo relatedReminderId,
//        // entonces la ruta debería ser:
//        docRef = db.collection("notifications").document(documentIdToUpdate);
//        // Si el documento se guarda en la colección "reminders" con ese ID,
//        // entonces la línea anterior era correcta:
//        // docRef = db.collection("reminders").document(documentIdToUpdate);
//        // ¡Confirma el nombre exacto de la colección en tu consola de Firebase!
//
//
//        Map<String, Object> updates = new HashMap<>();
//
//        if (NotificationHelper.ACTION_COMPLETE.equals(action)) {
//            updates.put("completed", true);
//            updates.put("dismissed", false);
//            updates.put("completedAt", System.currentTimeMillis());
//            Log.d(TAG, "Acción: Completar documento " + documentIdToUpdate);
//            Toast.makeText(context, "Recordatorio completado.", Toast.LENGTH_SHORT).show();
//        } else if (NotificationHelper.ACTION_DISMISS.equals(action)) {
//            updates.put("dismissed", true);
//            updates.put("completed", false);
//            updates.put("dismissedAt", System.currentTimeMillis());
//            Log.d(TAG, "Acción: Ignorar documento " + documentIdToUpdate);
//            Toast.makeText(context, "Recordatorio ignorado.", Toast.LENGTH_SHORT).show();
//        } else {
//            Log.w(TAG, "Acción de notificación desconocida: " + action);
//            return;
//        }
//
//        // Actualizar el documento en Firestore
//        docRef.update(updates)
//                .addOnSuccessListener(aVoid -> {
//                    Log.d(TAG, "Documento " + documentIdToUpdate + " actualizado en Firestore.");
//                    // *** INICIO DE CÓDIGO DE DEPURACIÓN (Mantén esto por un tiempo) ***
//                    docRef.get().addOnSuccessListener(documentSnapshot -> {
//                        if (documentSnapshot.exists()) {
//                            Boolean completedStatus = documentSnapshot.getBoolean("completed");
//                            Boolean dismissedStatus = documentSnapshot.getBoolean("dismissed");
//                            Log.d(TAG, "Estado de 'completed' después de actualización: " + completedStatus);
//                            Log.d(TAG, "Estado de 'dismissed' después de actualización: " + dismissedStatus);
//                        } else {
//                            Log.w(TAG, "Documento " + documentIdToUpdate + " no encontrado después de la actualización.");
//                        }
//                    }).addOnFailureListener(e -> {
//                        Log.e(TAG, "Error al verificar el documento después de la actualización: " + e.getMessage());
//                    });
//                    // *** FIN DE CÓDIGO DE DEPURACIÓN ***
//                })
//                .addOnFailureListener(e -> {
//                    Log.e(TAG, "Error al actualizar documento " + documentIdToUpdate + " en Firestore: " + e.getMessage(), e);
//                    Toast.makeText(context, "Error al actualizar el recordatorio en la nube.", Toast.LENGTH_LONG).show();
//                });
//    }
//}


package com.example.saludaldia.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.saludaldia.utils.NotificationHelper;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class NotificationActionReceiver extends BroadcastReceiver {

    private static final String TAG = "NotifActionReceiver";
    public static final String EXTRA_NOTIFICATION_ID = "extra_notification_id";
    public static final String EXTRA_REMINDER_ID = "extra_reminder_id"; // ID del recordatorio (no el del documento de notificación)
    public static final String EXTRA_MEDICATION_ID = "extra_medication_id";
    public static final String EXTRA_TREATMENT_ID = "extra_treatment_id";
    // NUEVO: Clave para el ID del documento de la notificación en Firestore
    public static final String EXTRA_NOTIFICATION_FIRESTORE_DOC_ID = "extra_notification_firestore_doc_id";


    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            Log.w(TAG, "Intent o acción nulos en NotificationActionReceiver.");
            return;
        }

        int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1);
        // OBTENER EL ID CORRECTO DEL DOCUMENTO DE NOTIFICACIÓN
        String notificationFirestoreDocId = intent.getStringExtra(EXTRA_NOTIFICATION_FIRESTORE_DOC_ID); // <--- ¡OBTENEMOS EL NUEVO EXTRA!

        // Los IDs relacionados (pueden ser útiles para otras lógicas, pero no para la ruta del documento de notificación)
        String relatedReminderId = intent.getStringExtra(EXTRA_REMINDER_ID);
        String medicationId = intent.getStringExtra(EXTRA_MEDICATION_ID);
        String treatmentId = intent.getStringExtra(EXTRA_TREATMENT_ID);
        String action = intent.getAction();

        Log.d(TAG, "Acción de notificación recibida: " + action +
                ", Notif ID (Android): " + notificationId +
                ", Notif Doc ID (Firestore): " + notificationFirestoreDocId + // <--- Log del nuevo ID
                ", Related Reminder ID: " + relatedReminderId);


        // Cierra la notificación de la barra de estado
        if (notificationId != -1) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.cancel(notificationId);
            }
        }

        // Verifica que tenemos el ID del documento de la notificación en Firestore
        if (notificationFirestoreDocId == null || notificationFirestoreDocId.isEmpty()) {
            Log.e(TAG, "No se proporcionó un ID de documento de notificación (Firestore) para la acción.");
            Toast.makeText(context, "Error: No se pudo identificar la notificación a actualizar.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference notificationDocRef;

        // ESTA ES LA RUTA CORRECTA AHORA:
        // Apunta a la colección "notifications" y usa el ID del documento de la notificación
        notificationDocRef = db.collection("notifications").document(notificationFirestoreDocId);


        Map<String, Object> updates = new HashMap<>();

        if (NotificationHelper.ACTION_COMPLETE.equals(action)) {
            updates.put("completed", true);
            updates.put("dismissed", false);
            updates.put("completedAt", System.currentTimeMillis());
            Log.d(TAG, "Acción: Completar notificación " + notificationFirestoreDocId);
            Toast.makeText(context, "Recordatorio completado.", Toast.LENGTH_SHORT).show();
        } else if (NotificationHelper.ACTION_DISMISS.equals(action)) {
            updates.put("dismissed", true);
            updates.put("completed", false);
            updates.put("dismissedAt", System.currentTimeMillis());
            Log.d(TAG, "Acción: Ignorar notificación " + notificationFirestoreDocId);
            Toast.makeText(context, "Recordatorio ignorado.", Toast.LENGTH_SHORT).show();
        } else {
            Log.w(TAG, "Acción de notificación desconocida: " + action);
            return;
        }

        // Actualizar el documento en Firestore
        notificationDocRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Documento de notificación " + notificationFirestoreDocId + " actualizado en Firestore.");
                    // *** INICIO DE CÓDIGO DE DEPURACIÓN (Mantén esto por un tiempo) ***
                    notificationDocRef.get().addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Boolean completedStatus = documentSnapshot.getBoolean("completed");
                            Boolean dismissedStatus = documentSnapshot.getBoolean("dismissed");
                            Log.d(TAG, "Estado de 'completed' después de actualización: " + completedStatus);
                            Log.d(TAG, "Estado de 'dismissed' después de actualización: " + dismissedStatus);
                        } else {
                            Log.w(TAG, "Documento " + notificationFirestoreDocId + " no encontrado después de la verificación.");
                        }
                    }).addOnFailureListener(e -> {
                        Log.e(TAG, "Error al verificar el documento después de la actualización: " + e.getMessage());
                    });
                    // *** FIN DE CÓDIGO DE DEPURACIÓN ***
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al actualizar documento de notificación " + notificationFirestoreDocId + " en Firestore: " + e.getMessage(), e);
                    Toast.makeText(context, "Error al actualizar la notificación en la nube.", Toast.LENGTH_LONG).show();
                });
    }
}
