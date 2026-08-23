package com.example.techfix.models;

public class Appointment {
    private int id;
    private int userId;
    private int branchId;
    private String deviceCategory; // "Mobile" or "Laptop"
    private String deviceModel;
    private String issueDescription;
    private double price;
    private String status; // "Pending", "In Progress", "Completed", "Paid"

    // Display fields
    private String userName;
    private String branchName;
    private String technicianName;
    private String assignmentNote;

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

    public String getDeviceModel() { return deviceModel; }
    public void setDeviceModel(String deviceModel) { this.deviceModel = deviceModel; }
    public String getIssueDescription() { return issueDescription; }
    public void setIssueDescription(String issueDescription) { this.issueDescription = issueDescription; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

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

    public String getTechnicianName() { return technicianName; }
    public void setTechnicianName(String technicianName) { this.technicianName = technicianName; }
    public String getAssignmentNote() { return assignmentNote; }
    public void setAssignmentNote(String assignmentNote) { this.assignmentNote = assignmentNote; }
}
