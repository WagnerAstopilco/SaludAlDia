package com.example.saludaldia.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.saludaldia.R;
import com.example.saludaldia.data.model.HistoryEvent;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class HistoryEventsAdapter extends RecyclerView.Adapter<HistoryEventsAdapter.HistoryEventViewHolder> {

    private List<HistoryEvent> eventList;
    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public HistoryEventsAdapter(List<HistoryEvent> eventList) {
        this.eventList = eventList;
    }

    public void setEventList(List<HistoryEvent> newEventList) {
        this.eventList = newEventList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history_event, parent, false);
        return new HistoryEventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryEventViewHolder holder, int position) {
        HistoryEvent event = eventList.get(position);

        holder.tvEventType.setText("Tipo: " + formatEventType(event.getEventType()));
        holder.tvDetails.setText("Detalles: " + event.getDetails());

        if (event.getTimestamp() != null) {
            holder.tvTimestamp.setText("Fecha: " + dateTimeFormat.format(event.getTimestamp()));
        } else {
            holder.tvTimestamp.setText("Fecha: N/A");
        }
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    private String formatEventType(String eventType) {
        if (eventType == null) return "Desconocido";
        switch (eventType) {
            case "treatment_added": return "Tratamiento Añadido";
            case "med_taken": return "Medicamento Tomado";
            case "symptom_reported": return "Síntoma Reportado";
            default: return eventType.replace("_", " ");
        }
    }

    static class HistoryEventViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventType, tvDetails, tvTimestamp, tvSymptom, tvRelatedReminders;

        public HistoryEventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventType = itemView.findViewById(R.id.tv_history_event_type);
            tvDetails = itemView.findViewById(R.id.tv_history_event_details);
            tvTimestamp = itemView.findViewById(R.id.tv_history_event_timestamp);
            tvSymptom = itemView.findViewById(R.id.tv_history_event_symptom);
            tvRelatedReminders = itemView.findViewById(R.id.tv_history_event_related_reminders);
        }
    }
}