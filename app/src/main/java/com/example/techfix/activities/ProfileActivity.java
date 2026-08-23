package com.example.techfix.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.techfix.R;

public class ProfileActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_profile);
        android.content.SharedPreferences prefs = getSharedPreferences("TechFixPrefs", Context.MODE_PRIVATE);
        ((TextView) findViewById(R.id.tvProfileGreeting)).setText("Welcome, " + prefs.getString("userName", "Customer") + "!");
        ((TextView) findViewById(R.id.tvProfileEmail)).setText(prefs.getString("userEmail", ""));
        findViewById(R.id.btnProfileTrackRepairs).setOnClickListener(v -> startActivity(new Intent(this, RepairHistoryActivity.class)));
        findViewById(R.id.btnProfileLogout).setOnClickListener(v -> {
            prefs.edit().clear().apply();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}
