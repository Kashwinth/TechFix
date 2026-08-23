package com.example.techfix.data;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities={Customer.class,Device.class,RepairJob.class,Part.class},version=2,exportSchema=false)
public abstract class TechFixDatabase extends RoomDatabase {
    public abstract RepairDao repairDao();
    private static volatile TechFixDatabase INSTANCE;

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override public void migrate(SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE Customer ADD COLUMN primaryUserId INTEGER NOT NULL DEFAULT -1");
            db.execSQL("ALTER TABLE RepairJob ADD COLUMN appointmentId INTEGER NOT NULL DEFAULT -1");
            db.execSQL("ALTER TABLE RepairJob ADD COLUMN primaryUserId INTEGER NOT NULL DEFAULT -1");
            db.execSQL("ALTER TABLE RepairJob ADD COLUMN serviceId INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE RepairJob ADD COLUMN serviceName TEXT");
        }
    };

    public static TechFixDatabase get(android.content.Context context) {
        if (INSTANCE == null) synchronized (TechFixDatabase.class) {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                TechFixDatabase.class, "techfix_repairs.db")
                        .addMigrations(MIGRATION_1_2)
                        .build();
            }
        }
        return INSTANCE;
    }
}
