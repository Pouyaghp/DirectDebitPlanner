package com.pouya.directdebitplanner;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:directdebits.db";

    public static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            String sql = """
                    CREATE TABLE IF NOT EXISTS direct_debits (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL,
                        amount REAL,
                        due_date TEXT,
                        category TEXT,
                        notes TEXT
                    );
                    """;

            stmt.execute(sql);
            System.out.println("Database initialized successfully");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static DirectDebit insertDirectDebit(DirectDebit directDebit) {
        String sql = """
                INSERT INTO direct_debits(name, amount, due_date, category, notes)
                VALUES(?, ?, ?, ?, ?)
                """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, directDebit.getName());
            pstmt.setDouble(2, directDebit.getAmount());
            pstmt.setString(3, directDebit.getDueDate());
            pstmt.setString(4, directDebit.getCategory());
            pstmt.setString(5, directDebit.getNotes());

            pstmt.executeUpdate();

            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int id = generatedKeys.getInt(1);
                System.out.println("Direct debit saved successfully");
                return new DirectDebit(
                        id,
                        directDebit.getName(),
                        directDebit.getAmount(),
                        directDebit.getDueDate(),
                        directDebit.getCategory(),
                        directDebit.getNotes()
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return directDebit;
    }

    public static void updateDirectDebit(DirectDebit directDebit) {
        String sql = """
                UPDATE direct_debits
                SET name = ?, amount = ?, due_date = ?, category = ?, notes = ?
                WHERE id = ?
                """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, directDebit.getName());
            pstmt.setDouble(2, directDebit.getAmount());
            pstmt.setString(3, directDebit.getDueDate());
            pstmt.setString(4, directDebit.getCategory());
            pstmt.setString(5, directDebit.getNotes());
            pstmt.setInt(6, directDebit.getId());

            pstmt.executeUpdate();
            System.out.println("Direct debit updated successfully");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<DirectDebit> getAllDirectDebits() {
        List<DirectDebit> debits = new ArrayList<>();

        String sql = "SELECT id, name, amount, due_date, category, notes FROM direct_debits";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                DirectDebit debit = new DirectDebit(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("amount"),
                        rs.getString("due_date"),
                        rs.getString("category"),
                        rs.getString("notes")
                );
                debits.add(debit);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return debits;
    }

    public static void deleteDirectDebit(int id) {
        String sql = "DELETE FROM direct_debits WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();

            System.out.println("Direct debit deleted successfully");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}