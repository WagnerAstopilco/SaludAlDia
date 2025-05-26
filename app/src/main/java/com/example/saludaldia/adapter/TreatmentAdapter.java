package com.example.saludaldia.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saludaldia.R;
import com.example.saludaldia.ui.treatment.TreatmentDetailsActivity;
import com.example.saludaldia.data.model.Medication;
import com.example.saludaldia.data.model.Treatment;
import com.example.saludaldia.data.repository.TreatmentRepository;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class TreatmentAdapter extends RecyclerView.Adapter<TreatmentAdapter.TreatmentViewHolder> {

    private final List<Treatment> treatmentList;
    private final Map<String, List<Medication>> medicationsMap;
    private final TreatmentRepository repository = new TreatmentRepository();
    private final boolean removeOnInactive;

    public TreatmentAdapter(List<Treatment> treatmentList, Map<String, List<Medication>> medicationsMap, boolean removeOnInactive) {
        this.treatmentList = treatmentList;
        this.medicationsMap = medicationsMap;
        this.removeOnInactive = removeOnInactive;
    }

    @NonNull
    @Override
    public TreatmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_treatment, parent, false);
        return new TreatmentViewHolder(view);
    }

    private CompoundButton.OnCheckedChangeListener createSwitchListener(Treatment treatment, TreatmentViewHolder holder) {
        return (buttonView, isChecked) -> {
            String newState = isChecked ? "activo" : "inactivo";
            treatment.setState(newState);
            repository.updateTreatment(treatment,
                    () -> {
                        int pos = holder.getAdapterPosition();
                        if (removeOnInactive && newState.equals("inactivo")) {
                            if (pos != RecyclerView.NO_POSITION) {
                                treatmentList.remove(pos);
                                notifyItemRemoved(pos);
                            }
                        } else {
                            if (pos != RecyclerView.NO_POSITION) {
                                notifyItemChanged(pos);
                            }
                        }
                        Log.d("Adapter", "Estado actualizado a: " + newState);
                    },
                    () -> {
                        holder.switchTreatmentState.setOnCheckedChangeListener(null);
                        holder.switchTreatmentState.setChecked(!isChecked);
                        holder.switchTreatmentState.setOnCheckedChangeListener(createSwitchListener(treatment, holder));
                        Log.e("Adapter", "Error al actualizar estado");
                    }
            );
        };
    }

    @Override
    public void onBindViewHolder(@NonNull TreatmentViewHolder holder, int position) {
        Treatment treatment = treatmentList.get(position);

        Date startDate = treatment.getStartDate();
        Date endDate = treatment.getEndDate();
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy");

        String formattedDates;
        if (startDate != null && endDate != null) {
            formattedDates = outputFormat.format(startDate) + " - " + outputFormat.format(endDate);
        } else {
            formattedDates = "Fechas no disponibles";
        }

        holder.tvTreatmentName.setText(treatment.getName());
        holder.tvTreatmentDates.setText(formattedDates);
        holder.tvTreatmentDescription.setText(treatment.getDescription());


        // Obtener medicamentos desde el mapa
        List<Medication> medications = medicationsMap.get(treatment.getTreatmentId());
        if (medications != null && !medications.isEmpty()) {

            // Ordenar alfabéticamente por nombre
            Collections.sort(medications, new Comparator<Medication>() {
                @Override
                public int compare(Medication m1, Medication m2) {
                    return m1.getName().compareToIgnoreCase(m2.getName());
                }
            });

            StringBuilder medsBuilder = new StringBuilder();
            for (Medication med : medications) {
                medsBuilder.append(med.getName()).append(" - ").append(med.getDose()).append("\n");
            }
            holder.tvMedicationsList.setText(medsBuilder.toString().trim());
        } else {
            holder.tvMedicationsList.setText("Sin medicamentos");
        }


        // Switch
        holder.switchTreatmentState.setOnCheckedChangeListener(null);
        boolean isActive = treatment.getState().equalsIgnoreCase("activo");
        holder.switchTreatmentState.setChecked(isActive);
        holder.switchTreatmentState.setText(isActive ? "Activo" : "Inactivo");
        holder.switchTreatmentState.setOnCheckedChangeListener(createSwitchListener(treatment, holder));

        // Ir a detalles
        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, TreatmentDetailsActivity.class);
            intent.putExtra("treatmentId", treatment.getTreatmentId());
            context.startActivity(intent);
        });

        holder.btnDeleteTreatment.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Eliminar tratamiento")
                    .setMessage("¿Estás seguro de que deseas eliminar este tratamiento?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        repository.deleteTreatment(treatment.getTreatmentId(),
                                () -> {
                                    int pos = holder.getAdapterPosition();
                                    if (pos != RecyclerView.NO_POSITION) {
                                        treatmentList.remove(pos);
                                        notifyItemRemoved(pos);
                                    }
                                },
                                () -> Log.e("Adapter", "Error al eliminar tratamiento")
                        );
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return treatmentList.size();
    }

    static class TreatmentViewHolder extends RecyclerView.ViewHolder {
        TextView tvTreatmentName, tvTreatmentDates, tvTreatmentDescription, tvMedicationsList;
        Switch switchTreatmentState;
        Button btnDeleteTreatment;

        public TreatmentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTreatmentName = itemView.findViewById(R.id.tvTreatmentName);
            tvTreatmentDates = itemView.findViewById(R.id.tvTreatmentDates);
            tvTreatmentDescription = itemView.findViewById(R.id.tvTreatmentDescription);
            tvMedicationsList = itemView.findViewById(R.id.tvMedicationsList);
            switchTreatmentState = itemView.findViewById(R.id.switchTreatmentState);
            btnDeleteTreatment = itemView.findViewById(R.id.btnDeleteTreatment);
        }
    }

    public void updateList(List<Treatment> newList, Map<String, List<Medication>> newMedicationsMap) {
        treatmentList.clear();
        treatmentList.addAll(newList);
        medicationsMap.clear();
        medicationsMap.putAll(newMedicationsMap);
        notifyDataSetChanged();
    }
}
