package com.example.techfix.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.techfix.models.Appointment;
import com.example.techfix.models.Branch;
import com.example.techfix.models.RepairService;
import com.example.techfix.models.SparePart;
import com.example.techfix.models.User;

import java.util.ArrayList;
import java.util.List;

/** SQLite gateway. All public methods are synchronous; callers must use a worker thread. */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "techfix.db";
    private static final int DATABASE_VERSION = 7;
    public static final String TABLE_USERS = "users", TABLE_BRANCHES = "branches",
            TABLE_SPARE_PARTS = "spare_parts", TABLE_APPOINTMENTS = "appointments",
            TABLE_SERVICES = "services", TABLE_TECHNICIANS = "technicians",
            TABLE_REPAIRED_SAMPLES = "repaired_samples";
    public static final String KEY_ID = "id", KEY_USER_NAME = "name", KEY_USER_EMAIL = "email",
            KEY_USER_PASSWORD = "password", KEY_USER_ROLE = "role";
    public static final String KEY_BRANCH_LOCATION_NAME = "location_name", KEY_BRANCH_ADDRESS = "address",
            KEY_BRANCH_LATITUDE = "latitude", KEY_BRANCH_LONGITUDE = "longitude";
    public static final String KEY_PART_NAME = "part_name", KEY_PART_BRANCH_ID = "branch_id",
            KEY_PART_STOCK_COUNT = "stock_count";
    public static final String KEY_APP_USER_ID = "user_id", KEY_APP_BRANCH_ID = "branch_id",
            KEY_APP_DEVICE_CATEGORY = "device_category", KEY_APP_DEVICE_MODEL = "device_model",
            KEY_APP_ISSUE = "issue_description", KEY_APP_STATUS = "status", KEY_APP_PRICE = "price",
            KEY_APP_SERVICE_ID = "service_id", KEY_APP_SERVICE_NAME = "service_name",
            KEY_APP_PHOTO_PATH = "photo_path",
            KEY_APP_TECHNICIAN_ID = "technician_id", KEY_APP_ASSIGNMENT_NOTE = "assignment_note";

    public DatabaseHelper(Context context) { super(context, DATABASE_NAME, null, DATABASE_VERSION); }
    @Override public void onConfigure(SQLiteDatabase db) { db.setForeignKeyConstraintsEnabled(true); }
    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,email TEXT UNIQUE NOT NULL,password TEXT NOT NULL,role TEXT NOT NULL)");
        db.execSQL("CREATE TABLE branches (id INTEGER PRIMARY KEY AUTOINCREMENT,location_name TEXT NOT NULL,address TEXT NOT NULL,latitude REAL NOT NULL,longitude REAL NOT NULL)");
        db.execSQL("CREATE TABLE spare_parts (id INTEGER PRIMARY KEY AUTOINCREMENT,part_name TEXT NOT NULL,branch_id INTEGER NOT NULL,stock_count INTEGER NOT NULL DEFAULT 0,UNIQUE(branch_id,part_name),FOREIGN KEY(branch_id) REFERENCES branches(id) ON DELETE CASCADE)");
        db.execSQL("CREATE TABLE services (id INTEGER PRIMARY KEY AUTOINCREMENT,category TEXT NOT NULL,name TEXT NOT NULL,price REAL NOT NULL DEFAULT 0,image_uri TEXT)");
        db.execSQL("CREATE TABLE technicians (id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,branch_id INTEGER NOT NULL,active INTEGER NOT NULL DEFAULT 1,FOREIGN KEY(branch_id) REFERENCES branches(id) ON DELETE CASCADE)");
        db.execSQL("CREATE TABLE appointments (id INTEGER PRIMARY KEY AUTOINCREMENT,user_id INTEGER NOT NULL,branch_id INTEGER NOT NULL,device_category TEXT NOT NULL,device_model TEXT,issue_description TEXT,service_id INTEGER,service_name TEXT,photo_path TEXT,price REAL NOT NULL DEFAULT 0,status TEXT NOT NULL DEFAULT 'Pending',technician_id INTEGER,assignment_note TEXT,FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE,FOREIGN KEY(branch_id) REFERENCES branches(id) ON DELETE CASCADE,FOREIGN KEY(service_id) REFERENCES services(id) ON DELETE SET NULL,FOREIGN KEY(technician_id) REFERENCES technicians(id) ON DELETE SET NULL)");
        db.execSQL("CREATE TABLE repaired_samples (id INTEGER PRIMARY KEY AUTOINCREMENT,category TEXT NOT NULL,description TEXT NOT NULL,image_uri TEXT,branch_id INTEGER,FOREIGN KEY(branch_id) REFERENCES branches(id) ON DELETE SET NULL)");
        seedData(db);
    }
    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            addColumn(db, TABLE_APPOINTMENTS, KEY_APP_DEVICE_MODEL, "TEXT");
            addColumn(db, TABLE_APPOINTMENTS, KEY_APP_ISSUE, "TEXT");
            addColumn(db, TABLE_APPOINTMENTS, KEY_APP_PRICE, "REAL NOT NULL DEFAULT 0");
            db.execSQL("CREATE TABLE IF NOT EXISTS services (id INTEGER PRIMARY KEY AUTOINCREMENT,category TEXT NOT NULL,name TEXT NOT NULL,price REAL NOT NULL DEFAULT 0,image_uri TEXT)");
            db.execSQL("CREATE TABLE IF NOT EXISTS technicians (id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,branch_id INTEGER NOT NULL,active INTEGER NOT NULL DEFAULT 1,FOREIGN KEY(branch_id) REFERENCES branches(id) ON DELETE CASCADE)");
            seedServices(db);
        }
        if (oldVersion < 3) {
            // Security migration: legacy admin accounts are manager accounts.
            db.execSQL("UPDATE users SET role='manager' WHERE lower(role)='admin'");
            seedManagerIfMissing(db);
        }
        if (oldVersion < 4) {
            addColumn(db, TABLE_BRANCHES, KEY_BRANCH_ADDRESS, "TEXT NOT NULL DEFAULT ''");
            updateBranch(db, "Colombo Branch", "Colombo Branch", "Majestic City, 10 Station Road, Colombo 00400", 6.893982, 79.854749);
            updateBranch(db, "Galle Branch", "Galle Branch", "Galle Fort Clock Tower, Fort, Galle 80000", 6.032857, 80.214954);
        }
        if (oldVersion < 5) {
            addColumn(db, TABLE_APPOINTMENTS, KEY_APP_TECHNICIAN_ID, "INTEGER");
            addColumn(db, TABLE_APPOINTMENTS, KEY_APP_ASSIGNMENT_NOTE, "TEXT");
            db.execSQL("CREATE TABLE IF NOT EXISTS repaired_samples (id INTEGER PRIMARY KEY AUTOINCREMENT,category TEXT NOT NULL,description TEXT NOT NULL,image_uri TEXT,branch_id INTEGER,FOREIGN KEY(branch_id) REFERENCES branches(id) ON DELETE SET NULL)");
        }
        if (oldVersion < 6) {
            addColumn(db, TABLE_APPOINTMENTS, KEY_APP_SERVICE_ID, "INTEGER");
            addColumn(db, TABLE_APPOINTMENTS, KEY_APP_SERVICE_NAME, "TEXT");
        }
        if (oldVersion < 7) {
            addColumn(db, TABLE_APPOINTMENTS, KEY_APP_PHOTO_PATH, "TEXT");
        }
    }
    private void seedManagerIfMissing(SQLiteDatabase db) {
        try (Cursor c = db.rawQuery("SELECT id FROM users WHERE lower(role)='manager' LIMIT 1", null)) {
            if (!c.moveToFirst()) {
                // Bootstrap manager credentials for local/demo installation. Change before deployment.
                addUser(db, "Admin Manager", "admin@techfix.com", "admin123", "manager");
            }
        }
    }
    @Override public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        seedManagerIfMissing(db);
    }
    private void addColumn(SQLiteDatabase db, String table, String column, String type) { try { db.execSQL("ALTER TABLE " + table + " ADD COLUMN " + column + " " + type); } catch (Exception ignored) { } }
    private void seedData(SQLiteDatabase db) {
        ContentValues b = new ContentValues(); b.put(KEY_BRANCH_LOCATION_NAME,"Colombo Branch"); b.put(KEY_BRANCH_ADDRESS,"Majestic City, 10 Station Road, Colombo 00400"); b.put(KEY_BRANCH_LATITUDE,6.893982); b.put(KEY_BRANCH_LONGITUDE,79.854749); long c=db.insert(TABLE_BRANCHES,null,b);
        b = new ContentValues(); b.put(KEY_BRANCH_LOCATION_NAME,"Galle Branch"); b.put(KEY_BRANCH_ADDRESS,"Galle Fort Clock Tower, Fort, Galle 80000"); b.put(KEY_BRANCH_LATITUDE,6.032857); b.put(KEY_BRANCH_LONGITUDE,80.214954); long g=db.insert(TABLE_BRANCHES,null,b);
        addUser(db,"Admin Manager","admin@techfix.com","admin123","manager"); addUser(db,"John Doe","john@gmail.com","user123","customer");
        addPart(db,"Mobile Screen",c,15); addPart(db,"Mobile Battery",c,20); addPart(db,"Laptop RAM",c,10); addPart(db,"Laptop Keyboard",c,8);
        addPart(db,"Mobile Screen",g,12); addPart(db,"Mobile Battery",g,5); addPart(db,"Laptop RAM",g,7); addPart(db,"Laptop Keyboard",g,0);
        addTech(db,"Nimal Perera",c); addTech(db,"Ayesha Silva",c); addTech(db,"Kasun Fernando",g); seedServices(db);
    }
    private void updateBranch(SQLiteDatabase db, String oldName, String name, String address, double latitude, double longitude) {
        ContentValues values = new ContentValues();
        values.put(KEY_BRANCH_LOCATION_NAME, name);
        values.put(KEY_BRANCH_ADDRESS, address);
        values.put(KEY_BRANCH_LATITUDE, latitude);
        values.put(KEY_BRANCH_LONGITUDE, longitude);
        db.update(TABLE_BRANCHES, values, KEY_BRANCH_LOCATION_NAME + "=?", new String[]{oldName});
    }
    private void addUser(SQLiteDatabase db,String n,String e,String p,String r){ContentValues v=new ContentValues();v.put(KEY_USER_NAME,n);v.put(KEY_USER_EMAIL,e);v.put(KEY_USER_PASSWORD,p);v.put(KEY_USER_ROLE,r);db.insert(TABLE_USERS,null,v);}
    private void addPart(SQLiteDatabase db,String n,long branch,int stock){ContentValues v=new ContentValues();v.put(KEY_PART_NAME,n);v.put(KEY_PART_BRANCH_ID,branch);v.put(KEY_PART_STOCK_COUNT,stock);db.insert(TABLE_SPARE_PARTS,null,v);}
    private void addTech(SQLiteDatabase db,String n,long branch){ContentValues v=new ContentValues();v.put("name",n);v.put("branch_id",branch);v.put("active",1);db.insert(TABLE_TECHNICIANS,null,v);}
    private void seedServices(SQLiteDatabase db){ addService(db,"Mobile","Screen Replacement",4500,"android.resource://com.example.techfix/drawable/techfix_logo"); addService(db,"Mobile","Battery Replacement",2800,"android.resource://com.example.techfix/drawable/techfix_logo"); addService(db,"Laptop","Keyboard Replacement",6500,"android.resource://com.example.techfix/drawable/techfix_logo"); addService(db,"Laptop","SSD Upgrade",12000,"android.resource://com.example.techfix/drawable/techfix_logo"); }
    private void addService(SQLiteDatabase db,String c,String n,double p,String i){ContentValues v=new ContentValues();v.put("category",c);v.put("name",n);v.put("price",p);v.put("image_uri",i);db.insert(TABLE_SERVICES,null,v);}

    public boolean registerUser(User u){ContentValues v=new ContentValues();v.put(KEY_USER_NAME,u.getName());v.put(KEY_USER_EMAIL,u.getEmail());v.put(KEY_USER_PASSWORD,u.getPassword());v.put(KEY_USER_ROLE,u.getRole());return getWritableDatabase().insert(TABLE_USERS,null,v)!=-1;}
    public User authenticateUser(String email,String password){try(Cursor c=getReadableDatabase().query(TABLE_USERS,null,"email=? AND password=?",new String[]{email,password},null,null,null)){if(c.moveToFirst()){User u=new User();u.setId(c.getInt(c.getColumnIndexOrThrow("id")));u.setName(c.getString(c.getColumnIndexOrThrow("name")));u.setEmail(c.getString(c.getColumnIndexOrThrow("email")));u.setPassword(c.getString(c.getColumnIndexOrThrow("password")));u.setRole(c.getString(c.getColumnIndexOrThrow("role")));return u;}}return null;}
    public boolean isEmailExists(String email){try(Cursor c=getReadableDatabase().query(TABLE_USERS,new String[]{"id"},"email=?",new String[]{email},null,null,null)){return c.moveToFirst();}}
    public boolean createStaffAccount(String username,String password,String role){if(!"manager".equalsIgnoreCase(role)&&!"staff".equalsIgnoreCase(role))return false;if(username==null||password==null||username.trim().isEmpty()||password.length()<6||isEmailExists(username.trim()))return false;ContentValues v=new ContentValues();v.put(KEY_USER_NAME,username.trim());v.put(KEY_USER_EMAIL,username.trim());v.put(KEY_USER_PASSWORD,password);v.put(KEY_USER_ROLE,role.toLowerCase(java.util.Locale.US));return getWritableDatabase().insert(TABLE_USERS,null,v)!=-1;}
    public List<Branch> getAllBranches(){List<Branch> out=new ArrayList<>();try(Cursor c=getReadableDatabase().rawQuery("SELECT * FROM branches ORDER BY location_name",null)){while(c.moveToNext()){out.add(new Branch(c.getInt(c.getColumnIndexOrThrow("id")),c.getString(c.getColumnIndexOrThrow("location_name")),c.getString(c.getColumnIndexOrThrow("address")),c.getDouble(c.getColumnIndexOrThrow("latitude")),c.getDouble(c.getColumnIndexOrThrow("longitude"))));}}return out;}
    public List<String> getActiveTechnicians(int branchId){List<String> out=new ArrayList<>();try(Cursor c=getReadableDatabase().query(TABLE_TECHNICIANS,new String[]{"name"},"branch_id=? AND active=1",new String[]{String.valueOf(branchId)},null,null,"name")){while(c.moveToNext())out.add(c.getString(0));}return out;}
    public List<com.example.techfix.models.Technician> getTechnicians(int branchId){List<com.example.techfix.models.Technician> out=new ArrayList<>();try(Cursor c=getReadableDatabase().query(TABLE_TECHNICIANS,null,"branch_id=?",new String[]{String.valueOf(branchId)},null,null,"name")){while(c.moveToNext())out.add(new com.example.techfix.models.Technician(c.getInt(c.getColumnIndexOrThrow("id")),c.getString(c.getColumnIndexOrThrow("name")),branchId,c.getInt(c.getColumnIndexOrThrow("active"))==1));}return out;}
    public String getAnyActiveTechnician(int branchId){List<String> names=getActiveTechnicians(branchId);return names.isEmpty()?null:names.get(0);}
    public List<SparePart> getSparePartsForBranch(int branchId){List<SparePart> out=new ArrayList<>();try(Cursor c=getReadableDatabase().query(TABLE_SPARE_PARTS,null,"branch_id=?",new String[]{String.valueOf(branchId)},null,null,"part_name")){while(c.moveToNext()){SparePart p=new SparePart(c.getInt(c.getColumnIndexOrThrow("id")),c.getString(c.getColumnIndexOrThrow("part_name")),branchId,c.getInt(c.getColumnIndexOrThrow("stock_count")));out.add(p);}}return out;}
    public boolean updateSparePartStock(int branchId,String part,int stock){ContentValues v=new ContentValues();v.put("stock_count",Math.max(0,stock));return getWritableDatabase().update(TABLE_SPARE_PARTS,v,"branch_id=? AND part_name=?",new String[]{String.valueOf(branchId),part})>0;}
    public int getSparePartStock(int branchId,String part){try(Cursor c=getReadableDatabase().query(TABLE_SPARE_PARTS,new String[]{"stock_count"},"branch_id=? AND part_name=?",new String[]{String.valueOf(branchId),part},null,null,null)){return c.moveToFirst()?c.getInt(0):0;}}
    public long addAppointment(Appointment a){ContentValues v=new ContentValues();v.put("user_id",a.getUserId());v.put("branch_id",a.getBranchId());v.put("device_category",a.getDeviceCategory());v.put("device_model",a.getDeviceModel());v.put("issue_description",a.getIssueDescription());if(a.getServiceId()>0)v.put("service_id",a.getServiceId());v.put("service_name",a.getServiceName());if(a.getPhotoPath()!=null)v.put("photo_path",a.getPhotoPath());v.put("status",a.getStatus());v.put("price",a.getPrice());if(a.getTechnicianName()!=null){int technicianId=findTechnicianId(a.getBranchId(),a.getTechnicianName());if(technicianId>0)v.put("technician_id",technicianId);}if(a.getAssignmentNote()!=null)v.put("assignment_note",a.getAssignmentNote());return getWritableDatabase().insert(TABLE_APPOINTMENTS,null,v);}
    private int findTechnicianId(int branchId,String name){try(Cursor c=getReadableDatabase().query(TABLE_TECHNICIANS,new String[]{"id"},"branch_id=? AND name=? AND active=1",new String[]{String.valueOf(branchId),name},null,null,null)){return c.moveToFirst()?c.getInt(0):0;}}
    private List<Appointment> appointments(String where,String[] args){List<Appointment> out=new ArrayList<>();String q="SELECT a.*,b.location_name,u.name,t.name AS technician_name FROM appointments a JOIN branches b ON a.branch_id=b.id JOIN users u ON a.user_id=u.id LEFT JOIN technicians t ON a.technician_id=t.id"+(where==null?"":" WHERE "+where)+" ORDER BY a.id DESC";try(Cursor c=getReadableDatabase().rawQuery(q,args)){while(c.moveToNext()){Appointment a=new Appointment();a.setId(c.getInt(c.getColumnIndexOrThrow("id")));a.setUserId(c.getInt(c.getColumnIndexOrThrow("user_id")));a.setBranchId(c.getInt(c.getColumnIndexOrThrow("branch_id")));a.setDeviceCategory(c.getString(c.getColumnIndexOrThrow("device_category")));a.setDeviceModel(c.getString(c.getColumnIndexOrThrow("device_model")));a.setIssueDescription(c.getString(c.getColumnIndexOrThrow("issue_description")));a.setServiceId(c.getInt(c.getColumnIndexOrThrow("service_id")));a.setServiceName(c.getString(c.getColumnIndexOrThrow("service_name")));a.setPhotoPath(c.getString(c.getColumnIndexOrThrow("photo_path")));a.setPrice(c.getDouble(c.getColumnIndexOrThrow("price")));a.setStatus(c.getString(c.getColumnIndexOrThrow("status")));a.setBranchName(c.getString(c.getColumnIndexOrThrow("location_name")));a.setUserName(c.getString(c.getColumnIndexOrThrow("name")));a.setTechnicianName(c.getString(c.getColumnIndexOrThrow("technician_name")));a.setAssignmentNote(c.getString(c.getColumnIndexOrThrow("assignment_note")));out.add(a);}}return out;}
    public List<Appointment> getAppointmentsForUser(int id){return appointments("a.user_id=?",new String[]{String.valueOf(id)});}
    public List<Appointment> getAllAppointments(){return appointments(null,null);}
    public boolean updateAppointmentStatus(int id,String status){ContentValues v=new ContentValues();v.put("status",status);return getWritableDatabase().update(TABLE_APPOINTMENTS,v,"id=?",new String[]{String.valueOf(id)})>0;}
    public List<RepairService> getServices(String search){return getServices(search,"All");}
    public List<RepairService> getAllServices(){return getServices("","All");}
    public List<RepairService> getServices(String search,String category){List<RepairService> out=new ArrayList<>();String s=search==null?"":search.trim();String cat=category==null?"All":category.trim();String where="(name LIKE ? OR category LIKE ?)";List<String> args=new ArrayList<>();args.add("%"+s+"%");args.add("%"+s+"%");if(!cat.isEmpty()&&!"All".equalsIgnoreCase(cat)){where+=" AND lower(category)=lower(?)";args.add(cat);}try(Cursor c=getReadableDatabase().query(TABLE_SERVICES,null,where,args.toArray(new String[0]),null,null,"category,name")){while(c.moveToNext())out.add(new RepairService(c.getInt(0),c.getString(1),c.getString(2),c.getDouble(3),c.getString(4)));}return out;}
    public boolean saveService(RepairService service){ContentValues v=new ContentValues();v.put("category",service.getCategory());v.put("name",service.getName());v.put("price",service.getPrice());v.put("image_uri",service.getImageUri());if(service.getId()>0)return getWritableDatabase().update(TABLE_SERVICES,v,"id=?",new String[]{String.valueOf(service.getId())})>0;return getWritableDatabase().insert(TABLE_SERVICES,null,v)>0;}
    public boolean deleteService(int id){return getWritableDatabase().delete(TABLE_SERVICES,"id=?",new String[]{String.valueOf(id)})>0;}
    public boolean addSparePart(int branchId,String name,int quantity){if(name==null||name.trim().isEmpty()||quantity<0)return false;ContentValues v=new ContentValues();v.put("branch_id",branchId);v.put("part_name",name.trim());v.put("stock_count",quantity);return getWritableDatabase().insertWithOnConflict(TABLE_SPARE_PARTS,null,v,SQLiteDatabase.CONFLICT_REPLACE)>0;}
    public boolean deleteSparePart(int id){return getWritableDatabase().delete(TABLE_SPARE_PARTS,"id=?",new String[]{String.valueOf(id)})>0;}
    public boolean addTechnician(String name,int branchId){if(name==null||name.trim().isEmpty())return false;ContentValues v=new ContentValues();v.put("name",name.trim());v.put("branch_id",branchId);v.put("active",1);return getWritableDatabase().insert(TABLE_TECHNICIANS,null,v)>0;}
    public boolean setTechnicianActive(int id,boolean active){ContentValues v=new ContentValues();v.put("active",active?1:0);return getWritableDatabase().update(TABLE_TECHNICIANS,v,"id=?",new String[]{String.valueOf(id)})>0;}
    public boolean addRepairedSample(String category,String description,String imageUri,int branchId){ContentValues v=new ContentValues();v.put("category",category);v.put("description",description);v.put("image_uri",imageUri);if(branchId>0)v.put("branch_id",branchId);return getWritableDatabase().insert(TABLE_REPAIRED_SAMPLES,null,v)>0;}
    public List<com.example.techfix.models.RepairedSample> getRepairedSamples(){List<com.example.techfix.models.RepairedSample> out=new ArrayList<>();String q="SELECT s.*,b.location_name AS branch_name FROM repaired_samples s LEFT JOIN branches b ON s.branch_id=b.id ORDER BY s.id DESC";try(Cursor c=getReadableDatabase().rawQuery(q,null)){while(c.moveToNext())out.add(new com.example.techfix.models.RepairedSample(c.getInt(c.getColumnIndexOrThrow("id")),c.getString(c.getColumnIndexOrThrow("category")),c.getString(c.getColumnIndexOrThrow("description")),c.getString(c.getColumnIndexOrThrow("image_uri")),c.getString(c.getColumnIndexOrThrow("branch_name"))));}return out;}
}
