package com.example.saludaldia.data.repository;

import com.example.saludaldia.data.model.UserSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;

public class UserSettingsRepository {

    private final FirebaseFirestore db;
    private final String userId;

    public UserSettingsRepository() {
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    // Guardar o actualizar configuración
    public void saveUserSettings(UserSettings settings, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        // Asegurarse de que el userId esté en el objeto antes de guardar
        settings.setUserId(userId);

        DocumentReference docRef = db.collection("userSettings").document(userId);
        docRef.set(settings)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    // Obtener configuración del usuario
    public void getUserSettings(OnSuccessListener<UserSettings> onSuccess, OnFailureListener onFailure) {
        DocumentReference docRef = db.collection("userSettings").document(userId);
        docRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserSettings settings = documentSnapshot.toObject(UserSettings.class);
                        onSuccess.onSuccess(settings);
                    } else {
                        onSuccess.onSuccess(null); // No hay configuración previa
                    }
                })
                .addOnFailureListener(onFailure);
    }

    public void updateUserSettingField(String field, Object value, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        db.collection("userSettings")
                .document(userId)
                .update(field, value)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

}
