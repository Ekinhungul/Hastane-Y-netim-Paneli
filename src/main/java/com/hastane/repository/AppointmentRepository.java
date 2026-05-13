package com.hastane.repository;

import com.hastane.config.DatabaseConfig;
import com.hastane.model.Appointment;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AppointmentRepository {

    // 1. Doktor Doluluk Kontrolü
    public boolean isDoctorBusy(int doctorId, LocalDateTime time) {
        String sql = "SELECT COUNT(*) FROM appointments WHERE doctor_id = ? AND appointment_time = ? AND status != 'IPTAL'";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, doctorId);
            pstmt.setTimestamp(2, Timestamp.valueOf(time));

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("❌ Çakışma kontrolü hatası: " + e.getMessage());
        }
        return false;
    }

    // 2. Yeni Randevu Ekleme
    public void addAppointment(Appointment app) {
        String sql = "INSERT INTO appointments (doctor_id, patient_id, appointment_time, status) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, app.getDoctorId());
            pstmt.setInt(2, app.getPatientId());
            pstmt.setTimestamp(3, Timestamp.valueOf(app.getAppointmentTime()));
            pstmt.setString(4, app.getStatus());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("❌ Randevu kayıt hatası: " + e.getMessage());
        }
    }

    // 3. Basit Durum Güncelleme (İptal vb.)
    public void updateAppointmentStatus(int appointmentId, String newStatus) {
        String sql = "UPDATE appointments SET status = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newStatus);
            pstmt.setInt(2, appointmentId);
            pstmt.executeUpdate();
            System.out.println("✅ Randevu durumu '" + newStatus + "' olarak güncellendi.");

        } catch (SQLException e) {
            System.out.println("❌ Güncelleme hatası: " + e.getMessage());
        }
    }

    // 4. TRANSACTION: Randevu Kapatma ve Geçmişe Yazma
    public void completeAppointment(int appointmentId, String diagnosis, String notes) {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            if (conn == null) return;

            conn.setAutoCommit(false); // 🚩 Transaction Başladı

            // A. Randevuyu Tamamla
            String sqlUpdate = "UPDATE appointments SET status = 'TAMAMLANDI' WHERE id = ?";
            try (PreparedStatement pstmt1 = conn.prepareStatement(sqlUpdate)) {
                pstmt1.setInt(1, appointmentId);
                pstmt1.executeUpdate();
            }

            // B. Geçmişe Kayıt At
            String sqlInsert = "INSERT INTO medical_histories (appointment_id, diagnosis, treatment_notes) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt2 = conn.prepareStatement(sqlInsert)) {
                pstmt2.setInt(1, appointmentId);
                pstmt2.setString(2, diagnosis);
                pstmt2.setString(3, notes);
                pstmt2.executeUpdate();
            }

            conn.commit(); // 🏁 Her şey yolunda, verileri işle!
            System.out.println("✅ İşlem başarılı: Randevu tamamlandı ve teşhis kaydedildi.");

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // ↩️ Hata var, her şeyi geri al!
                    System.out.println("⚠️ Hata oluştu, veritabanı eski haline döndürüldü.");
                } catch (SQLException ex) { ex.printStackTrace(); }
            }
            System.out.println("❌ İşlem başarısız: " + e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // 5. Hastanın Tüm Randevularını Listele (JOIN ile doktor adı dahil)
    public List<String> getAppointmentsByPatientId(int patientId) {
        List<String> result = new ArrayList<>();

        String sql = """
                SELECT a.id, d.name AS doctor_name, a.appointment_time, a.status
                FROM appointments a
                JOIN doctors d ON a.doctor_id = d.id
                WHERE a.patient_id = ?
                ORDER BY a.appointment_time DESC
                """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, patientId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String line = String.format(
                        "ID: %-4d | Doktor: %-20s | Tarih: %-20s | Durum: %s",
                        rs.getInt("id"),
                        rs.getString("doctor_name"),
                        rs.getTimestamp("appointment_time").toLocalDateTime(),
                        rs.getString("status")
                );
                result.add(line);
            }

        } catch (SQLException e) {
            System.out.println("❌ Randevu listeleme hatası: " + e.getMessage());
        }
        return result;
    }

    // 6. Randevu ID ile Tek Randevu Getir
    public Appointment findById(int appointmentId) {
        String sql = "SELECT * FROM appointments WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, appointmentId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Appointment(
                        rs.getInt("id"),
                        rs.getInt("doctor_id"),
                        rs.getInt("patient_id"),
                        rs.getTimestamp("appointment_time").toLocalDateTime(),
                        rs.getString("status")
                );
            }

        } catch (SQLException e) {
            System.out.println("❌ Randevu getirme hatası: " + e.getMessage());
        }
        return null;
    }
}