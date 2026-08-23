package com.example.techfix.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface RepairDao {
    @Insert long insertCustomer(Customer customer);
    @Insert long insertDevice(Device device);
    @Insert long insertJob(RepairJob job);
    @Insert void insertParts(List<Part> parts);

    @Query("SELECT id FROM Customer WHERE primaryUserId=:userId LIMIT 1")
    Long customerIdForUser(int userId);

    @Query("SELECT * FROM RepairJob WHERE primaryUserId=:userId ORDER BY createdDate DESC")
    LiveData<List<RepairJob>> jobsForUser(int userId);

    @Query("UPDATE RepairJob SET status=:status WHERE appointmentId=:appointmentId")
    int updateStatusForAppointment(long appointmentId, String status);

    @Query("SELECT * FROM Device WHERE id=:id") Device device(long id);
    @Query("SELECT * FROM Part WHERE repairJobId=:id") List<Part> parts(long id);
}
