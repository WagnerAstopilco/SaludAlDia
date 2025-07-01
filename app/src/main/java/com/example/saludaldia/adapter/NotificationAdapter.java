package com.example.saludaldia.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.saludaldia.R;
import com.example.saludaldia.data.model.Notification;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationLogViewHolder> {

    private List<Notification> notificationList;
    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public NotificationAdapter(List<Notification> notificationList) {
        this.notificationList = notificationList;
    }

    public void setNotificationList(List<Notification> notificationList) {
        this.notificationList = notificationList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotificationLogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationLogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationLogViewHolder holder, int position) {
        Notification notification = notificationList.get(position);
        holder.tvTitle.setText(notification.getTitle());
        holder.tvMessage.setText(notification.getMessage());
        if (notification.getTimestamp() != null) {
            holder.tvTimestamp.setText("Recibido: " + dateTimeFormat.format(notification.getTimestamp()));
        } else {
            holder.tvTimestamp.setText("Hora programada: " + dateTimeFormat.format(new Date(notification.getNotificationTriggerTimeMillis())));
        }
        if (notification.isCompleted()) {
            holder.tvStatus.setVisibility(View.VISIBLE);
            holder.tvStatus.setText("Estado: Completado");
            holder.tvStatus.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
        } else if (notification.isDismissed()) {
            holder.tvStatus.setVisibility(View.VISIBLE);
            holder.tvStatus.setText("Estado: Descartado");
            holder.tvStatus.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
        } else {
            holder.tvStatus.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    static class NotificationLogViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMessage, tvTimestamp, tvStatus;

        public NotificationLogViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_notification_item_title);
            tvMessage = itemView.findViewById(R.id.tv_notification_item_message);
            tvTimestamp = itemView.findViewById(R.id.tv_notification_item_timestamp);
            tvStatus = itemView.findViewById(R.id.tv_notification_item_status);
        }
    }
}