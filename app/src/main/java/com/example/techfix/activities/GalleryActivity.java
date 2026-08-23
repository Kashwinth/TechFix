package com.example.techfix.activities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;
import com.example.techfix.R;
import com.example.techfix.adapters.RepairedSampleAdapter;
import com.example.techfix.database.DatabaseHelper;
import java.util.concurrent.*;

public class GalleryActivity extends AppCompatActivity {
    private final ExecutorService io=Executors.newSingleThreadExecutor(); private DatabaseHelper db;
    protected void onCreate(Bundle b){super.onCreate(b);setContentView(R.layout.activity_gallery);db=new DatabaseHelper(this);RecyclerView rv=findViewById(R.id.rvGallery);rv.setLayoutManager(new GridLayoutManager(this,2));RepairedSampleAdapter a=new RepairedSampleAdapter();rv.setAdapter(a);findViewById(R.id.btnGalleryBack).setOnClickListener(v->finish());io.execute(()->{try{java.util.List<com.example.techfix.models.RepairedSample> data=db.getRepairedSamples();runOnUiThread(()->a.update(data));}catch(Exception e){runOnUiThread(()->Toast.makeText(this,"Unable to load gallery",Toast.LENGTH_LONG).show());}});}
    protected void onDestroy(){io.shutdown();if(db!=null)db.close();super.onDestroy();}
}
