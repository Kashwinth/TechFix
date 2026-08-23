package com.example.techfix.adapters;

import android.net.Uri;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.techfix.R;
import com.example.techfix.models.RepairedSample;
import java.io.File;
import java.util.*;

public class RepairedSampleAdapter extends RecyclerView.Adapter<RepairedSampleAdapter.Holder> {
    private final List<RepairedSample> items = new ArrayList<>();
    public void update(List<RepairedSample> value) { items.clear(); items.addAll(value); notifyDataSetChanged(); }
    @NonNull public Holder onCreateViewHolder(@NonNull ViewGroup p, int t) { return new Holder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_repaired_sample, p, false)); }
    public void onBindViewHolder(@NonNull Holder h, int i) { RepairedSample s=items.get(i); h.category.setText(s.getCategory()); h.description.setText(s.getDescription()); h.branch.setText(s.getBranchName()==null?"":"Branch: "+s.getBranchName()); if(s.getImageUri()!=null&&!s.getImageUri().isEmpty()) h.image.setImageURI(Uri.fromFile(new File(s.getImageUri()))); else h.image.setImageResource(R.drawable.techfix_logo); }
    public int getItemCount(){return items.size();}
    static class Holder extends RecyclerView.ViewHolder { ImageView image; TextView category,description,branch; Holder(View v){super(v);image=v.findViewById(R.id.ivSample);category=v.findViewById(R.id.tvSampleCategory);description=v.findViewById(R.id.tvSampleDescription);branch=v.findViewById(R.id.tvSampleBranch);} }
}
