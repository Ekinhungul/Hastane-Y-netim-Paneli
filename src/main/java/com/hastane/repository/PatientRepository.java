package com.hastane.repository;

import com.hastane.config.DatabaseConfig;
import com.hastane.model.Patient;
import org.postgresql.util.PSQLException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PatientRepository {

    public boolean addPatient(Patient patient) {
        String sql = "INSERT INTO patients (tc_no, first_name, last_name, phone) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection()) {
            if (conn == null) {
                System.out.println("❌ Hasta kayıt hatası: Veritabanı bağlantısı kurulamadı.");
                return false;
            }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, patient.getTcNo());
                pstmt.setString(2, patient.getFirstName());
                pstmt.setString(3, patient.getLastName());
                pstmt.setString(4, patient.getPhone());
                pstmt.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                String constraintName = getConstraintName(e);
                String errorMessage = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
                if ("uk_patients_tc_no".equalsIgnoreCase(constraintName)
                        || "patients_tc_no_key".equalsIgnoreCase(constraintName)
                        || errorMessage.contains("tc_no")) {
                    System.out.println("❌ Hata! TC No benzersiz olmalıdır. Bu TC ile kayıtlı hasta zaten var.");
                } else if ("patients_phone_key".equalsIgnoreCase(constraintName) || errorMessage.contains("phone")) {
                    System.out.println("❌ Hata! Telefon numarası zaten kayıtlı görünüyor.");
                } else {
                    System.out.println("❌ Hata! Benzersiz alan çakışması. Kısıt: " +
                            (constraintName.isBlank() ? "bilinmiyor" : constraintName));
                }
            } else {
                System.out.println("Hasta kayıt hatası: " + e.getMessage());
            }
            return false;
        }
    }

    public Patient findByTcNo(String tcNo) {
        String sql = "SELECT * FROM patients WHERE tc_no = ?";
        try (Connection conn = DatabaseConfig.getConnection()) {
            if (conn == null) {
                return null;
            }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, tcNo);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return new Patient(
                                rs.getInt("id"),
                                rs.getString("tc_no"),
                                rs.getString("first_name"),
                                rs.getString("last_name"),
                                rs.getString("phone")
                        );
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("TC ile hasta arama hatası: " + e.getMessage());
        }
        return null;
    }

    public List<Patient> getAllPatients() {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT * FROM patients";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Patient p = new Patient(
                        rs.getInt("id"),
                        rs.getString("tc_no"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("phone")
                );
                patients.add(p);
            }
        } catch (SQLException e) {
            System.out.println("Hasta listeleme hatası: " + e.getMessage());
        }
        return patients;
    }

    public boolean existsById(int patientId) {
        String sql = "SELECT 1 FROM patients WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection()) {
            if (conn == null) {
                System.out.println("❌ Hata: Veritabanına bağlanılamadı.");
                return false;
            }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, patientId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            System.out.println("Hasta kontrol hatası: " + e.getMessage());
            return false;
        }
    }

    private String getConstraintName(SQLException e) {
        if (e instanceof PSQLException pe && pe.getServerErrorMessage() != null) {
            String constraint = pe.getServerErrorMessage().getConstraint();
            return constraint == null ? "" : constraint;
        }
        return "";
    }
}