package com.example.techfix.data;
import androidx.room.Entity; import androidx.room.PrimaryKey;
@Entity public class Device { @PrimaryKey(autoGenerate=true) public long id; public long customerId; public String model; public String category; }
