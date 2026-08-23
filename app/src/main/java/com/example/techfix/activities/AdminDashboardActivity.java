package com.example.techfix.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.techfix.R;
import com.example.techfix.adapters.AppointmentAdapter;
import com.example.techfix.database.DatabaseHelper;
import com.example.techfix.models.Appointment;
import com.example.techfix.models.Branch;
import com.example.techfix.models.RepairService;
import com.example.techfix.models.SparePart;
import com.example.techfix.models.Technician;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminDashboardActivity extends AppCompatActivity {

    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private DatabaseHelper db;
    private AppointmentAdapter adapter;
    private Spinner branches, parts;
    private TextView current;
    private EditText stock;
    private List<Branch> branchData = new ArrayList<>();
    private List<SparePart> partData = new ArrayList<>();
    private List<Technician> technicianData = new ArrayList<>();
    private Uri selectedSampleImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        db = new DatabaseHelper(this);
        branches = findViewById(R.id.spinnerBranches);
        parts = findViewById(R.id.spinnerParts);
        current = findViewById(R.id.tvCurrentStock);
        stock = findViewById(R.id.etNewStock);

        RecyclerView rv = findViewById(R.id.rvAdminAppointments);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AppointmentAdapter(new ArrayList<>(), true, a -> advance(a.getId(), a.getStatus()));
        rv.setAdapter(adapter);

        // Handle Logout
        findViewById(R.id.btnAdminLogout).setOnClickListener(v -> {
            getSharedPreferences("TechFixPrefs", Context.MODE_PRIVATE).edit().clear().apply();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        // Branch Selection Listener
        branches.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position <= 0) {
                    partData = new ArrayList<>();
                    parts.setAdapter(createSpinnerAdapter(withPlaceholder("Select a spare part", new ArrayList<>())));
                    return;
                }
                loadParts();
            }
        });

        // Part Selection Listener
        parts.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                showStock();
            }
        });

        findViewById(R.id.btnUpdateStock).setOnClickListener(v -> updateStock());
        findViewById(R.id.btnSaveAdminService).setOnClickListener(v -> saveService());
        setupCollapsible(R.id.headerStock, R.id.contentStock, R.id.iconStockToggle);
        setupCollapsible(R.id.headerService, R.id.contentService, R.id.iconServiceToggle);
        setupCollapsible(R.id.headerRequests, R.id.contentRequests, R.id.iconRequestsToggle);
        setupCollapsible(R.id.headerTechnicians, R.id.contentTechnicians, R.id.iconTechniciansToggle);
        Spinner technicianBranch = findViewById(R.id.spinnerTechnicianBranch);
        technicianBranch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onNothingSelected(AdapterView<?> p) {}
            public void onItemSelected(AdapterView<?> p, View v, int position, long id) { if (position > 0) loadTechnicians(); }
        });
        findViewById(R.id.btnAddTechnician).setOnClickListener(v -> addTechnician());
        findViewById(R.id.btnChooseSampleImage).setOnClickListener(v -> chooseSampleImage());
        findViewById(R.id.btnSaveSample).setOnClickListener(v -> saveSample());
        findViewById(R.id.btnGallery).setOnClickListener(v -> startActivity(new Intent(this, GalleryActivity.class)));
        View manageStaff = findViewById(R.id.btnManageStaff);
        String sessionRole = getSharedPreferences("TechFixPrefs", Context.MODE_PRIVATE).getString("userRole", "");
        boolean isManager = "manager".equalsIgnoreCase(sessionRole) || "admin".equalsIgnoreCase(sessionRole);
        manageStaff.setVisibility(isManager ? View.VISIBLE : View.GONE);
        manageStaff.setOnClickListener(v -> startActivity(new Intent(this, ManageStaffActivity.class)));

        loadAll();
    }

    private void loadAll() {
        io.execute(() -> {
            branchData = db.getAllBranches();
            List<String> names = new ArrayList<>();
            for (Branch b : branchData) {
                names.add(b.getLocationName());
            }

            List<Appointment> apps = db.getAllAppointments();

            runOnUiThread(() -> {
                branches.setAdapter(createSpinnerAdapter(withPlaceholder("Select a branch", names)));
                ((Spinner) findViewById(R.id.spinnerTechnicianBranch)).setAdapter(createSpinnerAdapter(withPlaceholder("Select technician branch", names)));
                adapter.updateList(apps);
                findViewById(R.id.tvAdminEmptyMessage).setVisibility(apps.isEmpty() ? View.VISIBLE : View.GONE);
            });
        });
    }

    private void loadTechnicians() {
        Spinner spinner = findViewById(R.id.spinnerTechnicianBranch);
        if (branchData.isEmpty() || spinner.getSelectedItemPosition() <= 0) return;
        int branchId = branchData.get(spinner.getSelectedItemPosition() - 1).getId();
        io.execute(() -> { technicianData = db.getTechnicians(branchId); runOnUiThread(this::renderTechnicians); });
    }

    private void renderTechnicians() {
        LinearLayout list = findViewById(R.id.technicianList); list.removeAllViews();
        for (Technician t : technicianData) { Switch row = new Switch(this); row.setText(t.getName()); row.setTextColor(getColor(R.color.tech_text_primary)); row.setChecked(t.isActive()); row.setOnCheckedChangeListener((button, checked) -> io.execute(() -> db.setTechnicianActive(t.getId(), checked))); list.addView(row); }
    }

    private void addTechnician() {
        EditText name = findViewById(R.id.etTechnicianName); Spinner spinner = findViewById(R.id.spinnerTechnicianBranch); String value = name.getText().toString().trim();
        if (value.isEmpty() || spinner.getSelectedItemPosition() <= 0) { name.setError("Enter a name and select a branch"); return; }
        int branchId = branchData.get(spinner.getSelectedItemPosition() - 1).getId();
        io.execute(() -> { boolean ok = db.addTechnician(value, branchId); runOnUiThread(() -> { Toast.makeText(this, ok ? "Technician added" : "Unable to add technician", Toast.LENGTH_SHORT).show(); if (ok) { name.setText(""); loadTechnicians(); } }); });
    }

    private void chooseSampleImage() { Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT); intent.setType("image/*"); intent.addCategory(Intent.CATEGORY_OPENABLE); startActivityForResult(intent, 71); }
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 71 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            try (InputStream in = getContentResolver().openInputStream(data.getData())) {
                File target = new File(getFilesDir(), "sample_" + System.currentTimeMillis() + ".jpg");
                try (FileOutputStream out = new FileOutputStream(target)) { byte[] buffer = new byte[8192]; int count; while ((count = in.read(buffer)) != -1) out.write(buffer, 0, count); }
                selectedSampleImage = Uri.fromFile(target);
                ((TextView)findViewById(R.id.tvSampleImage)).setText(target.getName());
            } catch (Exception e) { Toast.makeText(this, "Image could not be selected", Toast.LENGTH_LONG).show(); }
        }
    }
    private void saveSample() {
        EditText category = findViewById(R.id.etSampleCategory), description = findViewById(R.id.etSampleDescription); String c = category.getText().toString().trim(), d = description.getText().toString().trim();
        if (c.isEmpty() || d.isEmpty()) { category.setError("Category and description are required"); return; }
        io.execute(() -> { boolean ok = db.addRepairedSample(c, d, selectedSampleImage == null ? "" : selectedSampleImage.getPath(), -1); runOnUiThread(() -> Toast.makeText(this, ok ? "Sample repair saved" : "Unable to save sample", Toast.LENGTH_SHORT).show()); });
    }

    private void loadParts() {
        if (branchData.isEmpty() || branches.getSelectedItemPosition() <= 0) return;
        int id = branchData.get(branches.getSelectedItemPosition() - 1).getId();

        io.execute(() -> {
            partData = db.getSparePartsForBranch(id);
            List<String> n = new ArrayList<>();
            for (SparePart p : partData) {
                n.add(p.getPartName());
            }
            runOnUiThread(() -> parts.setAdapter(createSpinnerAdapter(withPlaceholder("Select a spare part", n))));
        });
    }

    private ArrayAdapter<String> createSpinnerAdapter(List<String> values) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, values);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        return adapter;
    }

    private List<String> withPlaceholder(String placeholder, List<String> values) {
        List<String> result = new ArrayList<>();
        result.add(placeholder);
        result.addAll(values);
        return result;
    }

    private void setupCollapsible(int headerId, int contentId, int iconId) {
        View header = findViewById(headerId);
        View content = findViewById(contentId);
        ImageView icon = findViewById(iconId);
        header.setOnClickListener(v -> {
            boolean open = content.getVisibility() != View.VISIBLE;
            content.setVisibility(open ? View.VISIBLE : View.GONE);
            icon.setRotation(open ? 0f : -90f);
        });
    }

    private void showStock() {
        if (partData.isEmpty() || parts.getSelectedItemPosition() <= 0) return;
        current.setText(String.valueOf(partData.get(parts.getSelectedItemPosition() - 1).getStockCount()));
    }

    private void updateStock() {
        if (partData.isEmpty() || parts.getSelectedItemPosition() <= 0) return;
        String value = stock.getText().toString().trim();
        try {
            int count = Integer.parseInt(value);
            if (count < 0) throw new NumberFormatException();

            SparePart p = partData.get(parts.getSelectedItemPosition() - 1);
            int branchId = branchData.get(branches.getSelectedItemPosition() - 1).getId();

            io.execute(() -> {
                boolean ok = db.updateSparePartStock(branchId, p.getPartName(), count);
                runOnUiThread(() -> {
                    Toast.makeText(this, ok ? "Inventory updated" : "Inventory update failed", Toast.LENGTH_SHORT).show();
                    if (ok) {
                        p.setStockCount(count);
                        showStock();
                        stock.setText("");
                    }
                });
            });
        } catch (NumberFormatException e) {
            stock.setError("Enter a non-negative whole number");
        }
    }

    private void saveService() {
        EditText c = findViewById(R.id.etAdminServiceCategory);
        EditText n = findViewById(R.id.etAdminServiceName);
        EditText p = findViewById(R.id.etAdminServicePrice);
        EditText im = findViewById(R.id.etAdminServiceImage);

        try {
            String category = c.getText().toString().trim();
            String name = n.getText().toString().trim();
            double price = Double.parseDouble(p.getText().toString().trim());

            if (category.isEmpty() || name.isEmpty() || price < 0) throw new IllegalArgumentException();

            RepairService s = new RepairService(0, category, name, price, im.getText().toString().trim());

            io.execute(() -> {
                boolean ok = db.saveService(s);
                runOnUiThread(() -> {
                    Toast.makeText(this, ok ? "Service saved" : "Service could not be saved", Toast.LENGTH_SHORT).show();
                    if (ok) {
                        c.setText("");
                        n.setText("");
                        p.setText("");
                        im.setText("");
                    }
                });
            });
        } catch (Exception e) {
            p.setError("Enter valid service details");
        }
    }

    private void advance(int id, String status) {
        String next = "Pending".equalsIgnoreCase(status) ? "In Progress" : "Completed";
        io.execute(() -> {
            boolean ok = db.updateAppointmentStatus(id, next);
            runOnUiThread(() -> {
                if (ok) {
                    Toast.makeText(this, "Status updated to " + next, Toast.LENGTH_SHORT).show();
                    loadAll();
                } else {
                    Toast.makeText(this, "Status update failed", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        io.shutdown();
        if (db != null) db.close();
        super.onDestroy();
    }
}