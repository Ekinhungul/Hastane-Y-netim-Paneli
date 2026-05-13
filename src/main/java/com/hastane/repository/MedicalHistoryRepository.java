package com.hastane.repository;

import com.hastane.config.DatabaseConfig;
import com.hastane.model.MedicalHistory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedicalHistoryRepository {

    public List<MedicalHistory> getHistoryByPatientId(int patientId) {
        List<MedicalHistory> list = new ArrayList<>();

        String sql = """
                SELECT mh.id, mh.appointment_id, mh.diagnosis, mh.treatment_notes, mh.created_at
                FROM medical_histories mh
                JOIN appointments a ON mh.appointment_id = a.id
                WHERE a.patient_id = ?
                ORDER BY mh.created_at DESC
                """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, patientId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("created_at");
                MedicalHistory mh = new MedicalHistory(
                        rs.getInt("id"),
                        rs.getInt("appointment_id"),
                        rs.getString("diagnosis"),
                        rs.getString("treatment_notes"),
                        ts != null ? ts.toLocalDateTime() : null
                );
                list.add(mh);
            }

        } catch (SQLException e) {
            System.out.println("❌ Muayene geçmişi hatası: " + e.getMessage());
        }
        return list;
    }
}