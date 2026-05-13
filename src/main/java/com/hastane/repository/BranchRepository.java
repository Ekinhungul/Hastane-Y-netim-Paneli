package com.hastane.repository;

import com.hastane.config.DatabaseConfig;
import com.hastane.model.Branch;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BranchRepository {

    public void addBranch(String name) {
        String sql = "INSERT INTO branches (name) VALUES (?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.executeUpdate();
            System.out.println("✅ Branş başarıyla eklendi: " + name);
        } catch (SQLException e) {
            System.out.println("Branş ekleme hatası: " + e.getMessage());
        }
    }

    public List<Branch> getAllBranches() {
        List<Branch> branches = new ArrayList<>();
        String sql = "SELECT * FROM branches";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Branch branch = new Branch();
                branch.setId(rs.getInt("id"));
                branch.setName(rs.getString("name"));
                branches.add(branch);
            }
        } catch (SQLException e) {
            System.out.println("Branş listeleme hatası: " + e.getMessage());
        }
        return branches;
    }
}