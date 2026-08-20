package com.example.techfix.models;

public class Appointment {
    private int id;
    private int userId;
    private int branchId;
    private String deviceCategory; // "Mobile" or "Laptop"
    private String status; // "Pending", "In Progress", "Completed"

    // Display fields
    private String userName;
    private String branchName;

    public Appointment() {}

    public Appointment(int id, int userId, int branchId, String deviceCategory, String status) {
        this.id = id;
        this.userId = userId;
        this.branchId = branchId;
        this.deviceCategory = deviceCategory;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getBranchId() {
        return branchId;
    }

    public void setBranchId(int branchId) {
        this.branchId = branchId;
    }

    public String getDeviceCategory() {
        return deviceCategory;
    }

    public void setDeviceCategory(String deviceCategory) {
        this.deviceCategory = deviceCategory;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }
}
