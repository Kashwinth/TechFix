package com.example.techfix.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.techfix.R;
import com.example.techfix.database.DatabaseHelper;
import com.example.techfix.models.User;
import com.example.techfix.utils.CustomerNavigation;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    public static final String EXTRA_RETURN_TO_TRACK = "return_to_track";

    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegisterLink;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if already logged in
        SharedPreferences prefs = getSharedPreferences("TechFixPrefs", Context.MODE_PRIVATE);
        boolean returnToTrack = getIntent().getBooleanExtra(EXTRA_RETURN_TO_TRACK, false);
        if (prefs.getBoolean("isLoggedIn", false)) {
            String role = prefs.getString("userRole", "customer");
            if ("manager".equalsIgnoreCase(role) || "admin".equalsIgnoreCase(role) || "staff".equalsIgnoreCase(role)) {
                startActivity(new Intent(LoginActivity.this, AdminDashboardActivity.class));
            } else if (returnToTrack) {
                startActivity(new Intent(LoginActivity.this, RepairHistoryActivity.class));
            } else {
                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            }
            finish();
            return;
        }

        setContentView(R.layout.activity_login);
        CustomerNavigation.bind(this, "profile");

        dbHelper = new DatabaseHelper(this);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegisterLink = findViewById(R.id.tvRegisterLink);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(LoginActivity.this, "Please enter all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Run database work outside the UI thread and recover from SQLite errors.
            btnLogin.setEnabled(false);
            new Thread(() -> {
                try {
                    User user = dbHelper.authenticateUser(email, password);
                    runOnUiThread(() -> {
                        btnLogin.setEnabled(true);
                        if (user != null) {
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean("isLoggedIn", true);
                            editor.putInt("userId", user.getId());
                            editor.putString("userName", user.getName());
                            editor.putString("userEmail", user.getEmail());
                            editor.putString("userRole", user.getRole());
                            editor.apply();

                            Toast.makeText(LoginActivity.this,
                                    "Login Successful! Welcome " + user.getName(), Toast.LENGTH_SHORT).show();
                            Intent destination;
                            if ("manager".equalsIgnoreCase(user.getRole()) || "admin".equalsIgnoreCase(user.getRole()) || "staff".equalsIgnoreCase(user.getRole())) {
                                destination = new Intent(this, AdminDashboardActivity.class);
                            } else if (returnToTrack) {
                                destination = new Intent(this, RepairHistoryActivity.class);
                            } else {
                                destination = new Intent(this, HomeActivity.class);
                            }
                            startActivity(destination);
                            finish();
                        } else {
                            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception exception) {
                    android.util.Log.e("LoginActivity", "Authentication failed", exception);
                    runOnUiThread(() -> {
                        btnLogin.setEnabled(true);
                        Toast.makeText(this,
                                "Unable to log in. Please try again.", Toast.LENGTH_LONG).show();
                    });
                }
            }).start();
        });

        tvRegisterLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }
}
