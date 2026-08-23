package com.example.techfix.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.techfix.R;
import com.example.techfix.adapters.RepairJobAdapter;
import com.example.techfix.database.DatabaseHelper;
import com.example.techfix.models.Appointment;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RepairHistoryActivity extends AppCompatActivity {
    private RepairJobAdapter adapter;
    private TextView empty;
    private DatabaseHelper db;
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_repair_history);
        db = new DatabaseHelper(this);
        empty = findViewById(R.id.tvHistoryEmpty);
        RecyclerView recyclerView = findViewById(R.id.rvRepairHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RepairJobAdapter(this::showDetails);
        recyclerView.setAdapter(adapter);
        findViewById(R.id.btnHistoryBack).setOnClickListener(view -> finish());
        loadAppointments();
    }

    @Override protected void onResume() {
        super.onResume();
        if (db != null && adapter != null) loadAppointments();
    }

    private void loadAppointments() {
        int userId = getSharedPreferences("TechFixPrefs", MODE_PRIVATE).getInt("userId", -1);
        io.execute(() -> {
            List<Appointment> appointments = db.getAppointmentsForUser(userId);
            runOnUiThread(() -> {
                adapter.submitList(appointments);
                empty.setVisibility(appointments.isEmpty() ? View.VISIBLE : View.GONE);
            });
        });
    }

    private void showDetails(Appointment appointment) {
        View content = LayoutInflater.from(this).inflate(R.layout.dialog_payment, null);
        LinearLayout paymentSection = content.findViewById(R.id.paymentSection);
        Button payNow = content.findViewById(R.id.btnPayNow);
        TextView amount = content.findViewById(R.id.tvPaymentAmount);
        amount.setText(String.format(java.util.Locale.US, "Amount Due: LKR %.2f", appointment.getPrice()));

        LinearLayout details = new LinearLayout(this);
        details.setOrientation(LinearLayout.VERTICAL);
        details.setPadding(24, 0, 24, 8);
        String device = appointment.getDeviceModel();
        TextView text = new TextView(this);
        text.setText("Device: " + (device == null || device.isEmpty() ? appointment.getDeviceCategory() : device)
                + "\nIssue: " + (appointment.getIssueDescription() == null ? "Not provided" : appointment.getIssueDescription())
                + "\nStatus: " + appointment.getStatus()
                + "\nTechnician: " + (appointment.getTechnicianName() == null ? "Unassigned" : appointment.getTechnicianName())
                + "\nBranch: " + appointment.getBranchName()
                + "\nParts used: none");
        text.setTextColor(getColor(R.color.tech_text_primary));
        text.setPadding(0, 16, 0, 16);
        details.addView(text);
        if (appointment.getPhotoPath() != null && new File(appointment.getPhotoPath()).exists()) {
            ImageView image = new ImageView(this);
            image.setImageURI(android.net.Uri.fromFile(new File(appointment.getPhotoPath())));
            image.setAdjustViewBounds(true);
            details.addView(image, 0);
        }
        ((LinearLayout) content).addView(details, 0);

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.ThemeOverlay_TechFix_Dialog)
                .setTitle("Repair details")
                .setView(content)
                .setPositiveButton("Close", null)
                .create();
        boolean completed = "Completed".equalsIgnoreCase(appointment.getStatus());
        paymentSection.setVisibility(completed ? View.VISIBLE : View.GONE);
        payNow.setOnClickListener(view -> io.execute(() -> {
            boolean paid = db.updateAppointmentStatus(appointment.getId(), "Paid");
            runOnUiThread(() -> {
                if (paid) {
                    dialog.dismiss();
                    Toast.makeText(this, "Payment recorded", Toast.LENGTH_LONG).show();
                    loadAppointments();
                } else {
                    Toast.makeText(this, "Unable to record payment", Toast.LENGTH_LONG).show();
                }
            });
        }));
        dialog.show();
    }

    @Override protected void onDestroy() {
        io.shutdown();
        if (db != null) db.close();
        super.onDestroy();
    }
}
