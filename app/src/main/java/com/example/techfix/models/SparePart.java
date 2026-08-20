package com.example.techfix.models;

public class SparePart {
    private int id;
    private String partName;
    private int branchId;
    private int stockCount;

    public SparePart() {}

    public SparePart(int id, String partName, int branchId, int stockCount) {
        this.id = id;
        this.partName = partName;
        this.branchId = branchId;
        this.stockCount = stockCount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPartName() {
        return partName;
    }

    public void setPartName(String partName) {
        this.partName = partName;
    }

    public int getBranchId() {
        return branchId;
    }

    public void setBranchId(int branchId) {
        this.branchId = branchId;
    }

    public int getStockCount() {
        return stockCount;
    }

    public void setStockCount(int stockCount) {
        this.stockCount = stockCount;
    }
}
