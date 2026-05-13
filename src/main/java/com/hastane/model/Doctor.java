package com.hastane.model;

public class Doctor {
    private int id;
    private String name;
    private int branchId;

    public Doctor() {}
    public Doctor(int id, String name, int branchId) {
        this.id = id;
        this.name = name;
        this.branchId = branchId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getBranchId() { return branchId; }
    public void setBranchId(int branchId) { this.branchId = branchId; }
}