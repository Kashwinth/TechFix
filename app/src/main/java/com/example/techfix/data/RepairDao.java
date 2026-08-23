package com.example.techfix.data;
import androidx.room.*; import androidx.lifecycle.LiveData; import java.util.List;
@Dao public interface RepairDao { @Insert long insertCustomer(Customer c); @Insert long insertDevice(Device d); @Insert long insertJob(RepairJob j); @Insert void insertParts(List<Part> p); @Query("SELECT * FROM RepairJob ORDER BY createdDate DESC") LiveData<List<RepairJob>> jobs(); @Query("SELECT * FROM Device WHERE id=:id") Device device(long id); @Query("SELECT * FROM Part WHERE repairJobId=:id") List<Part> parts(long id); }
