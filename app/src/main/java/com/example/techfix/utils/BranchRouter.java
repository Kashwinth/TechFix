package com.example.techfix.utils;

import android.content.Context;
import com.example.techfix.database.DatabaseHelper;
import com.example.techfix.models.Branch;
import java.util.List;

public class BranchRouter {

    /**
     * Finds the nearest branch that has stock of the required spare part for the device category.
     * Uses the Haversine formula to calculate the distance between the user and branch locations.
     */
    public static Branch routeToNearestAvailableBranch(Context context, String deviceCategory, double userLat, double userLng) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        List<Branch> branches = dbHelper.getAllBranches();

        // Map device category to specific required spare part
        String requiredPartName;
        if ("Mobile".equalsIgnoreCase(deviceCategory)) {
            requiredPartName = "Mobile Screen";
        } else if ("Laptop".equalsIgnoreCase(deviceCategory)) {
            requiredPartName = "Laptop Keyboard";
        } else {
            requiredPartName = "Mobile Screen"; // Default fallback
        }

        Branch bestBranch = null;
        double minDistance = Double.MAX_VALUE;

        // Try to find the closest branch with stock > 0
        for (Branch branch : branches) {
            int stock = dbHelper.getSparePartStock(branch.getId(), requiredPartName);
            if (stock > 0) {
                double distance = calculateDistance(userLat, userLng, branch.getLatitude(), branch.getLongitude());
                if (distance < minDistance) {
                    minDistance = distance;
                    bestBranch = branch;
                }
            }
        }

        // If no branch has stock, fallback to the nearest branch regardless of stock
        if (bestBranch == null && !branches.isEmpty()) {
            minDistance = Double.MAX_VALUE;
            for (Branch branch : branches) {
                double distance = calculateDistance(userLat, userLng, branch.getLatitude(), branch.getLongitude());
                if (distance < minDistance) {
                    minDistance = distance;
                    bestBranch = branch;
                }
            }
        }

        // Ultimate fallback to first branch if everything else is empty
        if (bestBranch == null && !branches.isEmpty()) {
            bestBranch = branches.get(0);
        }

        return bestBranch;
    }

    /**
     * Calculates the distance between two points in kilometers using the Haversine formula.
     */
    private static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // distance in km
    }
}
