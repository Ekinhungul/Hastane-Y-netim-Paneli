package com.hastane.service;

import com.hastane.model.Patient;
import com.hastane.repository.PatientRepository;

import java.util.List;

public class PatientService {
    private final PatientRepository patientRepository = new PatientRepository();

    // 1. Hasta Kayıt
    public void registerPatient(String tc, String firstName, String lastName, String phone) {
        String normalizedTc        = tc        == null ? "" : tc.trim();
        String normalizedFirstName = firstName == null ? "" : firstName.trim();
        String normalizedLastName  = lastName  == null ? "" : lastName.trim();
        String normalizedPhone     = phone     == null ? "" : phone.trim();

        if (!normalizedTc.matches("\\d{11}")) {
            System.out.println("❌ Hata: TC Kimlik numarası 11 haneli olmalıdır!");
            return;
        }
        if (normalizedFirstName.isEmpty() || normalizedLastName.isEmpty()) {
            System.out.println("❌ Hata: İsim ve soyisim alanları boş bırakılamaz!");
            return;
        }

        Patient existing = patientRepository.findByTcNo(normalizedTc);
        if (existing != null) {
            System.out.println("⚠️ Bu TC ile hasta zaten kayıtlı.");
            System.out.println("ID: " + existing.getId()
                    + " | Ad Soyad: " + existing.getFirstName() + " " + existing.getLastName()
                    + " | Telefon: " + existing.getPhone());
            return;
        }

        Patient newPatient = new Patient(0, normalizedTc, normalizedFirstName, normalizedLastName, normalizedPhone);
        boolean saved = patientRepository.addPatient(newPatient);
        if (saved) {
            System.out.println("✅ Hasta başarıyla kaydedildi: " + normalizedFirstName + " " + normalizedLastName);
        }
    }

    // 2. Tüm Hastaları Listele
    public void showAllPatients() {
        List<Patient> list = patientRepository.getAllPatients();
        if (list.isEmpty()) {
            System.out.println("ℹ️ Kayıtlı hasta bulunamadı.");
            return;
        }
        System.out.println("\n----- Kayıtlı Hastalar -----");
        for (Patient p : list) {
            System.out.println("ID: " + p.getId()
                    + " | TC: " + p.getTcNo()
                    + " | Ad Soyad: " + p.getFirstName() + " " + p.getLastName()
                    + " | Telefon: " + p.getPhone());
        }
    }

    // 3. TC ile Hasta Ara
    public void showPatientByTc(String tc) {
        String normalizedTc = tc == null ? "" : tc.trim();
        if (!normalizedTc.matches("\\d{11}")) {
            System.out.println("❌ Hata: TC Kimlik numarası 11 haneli olmalıdır!");
            return;
        }
        Patient patient = patientRepository.findByTcNo(normalizedTc);
        if (patient == null) {
            System.out.println("ℹ️ Bu TC ile hasta bulunamadı.");
            return;
        }
        System.out.println("ID: " + patient.getId()
                + " | TC: " + patient.getTcNo()
                + " | Ad Soyad: " + patient.getFirstName() + " " + patient.getLastName()
                + " | Telefon: " + patient.getPhone());
    }

    // 4. ID ile Hasta Getir (Randevu işlemlerinde kullanılır)
    public Patient findPatientById(int id) {
        List<Patient> list = patientRepository.getAllPatients();
        for (Patient p : list) {
            if (p.getId() == id) return p;
        }
        System.out.println("❌ Bu ID ile hasta bulunamadı.");
        return null;
    }
}