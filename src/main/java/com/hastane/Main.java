package com.hastane;

import com.hastane.model.Patient;
import com.hastane.service.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class Main {

    static Scanner scanner = new Scanner(System.in);
    static DoctorService doctorService         = new DoctorService();
    static PatientService patientService       = new PatientService();
    static AppointmentService appointmentService = new AppointmentService();
    static ReportService reportService         = new ReportService();

    public static void main(String[] args) {
        System.out.println("🏥 Hastane Yönetim Sistemine Hoşgeldiniz!");

        while (true) {
            printMenu();
            int choice = readInt("Seçiminiz: ");

            switch (choice) {
                case 1  -> handleRegisterPatient();
                case 2  -> doctorService.showAllDoctors();
                case 3  -> handleCreateAppointment();
                case 4  -> handleCancelAppointment();
                case 5  -> handleFinalizeAppointment();
                case 6  -> patientService.showAllPatients();
                case 7  -> handleUploadReport();
                case 8  -> handleShowReports();
                case 9  -> handleShowReportPath();
                case 10 -> doctorService.showAllBranches();
                case 11 -> handleSearchDoctorByName();
                case 12 -> handleFilterDoctorByBranch();
                case 13 -> handleRegisterDoctor();
                case 14 -> handleRegisterBranch();
                case 15 -> handleSearchPatientByTc();
                case 16 -> handleShowAppointmentsByPatient();
                case 17 -> handleOpenReport();
                case 18 -> handleExportReport();
                case 0  -> {
                    System.out.println("👋 Çıkış yapılıyor...");
                    System.exit(0);
                }
                default -> System.out.println("⚠️ Geçersiz seçim, tekrar deneyin.");
            }
        }
    }

    // ──────────────────────────────────────────
    //  MENÜ
    // ──────────────────────────────────────────
    private static void printMenu() {
        System.out.println("\n==========================================");
        System.out.println("               ANA MENÜ");
        System.out.println("==========================================");
        System.out.println("--- HASTA İŞLEMLERİ ---");
        System.out.println(" 1  - Yeni Hasta Kaydet");
        System.out.println(" 6  - Tüm Hastaları Listele");
        System.out.println(" 15 - TC ile Hasta Ara");
        System.out.println("--- RANDEVU İŞLEMLERİ ---");
        System.out.println(" 3  - Randevu Al");
        System.out.println(" 4  - Randevu İptal Et");
        System.out.println(" 5  - Muayeneyi Tamamla (Teşhis Gir)");
        System.out.println(" 16 - Hastanın Randevularını Göster");
        System.out.println("--- DOKTOR / BRANŞ İŞLEMLERİ ---");
        System.out.println(" 2  - Tüm Doktorları Listele");
        System.out.println(" 10 - Branşları Listele");
        System.out.println(" 11 - Doktor Ara (İsme Göre)");
        System.out.println(" 12 - Doktorları Branşa Göre Filtrele");
        System.out.println(" 13 - Yeni Doktor Ekle");
        System.out.println(" 14 - Yeni Branş Ekle");
        System.out.println("--- RAPOR İŞLEMLERİ ---");
        System.out.println(" 7  - Hastaya Rapor Yükle");
        System.out.println(" 8  - Hastanın Raporlarını Listele");
        System.out.println(" 9  - Rapor Dosya Yolunu Göster");
        System.out.println(" 17 - Raporu Aç");
        System.out.println(" 18 - Raporu Dışa Aktar");
        System.out.println("------------------------------------------");
        System.out.println(" 0  - Çıkış");
        System.out.println("==========================================");
    }

    // ──────────────────────────────────────────
    //  HASTA
    // ──────────────────────────────────────────
    private static void handleRegisterPatient() {
        System.out.println("\n--- Yeni Hasta Kaydı ---");
        System.out.print("TC Kimlik No     : "); String tc        = scanner.nextLine().trim();
        System.out.print("Ad               : "); String firstName = scanner.nextLine().trim();
        System.out.print("Soyad            : "); String lastName  = scanner.nextLine().trim();
        System.out.print("Telefon (opsiyonel): "); String phone   = scanner.nextLine().trim();
        patientService.registerPatient(tc, firstName, lastName, phone);
    }

    private static void handleSearchPatientByTc() {
        System.out.println("\n--- TC ile Hasta Ara ---");
        System.out.print("TC Kimlik No: ");
        String tc = scanner.nextLine().trim();
        patientService.showPatientByTc(tc);
    }

    // ──────────────────────────────────────────
    //  RANDEVU
    // ──────────────────────────────────────────
    private static void handleCreateAppointment() {
        System.out.println("\n--- Yeni Randevu ---");

        doctorService.showAllDoctors();
        int doctorId = readInt("Doktor ID: ");

        patientService.showAllPatients();
        int patientId = readInt("Hasta ID: ");

        System.out.print("Tarih ve Saat (yyyy-MM-dd HH:mm): ");
        String dateStr = scanner.nextLine().trim();

        LocalDateTime dateTime = parseDateTime(dateStr);
        if (dateTime == null) return;

        appointmentService.createAppointment(doctorId, patientId, dateTime);
    }

    private static void handleCancelAppointment() {
        System.out.println("\n--- Randevu İptal ---");
        int id = readInt("İptal edilecek Randevu ID: ");
        appointmentService.cancelAppointment(id);
    }

    private static void handleFinalizeAppointment() {
        System.out.println("\n--- Muayene Tamamla ---");
        int id = readInt("Randevu ID: ");
        System.out.print("Teşhis      : "); String diagnosis = scanner.nextLine().trim();
        System.out.print("Notlar      : "); String notes     = scanner.nextLine().trim();
        appointmentService.finalizeAppointment(id, diagnosis, notes);
    }

    private static void handleShowAppointmentsByPatient() {
        System.out.println("\n--- Hasta Randevuları ---");
        int patientId = readInt("Hasta ID: ");
        appointmentService.showAppointmentsByPatient(patientId);
    }

    // ──────────────────────────────────────────
    //  DOKTOR / BRANŞ
    // ──────────────────────────────────────────
    private static void handleRegisterDoctor() {
        System.out.println("\n--- Yeni Doktor Ekle ---");
        doctorService.showAllBranches();
        System.out.print("Doktor Adı : "); String name     = scanner.nextLine().trim();
        int branchId = readInt("Branş ID   : ");
        doctorService.registerNewDoctor(name, branchId);
    }

    private static void handleRegisterBranch() {
        System.out.println("\n--- Yeni Branş Ekle ---");
        System.out.print("Branş Adı: ");
        String name = scanner.nextLine().trim();
        doctorService.registerNewBranch(name);
    }

    private static void handleSearchDoctorByName() {
        System.out.println("\n--- Doktor Ara ---");
        System.out.print("Aranacak isim: ");
        String keyword = scanner.nextLine().trim();
        doctorService.searchDoctorsByName(keyword);
    }

    private static void handleFilterDoctorByBranch() {
        System.out.println("\n--- Branşa Göre Doktor Filtrele ---");
        doctorService.showAllBranches();
        int branchId = readInt("Branş ID: ");
        doctorService.showDoctorsByBranch(branchId);
    }

    // ──────────────────────────────────────────
    //  RAPOR
    // ──────────────────────────────────────────
    private static void handleUploadReport() {
        System.out.println("\n--- Rapor Yükle ---");
        int patientId = readInt("Hasta ID       : ");
        System.out.print("Dosya Yolu     : ");
        String path = scanner.nextLine().trim();
        reportService.uploadReport(patientId, path);
    }

    private static void handleShowReports() {
        System.out.println("\n--- Hasta Raporları ---");
        int patientId = readInt("Hasta ID: ");
        reportService.showReportsByPatient(patientId);
    }

    private static void handleShowReportPath() {
        System.out.println("\n--- Rapor Yolu ---");
        int reportId = readInt("Rapor ID: ");
        reportService.showReportPath(reportId);
    }

    private static void handleOpenReport() {
        System.out.println("\n--- Raporu Aç ---");
        int reportId = readInt("Rapor ID: ");
        reportService.openReport(reportId);
    }

    private static void handleExportReport() {
        System.out.println("\n--- Raporu Dışa Aktar ---");
        int reportId = readInt("Rapor ID           : ");
        System.out.print("Hedef Klasör Yolu  : ");
        String targetDir = scanner.nextLine().trim();
        reportService.exportReportToDirectory(reportId, targetDir);
    }

    // ──────────────────────────────────────────
    //  YARDIMCI METODLAR
    // ──────────────────────────────────────────
    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Lütfen geçerli bir sayı girin.");
            }
        }
    }

    private static LocalDateTime parseDateTime(String dateStr) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            return LocalDateTime.parse(dateStr, formatter);
        } catch (DateTimeParseException e) {
            System.out.println("❌ Hata: Tarih formatı yanlış. Örnek: 2025-06-15 14:30");
            return null;
        }
    }
}