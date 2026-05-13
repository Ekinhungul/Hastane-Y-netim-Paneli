package com.hastane.model;

import java.time.LocalDateTime;

public class MedicalHistory {
    private int id;
    private int appointmentId;
    private String diagnosis;
    private String treatmentNotes;
    private LocalDateTime createdAt;

    public MedicalHistory() {}

    public MedicalHistory(int id, int appointmentId, String diagnosis,
                          String treatmentNotes, LocalDateTime createdAt) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.diagnosis = diagnosis;
        this.treatmentNotes = treatmentNotes;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getAppointmentId() { return appointmentId; }
    public void setAppointmentId(int appointmentId) { this.appointmentId = appointmentId; }
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    public String getTreatmentNotes() { return treatmentNotes; }
    public void setTreatmentNotes(String treatmentNotes) { this.treatmentNotes = treatmentNotes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}