package com.example.saludaldia.data.repository;

import com.example.saludaldia.data.model.Medication;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MedicationRepository {

    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String COLLECTION_MEDICATIONS = "medications";

    // Crear un nuevo medicamento como documento independiente
    public static void addMedication(String treatmentId, Medication medication,
                                     OnSuccessListener<Void> onSuccess,
                                     OnFailureListener onFailure) {
        medication.setTreatmentId(treatmentId); // Asegurar que el ID del tratamiento se guarda
        db.collection(COLLECTION_MEDICATIONS)
                .document(medication.getMedicationId())
                .set(medication)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    // Eliminar un medicamento (por su ID)
    public static void deleteMedication(String medicationId,
                                        OnSuccessListener<Void> onSuccess,
                                        OnFailureListener onFailure) {
        db.collection(COLLECTION_MEDICATIONS)
                .document(medicationId)
                .delete()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    // Actualizar un medicamento
    public static void updateMedication(Medication medication,
                                        OnSuccessListener<Void> onSuccess,
                                        OnFailureListener onFailure) {
        db.collection(COLLECTION_MEDICATIONS)
                .document(medication.getMedicationId())
                .set(medication)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }
    public static void getMedicationsByTreatmentId(String treatmentId,
                                                   OnSuccessListener<List<Medication>> onSuccess,
                                                   OnFailureListener onFailure) {
        FirebaseFirestore.getInstance()
                .collection("medications")
                .whereEqualTo("treatmentId", treatmentId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Medication> meds = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Medication med = doc.toObject(Medication.class);
                        if (med != null) {
                            meds.add(med);
                        }
                    }
                    onSuccess.onSuccess(meds);
                })
                .addOnFailureListener(onFailure);
    }

}
