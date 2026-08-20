package com.example.techfix.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.techfix.models.User;
import com.example.techfix.models.Branch;
import com.example.techfix.models.SparePart;
import com.example.techfix.models.Appointment;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "techfix.db";
    private static final int DATABASE_VERSION = 1;

    // Table names
    public static final String TABLE_USERS = "users";
    public static final String TABLE_BRANCHES = "branches";
    public static final String TABLE_SPARE_PARTS = "spare_parts";
    public static final String TABLE_APPOINTMENTS = "appointments";

    // Common columns
    public static final String KEY_ID = "id";

    // USERS Table columns
    public static final String KEY_USER_NAME = "name";
    public static final String KEY_USER_EMAIL = "email";
    public static final String KEY_USER_PASSWORD = "password";
    public static final String KEY_USER_ROLE = "role";

    // BRANCHES Table columns
    public static final String KEY_BRANCH_LOCATION_NAME = "location_name";
    public static final String KEY_BRANCH_LATITUDE = "latitude";
    public static final String KEY_BRANCH_LONGITUDE = "longitude";

    // SPARE_PARTS Table columns
    public static final String KEY_PART_NAME = "part_name";
    public static final String KEY_PART_BRANCH_ID = "branch_id";
    public static final String KEY_PART_STOCK_COUNT = "stock_count";

    // APPOINTMENTS Table columns
    public static final String KEY_APP_USER_ID = "user_id";
    public static final String KEY_APP_BRANCH_ID = "branch_id";
    public static final String KEY_APP_DEVICE_CATEGORY = "device_category";
    public static final String KEY_APP_STATUS = "status";

    // Create Tables SQL
    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + " ("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + KEY_USER_NAME + " TEXT, "
            + KEY_USER_EMAIL + " TEXT UNIQUE, "
            + KEY_USER_PASSWORD + " TEXT, "
            + KEY_USER_ROLE + " TEXT" + ");";

    private static final String CREATE_TABLE_BRANCHES = "CREATE TABLE " + TABLE_BRANCHES + " ("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + KEY_BRANCH_LOCATION_NAME + " TEXT, "
            + KEY_BRANCH_LATITUDE + " REAL, "
            + KEY_BRANCH_LONGITUDE + " REAL" + ");";

    private static final String CREATE_TABLE_SPARE_PARTS = "CREATE TABLE " + TABLE_SPARE_PARTS + " ("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + KEY_PART_NAME + " TEXT, "
            + KEY_PART_BRANCH_ID + " INTEGER, "
            + KEY_PART_STOCK_COUNT + " INTEGER, "
            + "FOREIGN KEY(" + KEY_PART_BRANCH_ID + ") REFERENCES " + TABLE_BRANCHES + "(" + KEY_ID + ") ON DELETE CASCADE" + ");";

    private static final String CREATE_TABLE_APPOINTMENTS = "CREATE TABLE " + TABLE_APPOINTMENTS + " ("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + KEY_APP_USER_ID + " INTEGER, "
            + KEY_APP_BRANCH_ID + " INTEGER, "
            + KEY_APP_DEVICE_CATEGORY + " TEXT, "
            + KEY_APP_STATUS + " TEXT, "
            + "FOREIGN KEY(" + KEY_APP_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + KEY_ID + ") ON DELETE CASCADE, "
            + "FOREIGN KEY(" + KEY_APP_BRANCH_ID + ") REFERENCES " + TABLE_BRANCHES + "(" + KEY_ID + ") ON DELETE CASCADE" + ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_BRANCHES);
        db.execSQL(CREATE_TABLE_SPARE_PARTS);
        db.execSQL(CREATE_TABLE_APPOINTMENTS);

        // Seed Initial Data
        seedData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_APPOINTMENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SPARE_PARTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BRANCHES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    private void seedData(SQLiteDatabase db) {
        // 1. Seed Branches (Colombo and Galle)
        ContentValues colomboBranch = new ContentValues();
        colomboBranch.put(KEY_BRANCH_LOCATION_NAME, "Colombo Branch");
        colomboBranch.put(KEY_BRANCH_LATITUDE, 6.9271);
        colomboBranch.put(KEY_BRANCH_LONGITUDE, 79.8612);
        long colomboId = db.insert(TABLE_BRANCHES, null, colomboBranch);

        ContentValues galleBranch = new ContentValues();
        galleBranch.put(KEY_BRANCH_LOCATION_NAME, "Galle Branch");
        galleBranch.put(KEY_BRANCH_LATITUDE, 6.0535);
        galleBranch.put(KEY_BRANCH_LONGITUDE, 80.2117);
        long galleId = db.insert(TABLE_BRANCHES, null, galleBranch);

        // 2. Seed Users (Admin and Customer)
        ContentValues adminUser = new ContentValues();
        adminUser.put(KEY_USER_NAME, "Admin Manager");
        adminUser.put(KEY_USER_EMAIL, "admin@techfix.com");
        adminUser.put(KEY_USER_PASSWORD, "admin123");
        adminUser.put(KEY_USER_ROLE, "admin");
        db.insert(TABLE_USERS, null, adminUser);

        ContentValues normalUser = new ContentValues();
        normalUser.put(KEY_USER_NAME, "John Doe");
        normalUser.put(KEY_USER_EMAIL, "john@gmail.com");
        normalUser.put(KEY_USER_PASSWORD, "user123");
        normalUser.put(KEY_USER_ROLE, "customer");
        db.insert(TABLE_USERS, null, normalUser);

        // 3. Seed Spare Parts
        // Colombo parts
        seedSparePart(db, "Mobile Screen", (int) colomboId, 15);
        seedSparePart(db, "Mobile Battery", (int) colomboId, 20);
        seedSparePart(db, "Laptop RAM", (int) colomboId, 10);
        seedSparePart(db, "Laptop Keyboard", (int) colomboId, 8);

        // Galle parts (Mobile Screen exists, Laptop Keyboard is OUT of stock)
        seedSparePart(db, "Mobile Screen", (int) galleId, 12);
        seedSparePart(db, "Mobile Battery", (int) galleId, 5);
        seedSparePart(db, "Laptop RAM", (int) galleId, 7);
        seedSparePart(db, "Laptop Keyboard", (int) galleId, 0); // Out of Stock!
    }

    private void seedSparePart(SQLiteDatabase db, String name, int branchId, int stock) {
        ContentValues values = new ContentValues();
        values.put(KEY_PART_NAME, name);
        values.put(KEY_PART_BRANCH_ID, branchId);
        values.put(KEY_PART_STOCK_COUNT, stock);
        db.insert(TABLE_SPARE_PARTS, null, values);
    }

    // USER OPERATIONS
    public boolean registerUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_USER_NAME, user.getName());
        values.put(KEY_USER_EMAIL, user.getEmail());
        values.put(KEY_USER_PASSWORD, user.getPassword());
        values.put(KEY_USER_ROLE, user.getRole());

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public User authenticateUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null,
                KEY_USER_EMAIL + "=? AND " + KEY_USER_PASSWORD + "=?",
                new String[]{email, password}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            User user = new User();
            user.setId(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)));
            user.setName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_NAME)));
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_EMAIL)));
            user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_PASSWORD)));
            user.setRole(cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_ROLE)));
            cursor.close();
            return user;
        }
        if (cursor != null) cursor.close();
        return null;
    }

    public boolean isEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{KEY_ID},
                KEY_USER_EMAIL + "=?", new String[]{email}, null, null, null);
        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) cursor.close();
        return exists;
    }

    // BRANCH OPERATIONS
    public List<Branch> getAllBranches() {
        List<Branch> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_BRANCHES, null);

        if (cursor.moveToFirst()) {
            do {
                Branch branch = new Branch();
                branch.setId(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)));
                branch.setLocationName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_BRANCH_LOCATION_NAME)));
                branch.setLatitude(cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_BRANCH_LATITUDE)));
                branch.setLongitude(cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_BRANCH_LONGITUDE)));
                list.add(branch);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public void addOrUpdateBranch(Branch branch) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_BRANCH_LOCATION_NAME, branch.getLocationName());
        values.put(KEY_BRANCH_LATITUDE, branch.getLatitude());
        values.put(KEY_BRANCH_LONGITUDE, branch.getLongitude());

        if (branch.getId() > 0) {
            db.update(TABLE_BRANCHES, values, KEY_ID + "=?", new String[]{String.valueOf(branch.getId())});
        } else {
            db.insert(TABLE_BRANCHES, null, values);
        }
    }

    // SPARE PARTS OPERATIONS
    public List<SparePart> getSparePartsForBranch(int branchId) {
        List<SparePart> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SPARE_PARTS, null, KEY_PART_BRANCH_ID + "=?",
                new String[]{String.valueOf(branchId)}, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                SparePart part = new SparePart();
                part.setId(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)));
                part.setPartName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_PART_NAME)));
                part.setBranchId(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_PART_BRANCH_ID)));
                part.setStockCount(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_PART_STOCK_COUNT)));
                list.add(part);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public boolean updateSparePartStock(int branchId, String partName, int newStock) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_PART_STOCK_COUNT, newStock);
        int rows = db.update(TABLE_SPARE_PARTS, values,
                KEY_PART_BRANCH_ID + "=? AND " + KEY_PART_NAME + "=?",
                new String[]{String.valueOf(branchId), partName});
        return rows > 0;
    }

    public int getSparePartStock(int branchId, String partName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SPARE_PARTS, new String[]{KEY_PART_STOCK_COUNT},
                KEY_PART_BRANCH_ID + "=? AND " + KEY_PART_NAME + "=?",
                new String[]{String.valueOf(branchId), partName}, null, null, null);

        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_PART_STOCK_COUNT));
            cursor.close();
        }
        return count;
    }

    // APPOINTMENT OPERATIONS
    public boolean addAppointment(Appointment app) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_APP_USER_ID, app.getUserId());
        values.put(KEY_APP_BRANCH_ID, app.getBranchId());
        values.put(KEY_APP_DEVICE_CATEGORY, app.getDeviceCategory());
        values.put(KEY_APP_STATUS, app.getStatus());

        long result = db.insert(TABLE_APPOINTMENTS, null, values);
        return result != -1;
    }

    public List<Appointment> getAppointmentsForUser(int userId) {
        List<Appointment> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT a.*, b." + KEY_BRANCH_LOCATION_NAME + " FROM " + TABLE_APPOINTMENTS + " a "
                + "JOIN " + TABLE_BRANCHES + " b ON a." + KEY_APP_BRANCH_ID + " = b." + KEY_ID
                + " WHERE a." + KEY_APP_USER_ID + " = ? ORDER BY a." + KEY_ID + " DESC";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                Appointment app = new Appointment();
                app.setId(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)));
                app.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_APP_USER_ID)));
                app.setBranchId(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_APP_BRANCH_ID)));
                app.setDeviceCategory(cursor.getString(cursor.getColumnIndexOrThrow(KEY_APP_DEVICE_CATEGORY)));
                app.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(KEY_APP_STATUS)));
                app.setBranchName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_BRANCH_LOCATION_NAME)));
                list.add(app);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public List<Appointment> getAllAppointments() {
        List<Appointment> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT a.*, b." + KEY_BRANCH_LOCATION_NAME + ", u." + KEY_USER_NAME + " FROM " + TABLE_APPOINTMENTS + " a "
                + "JOIN " + TABLE_BRANCHES + " b ON a." + KEY_APP_BRANCH_ID + " = b." + KEY_ID
                + " JOIN " + TABLE_USERS + " u ON a." + KEY_APP_USER_ID + " = u." + KEY_ID
                + " ORDER BY a." + KEY_ID + " DESC";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                Appointment app = new Appointment();
                app.setId(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)));
                app.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_APP_USER_ID)));
                app.setBranchId(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_APP_BRANCH_ID)));
                app.setDeviceCategory(cursor.getString(cursor.getColumnIndexOrThrow(KEY_APP_DEVICE_CATEGORY)));
                app.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(KEY_APP_STATUS)));
                app.setBranchName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_BRANCH_LOCATION_NAME)));
                app.setUserName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_NAME)));
                list.add(app);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public boolean updateAppointmentStatus(int appId, String newStatus) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_APP_STATUS, newStatus);
        int rows = db.update(TABLE_APPOINTMENTS, values, KEY_ID + "=?", new String[]{String.valueOf(appId)});
        return rows > 0;
    }
}
