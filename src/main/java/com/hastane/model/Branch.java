package com.hastane.model;

public class Branch {
    private int id;
    private String name;

    public Branch() {}
    public Branch(int id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getter ve Setter metotlarını ekle (Sağ tık -> Generate -> Getter and Setter)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}