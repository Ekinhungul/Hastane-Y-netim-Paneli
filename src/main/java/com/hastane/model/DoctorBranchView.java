package com.hastane.model;

public class DoctorBranchView {
    private int doctorId;
    private String doctorName;
    private int branchId;
    private String branchName;

    public DoctorBranchView(int doctorId, String doctorName, int branchId, String branchName) {
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.branchId = branchId;
        this.branchName = branchName;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public int getBranchId() {
        return branchId;
    }

    public String getBranchName() {
        return branchName;
    }
}
