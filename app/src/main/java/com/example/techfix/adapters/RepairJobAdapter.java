package com.example.techfix.adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.techfix.R;
import com.example.techfix.models.Appointment;

import java.io.File;
import java.util.Locale;

public class RepairJobAdapter extends ListAdapter<Appointment, RepairJobAdapter.Holder> {
    public interface Click { void open(Appointment appointment); }

    private final Click click;

    public RepairJobAdapter(Click click) {
        super(new DiffUtil.ItemCallback<Appointment>() {
            @Override public boolean areItemsTheSame(@NonNull Appointment oldItem, @NonNull Appointment newItem) {
                return oldItem.getId() == newItem.getId();
            }

            @Override public boolean areContentsTheSame(@NonNull Appointment oldItem, @NonNull Appointment newItem) {
                return oldItem.getId() == newItem.getId()
                        && equals(oldItem.getStatus(), newItem.getStatus())
                        && equals(oldItem.getPhotoPath(), newItem.getPhotoPath())
                        && oldItem.getPrice() == newItem.getPrice()
                        && equals(oldItem.getTechnicianName(), newItem.getTechnicianName());
            }

            private boolean equals(Object first, Object second) {
                return first == null ? second == null : first.equals(second);
            }
        });
        this.click = click;
    }

    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_repair_job, parent, false));
    }

    @Override public void onBindViewHolder(@NonNull Holder holder, int position) {
        Appointment appointment = getItem(position);
        String device = appointment.getDeviceModel();
        holder.device.setText("Repair #" + appointment.getId() + "  |  " +
                (device == null || device.isEmpty() ? appointment.getDeviceCategory() + " repair" : device));
        holder.status.setText(appointment.getStatus().toUpperCase(Locale.US));
        holder.issue.setText(appointment.getIssueDescription() == null ? "No issue description" : appointment.getIssueDescription());
        holder.meta.setText(String.format(Locale.US, "Estimated cost: LKR %.2f\nBranch: %s\nTechnician: %s",
                appointment.getPrice(), appointment.getBranchName(),
                appointment.getTechnicianName() == null ? "Not yet assigned" : appointment.getTechnicianName()));
        String photoPath = appointment.getPhotoPath();
        if (photoPath != null && !photoPath.isEmpty() && new File(photoPath).exists()) {
            holder.photo.setVisibility(View.VISIBLE);
            holder.photo.setImageURI(Uri.fromFile(new File(photoPath)));
        } else {
            holder.photo.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(view -> click.open(appointment));
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView device, status, issue, meta;
        final ImageView photo;

        Holder(View view) {
            super(view);
            device = view.findViewById(R.id.repairDevice);
            status = view.findViewById(R.id.repairStatus);
            issue = view.findViewById(R.id.repairIssue);
            meta = view.findViewById(R.id.repairMeta);
            photo = view.findViewById(R.id.repairPhoto);
        }
    }
}
