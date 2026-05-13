package com.hastane.service;

import com.hastane.model.Appointment;
import com.hastane.repository.AppointmentRepository;

import java.time.LocalDateTime;
import java.util.List;

public class AppointmentService {

    private final AppointmentRepository appointmentRepository = new AppointmentRepository();

    // 1. Yeni Randevu Oluşturma
    public void createAppointment(int doctorId, int patientId, LocalDateTime time) {
        if (time.isBefore(LocalDateTime.now())) {
            System.out.println("❌ Hata: Geçmiş bir tarihe randevu oluşturamazsınız!");
            return;
        }

        if (appointmentRepository.isDoctorBusy(doctorId, time)) {
            System.out.println("❌ Hata: Doktor bu saatte başka bir hasta ile görüşüyor!");
            return;
        }

        Appointment app = new Appointment(0, doctorId, patientId, time, "AKTIF");
        appointmentRepository.addAppointment(app);
        System.out.println("✅ Randevu başarıyla oluşturuldu: " + time);
    }

    // 2. Randevu İptal Etme
    public void cancelAppointment(int appointmentId) {
        Appointment existing = appointmentRepository.findById(appointmentId);

        if (existing == null) {
            System.out.println("❌ Hata: Randevu bulunamadı.");
            return;
        }
        if ("IPTAL".equals(existing.getStatus())) {
            System.out.println("⚠️ Bu randevu zaten iptal edilmiş.");
            return;
        }
        if ("TAMAMLANDI".equals(existing.getStatus())) {
            System.out.println("⚠️ Tamamlanmış randevu iptal edilemez.");
            return;
        }

        appointmentRepository.updateAppointmentStatus(appointmentId, "IPTAL");
        System.out.println("✅ Randevu iptal edildi.");
    }

    // 3. Muayeneyi Tamamlama (Transaction)
    public void finalizeAppointment(int appointmentId, String diagnosis, String notes) {
        if (diagnosis == null || diagnosis.trim().isEmpty()) {
            System.out.println("⚠️ Hata: Teşhis girmeden muayene tamamlanamaz!");
            return;
        }

        Appointment existing = appointmentRepository.findById(appointmentId);
        if (existing == null) {
            System.out.println("❌ Hata: Randevu bulunamadı.");
            return;
        }
        if (!"AKTIF".equals(existing.getStatus())) {
            System.out.println("⚠️ Sadece aktif randevular tamamlanabilir. Mevcut durum: " + existing.getStatus());
            return;
        }

        String finalNotes = (notes == null || notes.trim().isEmpty()) ? "Not belirtilmedi." : notes;
        appointmentRepository.completeAppointment(appointmentId, diagnosis, finalNotes);
    }

    // 4. Hastanın Randevularını Göster
    public void showAppointmentsByPatient(int patientId) {
        List<String> appointments = appointmentRepository.getAppointmentsByPatientId(patientId);
        if (appointments.isEmpty()) {
            System.out.println("ℹ️ Bu hastaya ait randevu bulunamadı.");
            return;
        }
        System.out.println("\n----- Hasta Randevuları -----");
        appointments.forEach(System.out::println);
    }
}