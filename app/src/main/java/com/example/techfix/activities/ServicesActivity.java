package com.example.techfix.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.techfix.R;
import com.example.techfix.adapters.ServiceAdapter;
import com.example.techfix.database.DatabaseHelper;
import com.example.techfix.utils.CustomerNavigation;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServicesActivity extends AppCompatActivity {
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private DatabaseHelper db;
    private ServiceAdapter adapter;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_services);
        CustomerNavigation.bind(this, "services");
        db = new DatabaseHelper(this);

        RecyclerView list = findViewById(R.id.rvServices);
        list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ServiceAdapter(new ArrayList<>());
        list.setAdapter(adapter);

        TextInputEditText search = findViewById(R.id.etServiceSearch);
        Spinner category = findViewById(R.id.spinnerServiceCategory);
        category.setAdapter(new android.widget.ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"All", "Mobile", "Laptop", "Desktop"}));
        category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onNothingSelected(AdapterView<?> parent) { }
            @Override public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                load(search.getText() == null ? "" : search.getText().toString(),
                        parent.getItemAtPosition(position).toString());
            }
        });
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence value, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence value, int start, int before, int count) {
                load(value.toString(), category.getSelectedItem() == null
                        ? "All" : category.getSelectedItem().toString());
            }
            @Override public void afterTextChanged(Editable value) { }
        });
        load("", "All");
    }

    private void load(String query, String category) {
        io.execute(() -> {
            try {
                java.util.List<com.example.techfix.models.RepairService> data = db.getServices(query, category);
                runOnUiThread(() -> adapter.update(data));
            } catch (Exception exception) {
                runOnUiThread(() -> Toast.makeText(this, "Unable to load services", Toast.LENGTH_LONG).show());
            }
        });
    }

    @Override protected void onDestroy() {
        io.shutdown();
        if (db != null) db.close();
        super.onDestroy();
    }
}
