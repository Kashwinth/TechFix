package com.example.techfix.models;

public class RepairedSample {
    private int id;
    private String category;
    private String description;
    private String imageUri;
    private String branchName;

    public RepairedSample(int id, String category, String description, String imageUri, String branchName) {
        this.id = id;
        this.category = category;
        this.description = description;
        this.imageUri = imageUri;
        this.branchName = branchName;
    }

    public int getId() { return id; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public String getImageUri() { return imageUri; }
    public String getBranchName() { return branchName; }
}
