package com.hastane.repository;

import com.hastane.config.DatabaseConfig;
import com.hastane.model.Report;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReportRepository {

    public void addReport(Report report) {
        String sql = "INSERT INTO reports (patient_id, original_filename, stored_filename, file_path, mime_type, file_size, uploaded_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection()) {
            if (conn == null) {
                System.out.println("❌ Rapor kayıt hatası: Veritabanı bağlantısı kurulamadı.");
                return;
            }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, report.getPatientId());
                pstmt.setString(2, report.getOriginalFilename());
                pstmt.setString(3, report.getStoredFilename());
                pstmt.setString(4, report.getFilePath());
                pstmt.setString(5, report.getMimeType());
                pstmt.setLong(6, report.getFileSize());
                pstmt.setTimestamp(7, Timestamp.valueOf(report.getUploadedAt()));
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println("❌ Rapor kayıt hatası: " + e.getMessage());
        }
    }

    public List<Report> getReportsByPatientId(int patientId) {
        List<Report> reports = new ArrayList<>();
        String sql = "SELECT * FROM reports WHERE patient_id = ? ORDER BY uploaded_at DESC";

        try (Connection conn = DatabaseConfig.getConnection()) {
            if (conn == null) {
                System.out.println("❌ Rapor listeleme hatası: Veritabanı bağlantısı kurulamadı.");
                return reports;
            }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, patientId);
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    reports.add(mapRowToReport(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Rapor listeleme hatası: " + e.getMessage());
        }
        return reports;
    }

    public Report getReportById(int reportId) {
        String sql = "SELECT * FROM reports WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection()) {
            if (conn == null) {
                System.out.println("❌ Rapor getirme hatası: Veritabanı bağlantısı kurulamadı.");
                return null;
            }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, reportId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return mapRowToReport(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Rapor getirme hatası: " + e.getMessage());
        }

        return null;
    }

    private Report mapRowToReport(ResultSet rs) throws SQLException {
        Timestamp uploadedAtTs = rs.getTimestamp("uploaded_at");
        LocalDateTime uploadedAt = uploadedAtTs != null ? uploadedAtTs.toLocalDateTime() : null;

        return new Report(
                rs.getInt("id"),
                rs.getInt("patient_id"),
                rs.getString("original_filename"),
                rs.getString("stored_filename"),
                rs.getString("file_path"),
                rs.getString("mime_type"),
                rs.getLong("file_size"),
                uploadedAt
        );
    }
}
