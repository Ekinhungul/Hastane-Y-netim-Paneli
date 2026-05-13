package com.hastane.model;

import java.time.LocalDateTime;

public class Appointment {
    private int id;
    private int doctorId;
    private int patientId;
    private LocalDateTime appointmentTime;
    private String status;

    public Appointment() {}

    public Appointment(int id, int doctorId, int patientId, LocalDateTime appointmentTime, String status) {
        this.id = id;
        this.doctorId = doctorId;
        this.patientId = patientId;
        this.appointmentTime = appointmentTime;
        this.status = status;
    }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }
    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }
    public LocalDateTime getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(LocalDateTime appointmentTime) { this.appointmentTime = appointmentTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}