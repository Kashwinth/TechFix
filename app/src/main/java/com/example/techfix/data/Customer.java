package com.example.techfix.data;
import androidx.room.Entity; import androidx.room.PrimaryKey;
@Entity public class Customer {
    @PrimaryKey(autoGenerate=true) public long id;
    public int primaryUserId;
    public String name;
    public String email;
    public Customer() { }
    public Customer(int userId, String n, String e) { primaryUserId=userId; name=n; email=e; }
}
