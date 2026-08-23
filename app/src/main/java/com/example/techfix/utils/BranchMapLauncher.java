package com.example.techfix.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.example.techfix.models.Branch;

import java.util.Locale;

public final class BranchMapLauncher {
    private BranchMapLauncher() {}

    public static void open(Context context, Branch branch) {
        String label = Uri.encode(branch.getLocationName());
        String geo = String.format(Locale.US, "geo:%f,%f?q=%f,%f(%s)",
                branch.getLatitude(), branch.getLongitude(), branch.getLatitude(),
                branch.getLongitude(), label);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(geo));
        mapIntent.addCategory(Intent.CATEGORY_DEFAULT);
        try {
            context.startActivity(mapIntent);
        } catch (ActivityNotFoundException e) {
            String webUrl = String.format(Locale.US,
                    "https://www.google.com/maps/search/?api=1&query=%f,%f",
                    branch.getLatitude(), branch.getLongitude());
            try {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)));
            } catch (ActivityNotFoundException ignored) {
                Toast.makeText(context, "No map application is available", Toast.LENGTH_LONG).show();
            }
        }
    }
}
