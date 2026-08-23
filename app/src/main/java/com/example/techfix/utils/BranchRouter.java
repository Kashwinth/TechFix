package com.example.techfix.utils;

import android.content.Context;
import com.example.techfix.database.DatabaseHelper;
import com.example.techfix.models.Branch;
import java.util.*;

public class BranchRouter {
    public static class RouteResult {
        public Branch nearest;
        public Branch eligible;
        public Branch fallback;
        public String requiredPart;
        public String reason;
    }

    public static RouteResult evaluate(Context context, String category, double userLat, double userLng) {
        DatabaseHelper db = new DatabaseHelper(context);
        List<Branch> branches = db.getAllBranches();
        String part = requiredPart(category);
        branches.sort(Comparator.comparingDouble(b -> distance(userLat, userLng, b.getLatitude(), b.getLongitude())));
        RouteResult result = new RouteResult(); result.requiredPart = part;
        if (branches.isEmpty()) return result;
        result.nearest = branches.get(0);
        for (Branch branch : branches) {
            boolean hasTech = !db.getActiveTechnicians(branch.getId()).isEmpty();
            boolean hasPart = db.getSparePartStock(branch.getId(), part) > 0;
            if (hasTech && hasPart) { result.eligible = branch; break; }
        }
        if (result.eligible != null && result.nearest != result.eligible) {
            result.fallback = result.eligible;
            boolean tech = !db.getActiveTechnicians(result.nearest.getId()).isEmpty();
            boolean stock = db.getSparePartStock(result.nearest.getId(), part) > 0;
            result.reason = !tech && !stock ? "technician availability and the required spare part: " + part + "" : (!tech ? "technician availability" : "the required spare part: " + part + "");
        }
        db.close(); return result;
    }

    public static String requiredPart(String category) {
        if ("Mobile".equalsIgnoreCase(category)) return "Mobile Screen";
        if ("Laptop".equalsIgnoreCase(category)) return "Laptop Keyboard";
        return "Mobile Screen";
    }
    public static double distance(double lat1,double lon1,double lat2,double lon2){double r=6371, a=Math.sin(Math.toRadians(lat2-lat1)/2)*Math.sin(Math.toRadians(lat2-lat1)/2)+Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2))*Math.sin(Math.toRadians(lon2-lon1)/2)*Math.sin(Math.toRadians(lon2-lon1)/2);return r*2*Math.atan2(Math.sqrt(a),Math.sqrt(1-a));}
    public static Branch routeToNearestAvailableBranch(Context context,String category,double lat,double lng){RouteResult r=evaluate(context,category,lat,lng);return r.eligible!=null?r.eligible:r.nearest;}
}
