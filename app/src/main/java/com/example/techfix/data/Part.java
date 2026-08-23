package com.example.techfix.data;
import androidx.room.Entity; import androidx.room.PrimaryKey;
@Entity public class Part { @PrimaryKey(autoGenerate=true) public long id; public long repairJobId; public String name; public int quantity; public double cost; }
