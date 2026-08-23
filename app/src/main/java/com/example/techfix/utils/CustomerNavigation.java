package com.example.techfix.utils;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.example.techfix.R;
import com.example.techfix.activities.CustomerDashboardActivity;
import com.example.techfix.activities.HomeActivity;
import com.example.techfix.activities.LoginActivity;
import com.example.techfix.activities.ProfileActivity;
import com.example.techfix.activities.RepairRequestActivity;
import com.example.techfix.activities.ServicesActivity;

public final class CustomerNavigation {
    private CustomerNavigation() { }

    public static void bind(Activity activity, String selected) {
        View root = activity.findViewById(R.id.customerBottomNav);
        if (root == null) return;

        root.findViewById(R.id.customerNavHome).setOnClickListener(v -> open(activity, HomeActivity.class, selected));
        root.findViewById(R.id.customerNavServices).setOnClickListener(v -> open(activity, ServicesActivity.class, selected));
        root.findViewById(R.id.customerNavBook).setOnClickListener(v -> open(activity, RepairRequestActivity.class, selected));
        root.findViewById(R.id.customerNavProfile).setOnClickListener(v -> open(activity, ProfileActivity.class, selected));

        int active = ContextCompat.getColor(activity, R.color.tech_accent);
        int inactive = ContextCompat.getColor(activity, R.color.tech_text_sub);
        setState(root, R.id.customerNavHome, R.id.customerNavHomeIcon, R.id.customerNavHomeLabel, "home".equals(selected), active, inactive);
        setState(root, R.id.customerNavServices, R.id.customerNavServicesIcon, R.id.customerNavServicesLabel, "services".equals(selected), active, inactive);
        setState(root, R.id.customerNavBook, R.id.customerNavBookIcon, R.id.customerNavBookLabel, "book".equals(selected), active, inactive);
        setState(root, R.id.customerNavProfile, R.id.customerNavProfileIcon, R.id.customerNavProfileLabel, "profile".equals(selected), active, inactive);
    }

    private static void setState(View root, int itemId, int iconId, int labelId, boolean active, int activeColor, int inactiveColor) {
        root.findViewById(itemId).setAlpha(active ? 1f : .78f);
        ((ImageView) root.findViewById(iconId)).setColorFilter(active ? activeColor : inactiveColor);
        ((TextView) root.findViewById(labelId)).setTextColor(active ? activeColor : inactiveColor);
        ((TextView) root.findViewById(labelId)).setTypeface(null, active ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
    }

    private static void open(Activity activity, Class<?> destination, String selected) {
        boolean loggedIn = activity.getSharedPreferences("TechFixPrefs", Activity.MODE_PRIVATE)
                .getBoolean("isLoggedIn", false);
        Class<?> resolved = destination;
        if (destination == HomeActivity.class && !loggedIn) {
            resolved = HomeActivity.class;
        } else if (destination == ProfileActivity.class && !loggedIn) {
            resolved = LoginActivity.class;
        } else if (destination == ProfileActivity.class
                && !(activity instanceof CustomerDashboardActivity)) {
            resolved = CustomerDashboardActivity.class;
        }
        if (resolved.getName().equals(activity.getClass().getName())) return;
        activity.startActivity(new Intent(activity, resolved));
    }
}
