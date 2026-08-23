package com.example.techfix.activities;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.example.techfix.R;

public class HomeActivity extends AppCompatActivity {
    private ViewGroup howItWorksCard;
    private TextView howItWorksDescription;
    private ImageView bookIcon, diagnoseIcon, repairIcon, trackIcon;
    private View activeStep;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state); setContentView(R.layout.activity_home);
        Button login=findViewById(R.id.btnHomeLogin), register=findViewById(R.id.btnHomeRegister);
        login.setOnClickListener(v->startActivity(new Intent(this,LoginActivity.class)));
        register.setOnClickListener(v->startActivity(new Intent(this,RegisterActivity.class)));
        findViewById(R.id.btnBookRepair).setOnClickListener(v->startActivity(new Intent(this,RepairRequestActivity.class)));
        findViewById(R.id.btnHomeViewServices).setOnClickListener(v->startActivity(new Intent(this,ServicesActivity.class)));
        findViewById(R.id.navServices).setOnClickListener(v->startActivity(new Intent(this,ServicesActivity.class)));
        findViewById(R.id.navBook).setOnClickListener(v->startActivity(new Intent(this,RepairRequestActivity.class)));
        findViewById(R.id.navProfile).setOnClickListener(v->startActivity(new Intent(this,LoginActivity.class)));
        findViewById(R.id.btnNearestBranch).setOnClickListener(v->findNearestBranch());
        howItWorksCard=findViewById(R.id.homeHowItWorksCard); howItWorksDescription=findViewById(R.id.homeHowItWorksDescription);
        bookIcon=findViewById(R.id.homeHowItWorksBookIcon); diagnoseIcon=findViewById(R.id.homeHowItWorksDiagnoseIcon); repairIcon=findViewById(R.id.homeHowItWorksRepairIcon); trackIcon=findViewById(R.id.homeHowItWorksTrackIcon); resetHighlights();
        bindStep(R.id.homeHowItWorksBook,bookIcon,"Choose your device and issue, and schedule a repair appointment at your nearest branch in just a few taps.");
        bindStep(R.id.homeHowItWorksDiagnose,diagnoseIcon,"Our technicians inspect your device and confirm the issue along with an accurate price estimate before any work begins.");
        bindStep(R.id.homeHowItWorksRepair,repairIcon,"Skilled technicians repair your device using genuine parts, keeping you updated on progress along the way.");
        bindStep(R.id.homeHowItWorksTrack,trackIcon,"Follow your repair status in real time from your dashboard, from drop-off to pickup.");
    }
    private void bindStep(int id,ImageView icon,String text){findViewById(id).setOnClickListener(v->{boolean collapse=activeStep==v;TransitionManager.beginDelayedTransition(howItWorksCard,new AutoTransition());resetHighlights();if(collapse){howItWorksDescription.setVisibility(View.GONE);activeStep=null;}else{howItWorksDescription.setText(text);howItWorksDescription.setVisibility(View.VISIBLE);icon.setAlpha(1f);activeStep=v;}});}
    private void resetHighlights(){bookIcon.setAlpha(.65f);diagnoseIcon.setAlpha(.65f);repairIcon.setAlpha(.65f);trackIcon.setAlpha(.65f);}
    private void findNearestBranch(){if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=getPackageManager().PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},7);return;}android.location.LocationManager lm=(android.location.LocationManager)getSystemService(LOCATION_SERVICE);Location here=lm.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER);if(here==null)here=lm.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER);if(here==null){Toast.makeText(this,"Current GPS location is unavailable",Toast.LENGTH_LONG).show();return;}Location c=new Location("branch");c.setLatitude(6.893982);c.setLongitude(79.854749);Location g=new Location("branch");g.setLatitude(6.032857);g.setLongitude(80.214954);Toast.makeText(this,"Nearest branch: "+(here.distanceTo(c)<here.distanceTo(g)?"Colombo Branch":"Galle Branch"),Toast.LENGTH_LONG).show();}
}
