package com.example.saludaldia.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.saludaldia.R;
import com.example.saludaldia.data.model.User;
import com.example.saludaldia.ui.caregiver.LinkedUserNotificationsActivity;
import com.example.saludaldia.ui.caregiver.LinkedUserTreatmentsActivity;
import java.util.List;

public class LinkedUsersAdapter extends RecyclerView.Adapter<LinkedUsersAdapter.LinkedUserViewHolder> {

    private List<User> linkedUsersList;

    public LinkedUsersAdapter(List<User> linkedUsersList) {
        this.linkedUsersList = linkedUsersList;
    }

    public void setLinkedUsersList(List<User> newLinkedUsersList) {
        this.linkedUsersList = newLinkedUsersList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LinkedUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_linked_user, parent, false);
        return new LinkedUserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LinkedUserViewHolder holder, int position) {
        User user = linkedUsersList.get(position);
        holder.tvLinkedUserName.setText(user.getNames() != null ? user.getNames() : "N/A");
        holder.tvLinkedUserEmail.setText(user.getEmail() != null ? user.getEmail() : "N/A");
        holder.btnViewTreatments.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), LinkedUserTreatmentsActivity.class);
            intent.putExtra("linkedUserId", user.getUserId());
            intent.putExtra("linkedUserName", user.getNames());
            v.getContext().startActivity(intent);
        });
        holder.btnViewNotifications.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), LinkedUserNotificationsActivity.class);
            intent.putExtra("linkedUserId", user.getUserId());
            intent.putExtra("linkedUserName", user.getNames());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return linkedUsersList.size();
    }

    static class LinkedUserViewHolder extends RecyclerView.ViewHolder {
        TextView tvLinkedUserName, tvLinkedUserEmail;
        Button btnViewTreatments, btnViewNotifications;

        public LinkedUserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLinkedUserName = itemView.findViewById(R.id.tvLinkedUserName);
            tvLinkedUserEmail = itemView.findViewById(R.id.tvLinkedUserEmail);
            btnViewTreatments = itemView.findViewById(R.id.btnViewTreatments);
            btnViewNotifications = itemView.findViewById(R.id.btnViewNotifications);
        }
    }
}