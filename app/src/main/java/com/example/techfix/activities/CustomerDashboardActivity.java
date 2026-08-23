package com.example.techfix.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.example.techfix.R;
import com.example.techfix.utils.BranchLocationHelper;
import com.example.techfix.utils.CustomerNavigation;
import com.example.techfix.database.DatabaseHelper;
import com.example.techfix.models.Appointment;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

public class CustomerDashboardActivity extends AppCompatActivity {
    private TextView tvWelcomeUser, tvActiveRepairTitle, tvActiveRepairStatus;
    private Button btnLogout;
    private DatabaseHelper db;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state); setContentView(R.layout.activity_customer_dashboard);
        SharedPreferences prefs = getSharedPreferences("TechFixPrefs", Context.MODE_PRIVATE);
        tvWelcomeUser = findViewById(R.id.tvWelcomeUser); tvWelcomeUser.setText("Welcome, " + prefs.getString("userName", "Customer") + "!");
        btnLogout = findViewById(R.id.btnLogout); db = new DatabaseHelper(this);
        tvActiveRepairTitle = findViewById(R.id.tvActiveRepairTitle); tvActiveRepairStatus = findViewById(R.id.tvActiveRepairStatus);
        btnLogout.setOnClickListener(v -> { prefs.edit().clear().apply(); startActivity(new Intent(this, LoginActivity.class)); finish(); });
        findViewById(R.id.btnCustomerTrackRepairs).setOnClickListener(v -> startActivity(new Intent(this, RepairHistoryActivity.class)));
        findViewById(R.id.btnCustomerGallery).setOnClickListener(v -> startActivity(new Intent(this, GalleryActivity.class)));
        findViewById(R.id.btnViewTechnicians).setOnClickListener(v -> startActivity(new Intent(this, BranchesActivity.class)));
        findViewById(R.id.btnCustomerNearestBranch).setOnClickListener(v -> findNearestBranch());
        CustomerNavigation.bind(this, "profile");
        loadCurrentRepair();
    }

    private void loadCurrentRepair() { int userId = getSharedPreferences("TechFixPrefs", 0).getInt("userId", -1); new Thread(() -> { List<Appointment> list = db.getAppointmentsForUser(userId); runOnUiThread(() -> { if (list.isEmpty()) { tvActiveRepairTitle.setText("No active repair"); tvActiveRepairStatus.setText("Book a repair to see its status here."); } else { Appointment a=list.get(0); tvActiveRepairTitle.setText(a.getDeviceModel()==null||a.getDeviceModel().isEmpty()?a.getDeviceCategory()+" repair":a.getDeviceModel()); String note=a.getAssignmentNote(); tvActiveRepairStatus.setText("Assigned to "+a.getBranchName()+" ("+a.getStatus()+")"+(note==null||note.isEmpty()?"":"\n"+note)); } }); }).start(); }
    private void findNearestBranch() {
        BranchLocationHelper.findNearest(this, branchName ->
                android.widget.Toast.makeText(this, "Nearest branch: " + branchName,
                        android.widget.Toast.LENGTH_LONG).show());
    }

    @Override public void onRequestPermissionsResult(int requestCode,
                                                     String[] permissions,
                                                     int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        BranchLocationHelper.handlePermissionResult(this, requestCode, grantResults,
                branchName -> android.widget.Toast.makeText(this,
                        "Nearest branch: " + branchName,
                        android.widget.Toast.LENGTH_LONG).show());
    }
}
