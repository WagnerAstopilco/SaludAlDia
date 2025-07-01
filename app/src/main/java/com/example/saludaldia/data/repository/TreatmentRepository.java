package com.example.saludaldia.data.repository;

import android.util.Log;
import androidx.annotation.NonNull;
import com.example.saludaldia.data.model.Treatment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TreatmentRepository {
    private static final String TAG = "TreatmentRepository";
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference treatmentCollection;
    public TreatmentRepository() {
        treatmentCollection = db.collection("treatments");
    }
    public interface TreatmentCallback {
        void onSuccess(List<Treatment> treatments);

        void onFailure(Exception e);
    }
    public static void getTreatmentsForCurrentUser(OnSuccessListener<List<Treatment>> onSuccess, OnFailureListener onFailure) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            onFailure.onFailure(new Exception("Usuario no autenticado"));
            return;
        }

        db.collection("treatments")
                .whereEqualTo("userId", currentUser.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Treatment> treatmentList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Treatment treatment = document.toObject(Treatment.class);
                        treatment.setTreatmentId(document.getId());
                        treatmentList.add(treatment);
                    }
                    Collections.sort(treatmentList, (t1, t2) -> t1.getStartDate().compareTo(t2.getStartDate()));

                    onSuccess.onSuccess(treatmentList);
                })
                .addOnFailureListener(onFailure);
    }
    public static void getTreatmentsForLinkedUser(String linkedUserId, OnSuccessListener<List<Treatment>> onSuccess, OnFailureListener onFailure) {
        if (linkedUserId == null || linkedUserId.isEmpty()) {
            onFailure.onFailure(new IllegalArgumentException("El ID del usuario vinculado no puede ser nulo o vacío."));
            return;
        }
        db.collection("treatments")
                .whereEqualTo("userId", linkedUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Treatment> treatmentList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Treatment treatment = document.toObject(Treatment.class);
                        if (treatment != null) {
                            treatment.setTreatmentId(document.getId());
                            treatmentList.add(treatment);
                        }
                    }
                    Collections.sort(treatmentList, (t1, t2) -> {
                        if (t1.getStartDate() == null && t2.getStartDate() == null) return 0;
                        if (t1.getStartDate() == null) return -1; // nulls first
                        if (t2.getStartDate() == null) return 1;  // nulls last
                        return t1.getStartDate().compareTo(t2.getStartDate());
                    });

                    onSuccess.onSuccess(treatmentList);
                })
                .addOnFailureListener(onFailure);
    }

    public static void getActiveTreatmentsForCurrentUser(OnSuccessListener<List<Treatment>> onSuccess, OnFailureListener onFailure) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            onFailure.onFailure(new Exception("Usuario no autenticado"));
            return;
        }

        db.collection("treatments")
                .whereEqualTo("userId", currentUser.getUid())
                .whereEqualTo("state", "activo")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Treatment> treatmentList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Treatment treatment = document.toObject(Treatment.class);
                        treatment.setTreatmentId(document.getId());
                        treatmentList.add(treatment);
                    }
                    Collections.sort(treatmentList, (t1, t2) -> t1.getStartDate().compareTo(t2.getStartDate()));
                    onSuccess.onSuccess(treatmentList);
                })
                .addOnFailureListener(onFailure);
    }

    public void addTreatment(Treatment treatment, @NonNull Runnable onSuccess, @NonNull Runnable onFailure) {
        treatmentCollection.document(treatment.getTreatmentId())
                .set(treatment)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Tratamiento guardado con ID: " + treatment.getTreatmentId());
                    onSuccess.run(); // Llama al callback de éxito
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al guardar tratamiento: " + treatment.getTreatmentId(), e);
                    onFailure.run(); // Llama al callback de fallo
                });
    }

    public void updateTreatment(Treatment treatment, @NonNull Runnable onSuccess, @NonNull Runnable onFailure) {
        treatmentCollection.document(treatment.getTreatmentId())
                .set(treatment)
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al actualizar tratamiento", e);
                    onFailure.run();
                });
    }

    public void deleteTreatment(String treatmentId, @NonNull Runnable onSuccess, @NonNull Runnable onFailure) {
        treatmentCollection.document(treatmentId)
                .delete()
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al eliminar tratamiento", e);
                    onFailure.run();
                });
    }

}
