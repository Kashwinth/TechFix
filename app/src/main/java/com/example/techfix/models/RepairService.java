package com.example.techfix.models;

/** Immutable-enough display/domain model for a repair service. */
public class RepairService {
    private int id;
    private String category;
    private String name;
    private double price;
    private String imageUri;

    public RepairService() { }

    public RepairService(int id, String category, String name, double price, String imageUri) {
        this.id = id;
        this.category = category;
        this.name = name;
        this.price = price;
        this.imageUri = imageUri;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getImageUri() { return imageUri; }
    public void setImageUri(String imageUri) { this.imageUri = imageUri; }
}
