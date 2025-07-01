package com.example.saludaldia.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.example.saludaldia.R;
import com.example.saludaldia.data.model.Medication;
import com.example.saludaldia.ui.ReminderActivity;
import com.example.saludaldia.ui.medication.MedicationDetailsActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.MedicationViewHolder> {

    private final List<Medication> medicationList;

    public MedicationAdapter(List<Medication> medicationList) {
        this.medicationList = medicationList;
    }

    @NonNull
    @Override
    public MedicationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medication, parent, false);
        return new MedicationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicationViewHolder holder, int position) {
        Medication medication = medicationList.get(position);
        holder.txtName.setText(medication.getName());
        holder.txtState.setText(medication.getIsActive()?"Activo":"Inactivo");
        holder.txtNumberDays.setText("N° días: "+medication.getNumber_days());
        holder.txtDose.setText("Dosis: "+medication.getDose());

        holder.btnReminder.setEnabled(true);
        holder.btnReminder.setAlpha(1.0f);
        holder.btnReminder.setOnClickListener(null);

        FirebaseFirestore.getInstance()
                .collection("reminders")
                .whereEqualTo("medicationId", medication.getMedicationId())
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Intent intent = new Intent(holder.btnReminder.getContext(), ReminderActivity.class);
                    intent.putExtra("medicationId", medication.getMedicationId());
                    intent.putExtra("treatmentId", medication.getTreatmentId());

                    if (!querySnapshot.isEmpty()) {
                        String reminderId = querySnapshot.getDocuments().get(0).getId();
                        intent.putExtra("reminderId", reminderId);
                    }
                    holder.btnReminder.setOnClickListener(v -> {
                        v.getContext().startActivity(intent);
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(holder.btnReminder.getContext(), "No se pudo verificar el recordatorio", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(holder.btnReminder.getContext(), ReminderActivity.class);
                    intent.putExtra("medicationId", medication.getMedicationId());
                    intent.putExtra("treatmentId", medication.getTreatmentId());
                    holder.btnReminder.setOnClickListener(v -> v.getContext().startActivity(intent));
                });

        holder.btnViewDetails.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), MedicationDetailsActivity.class);
            intent.putExtra("medicationId", medication.getMedicationId());
            v.getContext().startActivity(intent);
        });

        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Eliminar medicamento")
                    .setMessage("¿Estás seguro de que deseas eliminar este medicamento?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        FirebaseFirestore.getInstance()
                                .collection("medications")
                                .document(medication.getMedicationId())
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    int currentPosition = holder.getAdapterPosition();
                                    if (currentPosition != RecyclerView.NO_POSITION) {
                                        medicationList.remove(currentPosition);
                                        notifyItemRemoved(currentPosition);
                                        notifyItemRangeChanged(currentPosition, medicationList.size());
                                        Toast.makeText(v.getContext(), "Medicamento eliminado", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(v.getContext(), "Error al eliminar", Toast.LENGTH_SHORT).show()
                                );
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
    }



    @Override
    public int getItemCount() {
        return medicationList.size();
    }

    static class MedicationViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtDose,txtNumberDays,txtState;
        ImageButton btnViewDetails, btnReminder, btnDelete;

        public MedicationViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtMedicationName);
            txtNumberDays=itemView.findViewById(R.id.txtMedicationNumberOfDays);
            txtState=itemView.findViewById(R.id.txtMedicationState);
            txtDose = itemView.findViewById(R.id.txtMedicationDose);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            btnReminder = itemView.findViewById(R.id.btnSetReminder);
            btnDelete = itemView.findViewById(R.id.btnDeleteMedication);
        }
    }
}
