package com.hastane.model;

public class Patient {
    private int id;
    private String tcNo;
    private String firstName;
    private String lastName;
    private String phone;

    public Patient() {}

    public Patient(int id, String tcNo, String firstName, String lastName, String phone) {
        this.id = id;
        this.tcNo = tcNo;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTcNo() { return tcNo; }
    public void setTcNo(String tcNo) { this.tcNo = tcNo; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}