package com.hastane.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {
    // Veritabanı adresi (hastane_db oluşturduğumuz isim)
    private static final String URL = "jdbc:postgresql://localhost:5432/hastane_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "130630"; // <--- Burayı değiştir!

    public static Connection getConnection() {
        try {
            // PostgreSQL sürücüsünü kullanarak bağlantı açıyoruz
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.err.println("Bağlantı hatası: " + e.getMessage());
            return null;
        }
    }
}