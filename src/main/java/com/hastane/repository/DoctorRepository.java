package com.hastane.repository;

import com.hastane.config.DatabaseConfig;
import com.hastane.model.DoctorBranchView;
import com.hastane.model.Doctor;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DoctorRepository {

    // 1. Yeni Doktor Ekleme (INSERT)
    public void addDoctor(String name, int branchId) {
        String sql = "INSERT INTO doctors (name, branch_id) VALUES (?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setInt(2, branchId);
            pstmt.executeUpdate();
            System.out.println("✅ Doktor başarıyla eklendi: " + name);

        } catch (SQLException e) {
            System.out.println("Doktor ekleme hatası: " + e.getMessage());
        }
    }

    public List<DoctorBranchView> listDoctorsWithBranches() {
        String sql = "SELECT d.id AS doctor_id, d.name AS doctor_name, b.id AS branch_id, b.name AS branch_name " +
                "FROM doctors d " +
                "JOIN branches b ON d.branch_id = b.id " +
                "ORDER BY d.id";
        return queryDoctorViewList(sql, null);
    }

    public List<DoctorBranchView> searchDoctorsByName(String keyword) {
        String sql = "SELECT d.id AS doctor_id, d.name AS doctor_name, b.id AS branch_id, b.name AS branch_name " +
                "FROM doctors d " +
                "JOIN branches b ON d.branch_id = b.id " +
                "WHERE LOWER(d.name) LIKE LOWER(?) " +
                "ORDER BY d.id";
        return queryDoctorViewList(sql, pstmt -> pstmt.setString(1, "%" + keyword + "%"));
    }

    public List<DoctorBranchView> findDoctorsByBranchId(int branchId) {
        String sql = "SELECT d.id AS doctor_id, d.name AS doctor_name, b.id AS branch_id, b.name AS branch_name " +
                "FROM doctors d " +
                "JOIN branches b ON d.branch_id = b.id " +
                "WHERE b.id = ? " +
                "ORDER BY d.id";
        return queryDoctorViewList(sql, pstmt -> pstmt.setInt(1, branchId));
    }

    private List<DoctorBranchView> queryDoctorViewList(String sql, SqlBinder binder) {
        List<DoctorBranchView> doctors = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (binder != null) {
                binder.bind(pstmt);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    doctors.add(new DoctorBranchView(
                            rs.getInt("doctor_id"),
                            rs.getString("doctor_name"),
                            rs.getInt("branch_id"),
                            rs.getString("branch_name")
                    ));
                }
            }
        } catch (SQLException e) {
            System.out.println("Doktor listeleme hatası: " + e.getMessage());
        }

        return doctors;
    }

    @FunctionalInterface
    private interface SqlBinder {
        void bind(PreparedStatement pstmt) throws SQLException;
    }
}