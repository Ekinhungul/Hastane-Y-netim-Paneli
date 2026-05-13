package com.hastane.service;

import com.hastane.model.Report;
import com.hastane.repository.PatientRepository;
import com.hastane.repository.ReportRepository;

import java.io.IOException;
import java.awt.Desktop;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class ReportService {
    private static final Path UPLOADS_DIR = Paths.get("uploads");
    private final ReportRepository reportRepository = new ReportRepository();
    private final PatientRepository patientRepository = new PatientRepository();

    public void uploadReport(int patientId, String sourceFilePath) {
        if (patientId <= 0) {
            System.out.println("❌ Hata: Geçerli bir hasta ID giriniz.");
            return;
        }

        if (!patientRepository.existsById(patientId)) {
            System.out.println("❌ Hata: Bu ID ile kayıtlı hasta bulunamadı.");
            return;
        }

        if (sourceFilePath == null || sourceFilePath.trim().isEmpty()) {
            System.out.println("❌ Hata: Dosya yolu boş olamaz.");
            return;
        }

        String sanitizedPath = sourceFilePath.trim().replace("\"", "");
        Path sourcePath;
        try {
            sourcePath = Paths.get(sanitizedPath);
        } catch (InvalidPathException e) {
            System.out.println("❌ Hata: Dosya yolu formatı geçersiz.");
            return;
        }

        if (!Files.exists(sourcePath) || !Files.isRegularFile(sourcePath)) {
            System.out.println("❌ Hata: Belirtilen dosya bulunamadı. Yol: " + sourcePath.toAbsolutePath());
            return;
        }

        try {
            Files.createDirectories(UPLOADS_DIR);

            String originalFilename = sourcePath.getFileName().toString();
            String extension = getExtension(originalFilename);
            String storedFilename = UUID.randomUUID() + extension;
            Path targetPath = UPLOADS_DIR.resolve(storedFilename);

            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

            String mimeType = Files.probeContentType(sourcePath);
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }

            long fileSize = Files.size(targetPath);
            Report report = new Report(
                    0,
                    patientId,
                    originalFilename,
                    storedFilename,
                    targetPath.toAbsolutePath().toString(),
                    mimeType,
                    fileSize,
                    LocalDateTime.now()
            );

            reportRepository.addReport(report);
            System.out.println("✅ Rapor başarıyla yüklendi: " + originalFilename);
        } catch (IOException e) {
            System.out.println("❌ Dosya yükleme hatası: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ Beklenmeyen hata: " + e.getMessage());
        }
    }

    public void showReportsByPatient(int patientId) {
        List<Report> reports = reportRepository.getReportsByPatientId(patientId);
        if (reports.isEmpty()) {
            System.out.println("ℹ️ Bu hastaya ait rapor bulunamadı.");
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        System.out.println("\n----- Hasta Raporları -----");
        for (Report report : reports) {
            String uploadedAt = report.getUploadedAt() == null ? "-" : report.getUploadedAt().format(formatter);
            System.out.println(
                    "Rapor ID: " + report.getId()
                            + " | Dosya: " + report.getOriginalFilename()
                            + " | Tip: " + report.getMimeType()
                            + " | Boyut: " + report.getFileSize() + " byte"
                            + " | Yüklenme: " + uploadedAt
            );
        }
    }

    public void showReportPath(int reportId) {
        Report report = reportRepository.getReportById(reportId);
        if (report == null) {
            System.out.println("❌ Hata: Rapor bulunamadı.");
            return;
        }
        Path reportPath = resolveReportPath(report);
        if (reportPath == null) {
            System.out.println("❌ Hata: Kayıtlı dosya yolu yok ve dosya adı da bulunamadı.");
            return;
        }
        System.out.println("📎 Rapor yolu: " + reportPath.toAbsolutePath());
    }

    public void openReport(int reportId) {
        Report report = reportRepository.getReportById(reportId);
        if (report == null) {
            System.out.println("❌ Hata: Rapor bulunamadı.");
            return;
        }

        Path reportPath = resolveReportPath(report);
        if (reportPath == null) {
            System.out.println("❌ Hata: Raporun dosya yolu bilgisi eksik.");
            return;
        }
        if (!Files.exists(reportPath)) {
            System.out.println("❌ Hata: Rapor dosyası diskte bulunamadı.");
            return;
        }

        if (!Desktop.isDesktopSupported()) {
            System.out.println("⚠️ Bu sistemde dosya otomatik açma desteklenmiyor.");
            return;
        }

        try {
            Desktop.getDesktop().open(reportPath.toFile());
            System.out.println("✅ Rapor açılıyor: " + report.getOriginalFilename());
        } catch (IOException e) {
            System.out.println("❌ Rapor açma hatası: " + e.getMessage());
        }
    }

    public void exportReportToDirectory(int reportId, String targetDir) {
        Report report = reportRepository.getReportById(reportId);
        if (report == null) {
            System.out.println("❌ Hata: Rapor bulunamadı.");
            return;
        }

        if (targetDir == null || targetDir.trim().isEmpty()) {
            System.out.println("❌ Hata: Hedef klasör yolu boş olamaz.");
            return;
        }

        Path sourcePath = resolveReportPath(report);
        if (sourcePath == null) {
            System.out.println("❌ Hata: Raporun dosya yolu bilgisi eksik.");
            return;
        }
        if (!Files.exists(sourcePath)) {
            System.out.println("❌ Hata: Kaynak rapor dosyası bulunamadı.");
            return;
        }

        try {
            Path destinationDir = Paths.get(targetDir.trim().replace("\"", ""));
            Files.createDirectories(destinationDir);

            Path destination = destinationDir.resolve(report.getOriginalFilename());
            Files.copy(sourcePath, destination, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("✅ Rapor dışa aktarıldı: " + destination.toAbsolutePath());
        } catch (IOException e) {
            System.out.println("❌ Rapor dışa aktarma hatası: " + e.getMessage());
        }
    }

    private String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex == -1 || dotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(dotIndex);
    }

    private Path resolveReportPath(Report report) {
        if (report.getFilePath() != null && !report.getFilePath().trim().isEmpty()) {
            try {
                return Paths.get(report.getFilePath().trim());
            } catch (InvalidPathException ignored) {
                // fallback below
            }
        }

        if (report.getStoredFilename() != null && !report.getStoredFilename().trim().isEmpty()) {
            return UPLOADS_DIR.resolve(report.getStoredFilename().trim());
        }

        return null;
    }
}
