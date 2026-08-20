package com.example.techfix.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.techfix.R;
import com.example.techfix.models.Appointment;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {

    public interface OnAppointmentActionListener {
        void onActionClick(Appointment appointment);
    }

    private List<Appointment> appointments;
    private boolean isAdmin;
    private OnAppointmentActionListener actionListener;

    public AppointmentAdapter(List<Appointment> appointments, boolean isAdmin, OnAppointmentActionListener actionListener) {
        this.appointments = appointments;
        this.isAdmin = isAdmin;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment app = appointments.get(position);

        holder.tvDeviceCategory.setText(app.getDeviceCategory() + " Repair");
        holder.tvAppId.setText("Appointment ID: #" + app.getId());
        holder.tvAssignedBranch.setText("Assigned Branch: " + app.getBranchName());

        // Status badge
        String status = app.getStatus();
        holder.tvStatusBadge.setText(status.toUpperCase());

        if ("Pending".equalsIgnoreCase(status)) {
            holder.tvStatusBadge.setBackgroundResource(R.drawable.status_badge_pending);
        } else if ("In Progress".equalsIgnoreCase(status)) {
            holder.tvStatusBadge.setBackgroundResource(R.drawable.status_badge_inprogress);
        } else {
            holder.tvStatusBadge.setBackgroundResource(R.drawable.status_badge_completed);
        }

        // Show Customer name if viewing as admin
        if (isAdmin && app.getUserName() != null) {
            holder.tvCustomerName.setVisibility(View.VISIBLE);
            holder.tvCustomerName.setText("Customer: " + app.getUserName());
        } else {
            holder.tvCustomerName.setVisibility(View.GONE);
        }

        // Action button visibility and labeling
        if (isAdmin) {
            holder.layoutActionButtons.setVisibility(View.VISIBLE);
            if ("Pending".equalsIgnoreCase(status)) {
                holder.btnActionButton.setVisibility(View.VISIBLE);
                holder.btnActionButton.setText("Start Repair");
                holder.btnActionButton.setBackgroundColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_blue_dark));
            } else if ("In Progress".equalsIgnoreCase(status)) {
                holder.btnActionButton.setVisibility(View.VISIBLE);
                holder.btnActionButton.setText("Complete Repair");
                holder.btnActionButton.setBackgroundColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
            } else {
                // Completed status, nothing to do
                holder.layoutActionButtons.setVisibility(View.GONE);
            }
        } else {
            // Customer role actions
            if ("Completed".equalsIgnoreCase(status)) {
                holder.layoutActionButtons.setVisibility(View.VISIBLE);
                holder.btnActionButton.setVisibility(View.VISIBLE);
                holder.btnActionButton.setText("Pay Now (Simulated)");
                holder.btnActionButton.setBackgroundColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_orange_dark));
            } else if ("Paid".equalsIgnoreCase(status)) {
                holder.layoutActionButtons.setVisibility(View.VISIBLE);
                holder.btnActionButton.setVisibility(View.VISIBLE);
                holder.btnActionButton.setText("Receipt / Paid");
                holder.btnActionButton.setEnabled(false);
                holder.btnActionButton.setBackgroundColor(holder.itemView.getContext().getResources().getColor(android.R.color.darker_gray));
            } else {
                holder.layoutActionButtons.setVisibility(View.GONE);
            }
        }

        holder.btnActionButton.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onActionClick(app);
            }
        });
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    public void updateList(List<Appointment> newList) {
        this.appointments = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDeviceCategory, tvAppId, tvCustomerName, tvAssignedBranch, tvStatusBadge;
        LinearLayout layoutActionButtons;
        MaterialButton btnActionButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDeviceCategory = itemView.findViewById(R.id.tvDeviceCategory);
            tvAppId = itemView.findViewById(R.id.tvAppId);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvAssignedBranch = itemView.findViewById(R.id.tvAssignedBranch);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            layoutActionButtons = itemView.findViewById(R.id.layoutActionButtons);
            btnActionButton = itemView.findViewById(R.id.btnActionButton);
        }
    }
}
