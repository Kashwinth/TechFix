package com.example.techfix.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.techfix.R;
import com.example.techfix.database.DatabaseHelper;
import com.example.techfix.models.Appointment;
import com.example.techfix.models.Branch;
import com.example.techfix.utils.BranchRouter;
import com.google.android.material.textfield.TextInputEditText;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RepairRequestActivity extends AppCompatActivity {
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private DatabaseHelper db;
    private Spinner category, branch;
    private TextInputEditText model, issue;
    private final List<Branch> branches = new ArrayList<>();
    private String photoPath;
    private ImageView preview;
    private static final int CAMERA = 42;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state); setContentView(R.layout.activity_repair_request);
        db = new DatabaseHelper(this);
        category = findViewById(R.id.spinnerCategory); branch = findViewById(R.id.spinnerBranch);
        model = findViewById(R.id.etDeviceModel); issue = findViewById(R.id.etIssueDescription); preview = findViewById(R.id.photoPreview);
        category.setAdapter(ArrayAdapter.createFromResource(this, R.array.device_categories, android.R.layout.simple_spinner_dropdown_item));
        findViewById(R.id.btnCapturePhoto).setOnClickListener(v -> capture());
        findViewById(R.id.btnSubmitRepair).setOnClickListener(v -> submit());
        loadBranches();
    }

    private void loadBranches() { io.execute(() -> { branches.addAll(db.getAllBranches()); List<String> names = new ArrayList<>(); for (Branch b : branches) names.add(b.getLocationName()); runOnUiThread(() -> branch.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, names))); }); }
    private void capture() { if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.CAMERA) != getPackageManager().PERMISSION_GRANTED) { requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA); return; } startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), CAMERA); }
    @Override protected void onActivityResult(int request, int result, @Nullable Intent data) { super.onActivityResult(request, result, data); if (request == CAMERA && result == RESULT_OK && data != null && data.getExtras() != null) { Bitmap bitmap = (Bitmap)data.getExtras().get("data"); try { File file = new File(getFilesDir(), "repair_" + System.currentTimeMillis() + ".jpg"); FileOutputStream out = new FileOutputStream(file); bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out); out.close(); photoPath = file.getAbsolutePath(); preview.setImageBitmap(bitmap); preview.setVisibility(View.VISIBLE); } catch (IOException e) { Toast.makeText(this, "Photo could not be saved", Toast.LENGTH_LONG).show(); } } }

    private void submit() {
        String deviceModel = text(model), description = text(issue);
        int user = getSharedPreferences("TechFixPrefs", 0).getInt("userId", -1);
        if (user < 1) { Toast.makeText(this, "Please log in again", Toast.LENGTH_LONG).show(); return; }
        if (TextUtils.isEmpty(deviceModel)) { model.setError("Device model is required"); return; }
        if (TextUtils.isEmpty(description)) { issue.setError("Describe the issue"); return; }
        if (branches.isEmpty() || branch.getSelectedItemPosition() < 0) { Toast.makeText(this, "Select an available branch", Toast.LENGTH_LONG).show(); return; }
        findViewById(R.id.btnSubmitRepair).setEnabled(false);
        String selectedCategory = category.getSelectedItem().toString();
        String normalizedCategory = selectedCategory.toLowerCase().contains("laptop") || selectedCategory.toLowerCase().contains("desktop") ? "Laptop" : selectedCategory;
        Branch origin = branches.get(branch.getSelectedItemPosition());
        io.execute(() -> {
            BranchRouter.RouteResult route = BranchRouter.evaluate(this, normalizedCategory, origin.getLatitude(), origin.getLongitude());
            runOnUiThread(() -> handleRoute(route, user, normalizedCategory, deviceModel, description));
        });
    }

    private void handleRoute(BranchRouter.RouteResult route, int user, String categoryName, String deviceModel, String description) {
        if (route.eligible != null && route.fallback == null) {
            createAppointment(user, categoryName, deviceModel, description, route.eligible, null, "");
        } else if (route.fallback != null) {
            String message = route.nearest.getLocationName() + " doesn't currently have " + route.reason + ".\n\nWe'd like to assign your request to " + route.fallback.getLocationName() + " instead, which has both available.\n\nDo you want to proceed?";
            new AlertDialog.Builder(this).setTitle("Confirm branch assignment").setMessage(message).setPositiveButton("Confirm & Assign to " + route.fallback.getLocationName(), (d, w) -> createAppointment(user, categoryName, deviceModel, description, route.fallback, "reassigned — " + route.reason, "" )).setNegativeButton("Cancel", (d, w) -> findViewById(R.id.btnSubmitRepair).setEnabled(true)).setOnCancelListener(d -> findViewById(R.id.btnSubmitRepair).setEnabled(true)).show();
        } else {
            new AlertDialog.Builder(this).setTitle("Manual review required").setMessage("No branch currently has the required technician and spare parts available for this service. Your request will need manual review — would you like to submit anyway?").setPositiveButton("Submit anyway", (d, w) -> createAppointment(user, categoryName, deviceModel, description, route.nearest, null, "Manual review — no branch has both technician and required part: " + route.requiredPart)).setNegativeButton("Cancel", (d, w) -> findViewById(R.id.btnSubmitRepair).setEnabled(true)).setOnCancelListener(d -> findViewById(R.id.btnSubmitRepair).setEnabled(true)).show();
        }
    }

    private void createAppointment(int user, String categoryName, String deviceModel, String description, Branch assigned, String note, String manualNote) {
        io.execute(() -> {
            Appointment a = new Appointment(); a.setUserId(user); a.setBranchId(assigned.getId()); a.setDeviceCategory(categoryName); a.setDeviceModel(deviceModel); a.setIssueDescription(description); a.setStatus(manualNote.isEmpty() ? "Pending" : "Manual Review"); a.setPrice(4500); a.setTechnicianName(manualNote.isEmpty() ? db.getAnyActiveTechnician(assigned.getId()) : null); a.setAssignmentNote(note == null ? manualNote : note);
            boolean ok = db.addAppointment(a);
            if (ok) insertRoomHistory(user, deviceModel, categoryName, description);
            runOnUiThread(() -> { Toast.makeText(this, ok ? "Repair request submitted to " + assigned.getLocationName() : "Unable to submit request", Toast.LENGTH_LONG).show(); if (ok) finish(); else findViewById(R.id.btnSubmitRepair).setEnabled(true); });
        });
    }
    private void insertRoomHistory(int user, String deviceModel, String categoryName, String description) {
        com.example.techfix.data.TechFixDatabase room = com.example.techfix.data.TechFixDatabase.get(this);
        com.example.techfix.data.Customer customer = new com.example.techfix.data.Customer("Customer " + user, "local" + user + "@techfix");
        long customerId = room.repairDao().insertCustomer(customer);
        com.example.techfix.data.Device device = new com.example.techfix.data.Device(); device.customerId = customerId; device.model = deviceModel; device.category = categoryName;
        long deviceId = room.repairDao().insertDevice(device);
        com.example.techfix.data.RepairJob job = new com.example.techfix.data.RepairJob(); job.deviceId = deviceId; job.issueType = description; job.status = "pending"; job.estimatedCost = 4500; job.createdDate = System.currentTimeMillis(); job.photoPath = photoPath; room.repairDao().insertJob(job);
    }
    private String text(TextInputEditText e) { return e.getText() == null ? "" : e.getText().toString().trim(); }
    @Override protected void onDestroy() { io.shutdown(); if (db != null) db.close(); super.onDestroy(); }
}
