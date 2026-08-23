package com.example.techfix.data;
import androidx.room.Entity; import androidx.room.PrimaryKey;
@Entity public class Customer { @PrimaryKey(autoGenerate=true) public long id; public String name; public String email; public Customer() {} public Customer(String n,String e){name=n;email=e;} }
