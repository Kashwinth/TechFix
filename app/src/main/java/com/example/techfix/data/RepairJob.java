package com.example.techfix.data;
import androidx.room.Entity; import androidx.room.PrimaryKey;
@Entity public class RepairJob { @PrimaryKey(autoGenerate=true) public long id; public long deviceId; public String issueType; public String status; public double estimatedCost; public long createdDate; public String technicianAssigned; public String photoPath; }
