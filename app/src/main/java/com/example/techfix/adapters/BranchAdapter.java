package com.example.techfix.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.techfix.R;
import com.example.techfix.models.Branch;
import com.example.techfix.utils.BranchMapLauncher;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BranchAdapter extends RecyclerView.Adapter<BranchAdapter.Holder> {
    private final List<Branch> items;
    private final Map<Integer, List<String>> techs;

    public BranchAdapter(List<Branch> items, Map<Integer, List<String>> techs) {
        this.items = items;
        this.techs = techs;
    }

    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_branch, parent, false));
    }

    @Override public void onBindViewHolder(@NonNull Holder holder, int position) {
        Branch branch = items.get(position);
        holder.name.setText(branch.getLocationName());
        holder.coords.setText(String.format(Locale.getDefault(),
                "%s\nCoordinates: %.6f, %.6f", branch.getAddress(),
                branch.getLatitude(), branch.getLongitude()));
        List<String> names = techs.get(branch.getId());
        holder.technicians.setText(names == null || names.isEmpty()
                ? "No active technicians"
                : "Active technicians: " + android.text.TextUtils.join(", ", names));
        holder.itemView.setOnClickListener(v -> BranchMapLauncher.open(v.getContext(), branch));
    }

    @Override public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView coords;
        final TextView technicians;

        Holder(View view) {
            super(view);
            name = view.findViewById(R.id.tvBranchName);
            coords = view.findViewById(R.id.tvBranchCoordinates);
            technicians = view.findViewById(R.id.tvBranchTechnicians);
        }
    }
}
