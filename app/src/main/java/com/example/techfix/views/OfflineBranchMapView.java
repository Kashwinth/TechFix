package com.example.techfix.views;

import android.content.Context;
import android.util.AttributeSet;

import com.example.techfix.models.Branch;
import com.example.techfix.utils.BranchLocationHelper;
import com.example.techfix.utils.BranchMapLauncher;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class OfflineBranchMapView extends MapView implements OnMapReadyCallback {
    private final List<Branch> branches = BranchLocationHelper.branches();
    private GoogleMap googleMap;

    public OfflineBranchMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        MapsInitializer.initialize(context.getApplicationContext(),
                MapsInitializer.Renderer.LATEST, result -> { });
        onCreate(null);
        getMapAsync(this);
        setContentDescription("Google map showing TechFix branches in Colombo and Galle");
    }

    @Override public void onMapReady(GoogleMap map) {
        googleMap = map;
        map.getUiSettings().setZoomControlsEnabled(false);
        map.getUiSettings().setMapToolbarEnabled(false);
        map.getUiSettings().setCompassEnabled(true);
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.setInfoWindowAdapter(new BranchInfoWindow(getContext()));
        map.setOnInfoWindowClickListener(marker -> {
            Object tag = marker.getTag();
            if (tag instanceof Branch) {
                BranchMapLauncher.open(getContext(), (Branch) tag);
            }
        });

        LatLngBoundsBuilder bounds = new LatLngBoundsBuilder();
        for (Branch branch : branches) {
            LatLng position = new LatLng(branch.getLatitude(), branch.getLongitude());
            Marker marker = map.addMarker(new MarkerOptions()
                    .position(position)
                    .title(branch.getLocationName())
                    .snippet(branch.getAddress()));
            if (marker != null) {
                marker.setTag(branch);
                bounds.include(position);
            }
        }
        if (!branches.isEmpty()) {
            post(() -> map.moveCamera(
                    CameraUpdateFactory.newLatLngBounds(bounds.build(), 40)));
        }
    }

    @Override public void onAttachedToWindow() {
        super.onAttachedToWindow();
        onResume();
    }

    @Override public void onDetachedFromWindow() {
        onPause();
        onDestroy();
        super.onDetachedFromWindow();
    }

    private static final class LatLngBoundsBuilder {
        private final com.google.android.gms.maps.model.LatLngBounds.Builder builder =
                new com.google.android.gms.maps.model.LatLngBounds.Builder();

        void include(LatLng point) {
            builder.include(point);
        }

        com.google.android.gms.maps.model.LatLngBounds build() {
            return builder.build();
        }
    }

    private static final class BranchInfoWindow implements GoogleMap.InfoWindowAdapter {
        private final Context context;

        BranchInfoWindow(Context context) {
            this.context = context;
        }

        @Override public android.view.View getInfoWindow(Marker marker) {
            return null;
        }

        @Override public android.view.View getInfoContents(Marker marker) {
            android.widget.LinearLayout layout = new android.widget.LinearLayout(context);
            layout.setOrientation(android.widget.LinearLayout.VERTICAL);
            layout.setPadding(24, 16, 24, 16);
            layout.setBackgroundColor(android.graphics.Color.WHITE);

            android.widget.TextView title = new android.widget.TextView(context);
            title.setText(marker.getTitle());
            title.setTextColor(android.graphics.Color.BLACK);
            title.setTextSize(16);
            title.setTypeface(null, android.graphics.Typeface.BOLD);
            layout.addView(title);

            android.widget.TextView details = new android.widget.TextView(context);
            details.setText(marker.getSnippet() + "\nTap for directions");
            details.setTextColor(android.graphics.Color.DKGRAY);
            details.setTextSize(13);
            layout.addView(details);
            return layout;
        }
    }
}
