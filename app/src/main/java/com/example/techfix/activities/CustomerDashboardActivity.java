package com.example.techfix.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.techfix.R;
import com.example.techfix.adapters.AppointmentAdapter;
import com.example.techfix.database.DatabaseHelper;
import com.example.techfix.models.Appointment;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

public class CustomerDashboardActivity extends AppCompatActivity {

    private TextView tvWelcomeUser, tvEmptyMessage;
    private MaterialButton btnBookAppointment, btnLogout;
    private RecyclerView rvAppointments;
    private AppointmentAdapter adapter;
    private List<Appointment> appointmentList = new ArrayList<>();
    private DatabaseHelper dbHelper;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_dashboard);

        dbHelper = new DatabaseHelper(this);

        SharedPreferences prefs = getSharedPreferences("TechFixPrefs", Context.MODE_PRIVATE);
        userId = prefs.getInt("userId", -1);
        String userName = prefs.getString("userName", "Customer");

        tvWelcomeUser = findViewById(R.id.tvWelcomeUser);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
        btnBookAppointment = findViewById(R.id.btnBookAppointment);
        btnLogout = findViewById(R.id.btnLogout);
        rvAppointments = findViewById(R.id.rvAppointments);

        tvWelcomeUser.setText("Welcome, " + userName + "!");

        rvAppointments.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AppointmentAdapter(appointmentList, false, this::handlePaymentSimulation);
        rvAppointments.setAdapter(adapter);

        btnBookAppointment.setOnClickListener(v -> {
            startActivity(new Intent(CustomerDashboardActivity.this, RepairRequestActivity.class));
        });

        btnLogout.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();
            startActivity(new Intent(CustomerDashboardActivity.this, LoginActivity.class));
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAppointments();
    }

    private void loadAppointments() {
        new Thread(() -> {
            List<Appointment> list = dbHelper.getAppointmentsForUser(userId);
            runOnUiThread(() -> {
                appointmentList.clear();
                appointmentList.addAll(list);
                adapter.updateList(appointmentList);

                if (appointmentList.isEmpty()) {
                    tvEmptyMessage.setVisibility(View.VISIBLE);
                } else {
                    tvEmptyMessage.setVisibility(View.GONE);
                }
            });
        }).start();
    }

    private void handlePaymentSimulation(Appointment appointment) {
        // Show simulated credit card payment dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_payment, null);
        builder.setView(dialogView);

        EditText etCardNumber = dialogView.findViewById(R.id.etCardNumber);
        EditText etExpiry = dialogView.findViewById(R.id.etExpiry);
        EditText etCVV = dialogView.findViewById(R.id.etCVV);
        Button btnPayNow = dialogView.findViewById(R.id.btnPayNow);

        AlertDialog dialog = builder.create();

        btnPayNow.setOnClickListener(v -> {
            String card = etCardNumber.getText().toString().trim();
            String expiry = etExpiry.getText().toString().trim();
            String cvv = etCVV.getText().toString().trim();

            if (card.length() < 16 || expiry.isEmpty() || cvv.length() < 3) {
                Toast.makeText(this, "Please enter valid payment details", Toast.LENGTH_SHORT).show();
                return;
            }

            // Simulate transaction
            Toast.makeText(this, "Processing payment of Rs. 4,500.00...", Toast.LENGTH_SHORT).show();

            new Thread(() -> {
                try {
                    Thread.sleep(1500); // Simulated delay
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // Update database
                dbHelper.updateAppointmentStatus(appointment.getId(), "Paid");

                runOnUiThread(() -> {
                    Toast.makeText(this, "Payment processed successfully! Appointment status is now Paid.", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                    loadAppointments(); // Refresh the list
                });
            }).start();
        });

        dialog.show();
    }
}
