package com.example.techfix.data;
import androidx.room.*;
@Database(entities={Customer.class,Device.class,RepairJob.class,Part.class},version=1,exportSchema=false)
public abstract class TechFixDatabase extends RoomDatabase { public abstract RepairDao repairDao(); private static volatile TechFixDatabase INSTANCE; public static TechFixDatabase get(android.content.Context c){if(INSTANCE==null) synchronized(TechFixDatabase.class){if(INSTANCE==null) INSTANCE=Room.databaseBuilder(c.getApplicationContext(),TechFixDatabase.class,"techfix_repairs.db").build();}return INSTANCE;} }
