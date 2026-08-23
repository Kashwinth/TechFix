package com.example.techfix.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.content.Intent;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.example.techfix.models.Branch;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public final class BranchLocationHelper {
    public static final int LOCATION_PERMISSION_REQUEST = 701;
    private static final long MAX_CACHED_LOCATION_AGE_MS = 2 * 60 * 1000L;
    private static final long LOCATION_TIMEOUT_MS = 12_000L;

    private static final Branch COLOMBO = new Branch(1, "Colombo Branch",
            "Majestic City, 10 Station Road, Colombo 00400", 6.893982, 79.854749);
    private static final Branch GALLE = new Branch(2, "Galle Branch",
            "Galle Fort Clock Tower, Fort, Galle 80000", 6.032857, 80.214954);

    private BranchLocationHelper() {}

    public static List<Branch> branches() {
        return Arrays.asList(COLOMBO, GALLE);
    }

    public static String nearestBranchName(double latitude, double longitude) {
        double colomboDistance = distanceKm(latitude, longitude,
                COLOMBO.getLatitude(), COLOMBO.getLongitude());
        double galleDistance = distanceKm(latitude, longitude,
                GALLE.getLatitude(), GALLE.getLongitude());
        return colomboDistance < galleDistance
                ? COLOMBO.getLocationName() : GALLE.getLocationName();
    }

    public static void findNearest(Activity activity, Consumer<String> result) {
        boolean fine = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean coarse = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (!fine && !coarse) {
            ActivityCompat.requestPermissions(activity, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, LOCATION_PERMISSION_REQUEST);
            return;
        }

        LocationManager manager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        String provider = enabledProvider(manager);
        if (provider == null) {
            Toast.makeText(activity, "Turn on Location to find the nearest branch",
                    Toast.LENGTH_LONG).show();
            try {
                activity.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            } catch (Exception ignored) {
                // The explanatory toast is still useful if settings cannot be opened.
            }
            return;
        }

        try {
            Location cached = newestCachedLocation(manager);
            if (cached != null && System.currentTimeMillis() - cached.getTime()
                    <= MAX_CACHED_LOCATION_AGE_MS) {
                deliver(cached, result);
                return;
            }
            requestFreshLocation(activity, manager, provider, result);
        } catch (SecurityException e) {
            Toast.makeText(activity, "Location permission is required",
                    Toast.LENGTH_LONG).show();
        }
    }

    public static boolean handlePermissionResult(Activity activity, int requestCode,
                                                  int[] grantResults,
                                                  Consumer<String> result) {
        if (requestCode != LOCATION_PERMISSION_REQUEST) return false;
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                findNearest(activity, result);
                return true;
            }
        }
        Toast.makeText(activity,
                "Location permission was denied. You can enable it in app settings.",
                Toast.LENGTH_LONG).show();
        return true;
    }

    private static String enabledProvider(LocationManager manager) {
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return LocationManager.GPS_PROVIDER;
        }
        if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            return LocationManager.NETWORK_PROVIDER;
        }
        return null;
    }

    private static Location newestCachedLocation(LocationManager manager) {
        Location gps = manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                ? manager.getLastKnownLocation(LocationManager.GPS_PROVIDER) : null;
        Location network = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                ? manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) : null;
        if (gps == null) return network;
        if (network == null) return gps;
        return gps.getTime() >= network.getTime() ? gps : network;
    }

    private static void requestFreshLocation(Activity activity, LocationManager manager,
                                             String provider, Consumer<String> result) {
        Handler handler = new Handler(Looper.getMainLooper());
        final boolean[] completed = {false};
        Runnable timeout = () -> {
            if (!completed[0]) {
                completed[0] = true;
                Toast.makeText(activity,
                        "Unable to get a current location. Check your signal and try again.",
                        Toast.LENGTH_LONG).show();
            }
        };
        handler.postDelayed(timeout, LOCATION_TIMEOUT_MS);

        Consumer<Location> finish = location -> {
            if (completed[0]) return;
            completed[0] = true;
            handler.removeCallbacks(timeout);
            if (location == null) {
                Toast.makeText(activity, "Current location is unavailable",
                        Toast.LENGTH_LONG).show();
            } else {
                deliver(location, result);
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            manager.getCurrentLocation(provider, new CancellationSignal(),
                    activity.getMainExecutor(), finish::accept);
        } else {
            LocationListener listener = new LocationListener() {
                @Override public void onLocationChanged(Location location) {
                    manager.removeUpdates(this);
                    finish.accept(location);
                }
                @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
                @Override public void onProviderEnabled(String provider) {}
                @Override public void onProviderDisabled(String provider) {}
            };
            manager.requestSingleUpdate(provider, listener, Looper.getMainLooper());
            handler.postDelayed(() -> manager.removeUpdates(listener), LOCATION_TIMEOUT_MS);
        }
    }

    private static void deliver(Location location, Consumer<String> result) {
        result.accept(nearestBranchName(location.getLatitude(), location.getLongitude()));
    }

    private static double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double earthRadiusKm = 6371.0;
        double latitudeDelta = Math.toRadians(lat2 - lat1);
        double longitudeDelta = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latitudeDelta / 2) * Math.sin(latitudeDelta / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(longitudeDelta / 2) * Math.sin(longitudeDelta / 2);
        return earthRadiusKm * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
