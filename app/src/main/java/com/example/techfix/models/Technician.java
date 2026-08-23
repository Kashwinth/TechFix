package com.example.techfix.models;

public class Technician {
    private int id;
    private String name;
    private int branchId;
    private boolean active;

    public Technician(int id, String name, int branchId, boolean active) {
        this.id = id;
        this.name = name;
        this.branchId = branchId;
        this.active = active;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getBranchId() { return branchId; }
    public boolean isActive() { return active; }
}
