package com.example.techfix.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.techfix.R;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Change the IDs inside the brackets to match your XML
        Button btnNavLogin = findViewById(R.id.btnHomeLogin);
        Button btnNavRegister = findViewById(R.id.btnHomeRegister);

        btnNavLogin.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, LoginActivity.class)));

        btnNavRegister.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, RegisterActivity.class)));
    }
}