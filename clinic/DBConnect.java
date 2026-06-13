/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package clinic;


// ============================================================
//  DBConnect.java
//  Purpose : Manages the MySQL database connection.
//  Pattern : Singleton — only ONE connection is created and
//            reused throughout the entire application.
//  Location: src/clinic/DBConnect.java
// ============================================================

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class DBConnect {

    // ── Database configuration ─────────────────────────────
    // Change these values to match your MySQL setup.
    private static final String DB_URL      = "jdbc:mysql://localhost:3306/clinic_db";
    private static final String DB_USER     = "root";       // your MySQL username
    private static final String DB_PASSWORD = "password";           // your MySQL password

    // The single shared connection object (Singleton pattern)
    private static Connection connection = null;

    // ── Private constructor ────────────────────────────────
    // Prevents anyone from doing: new DBConnect()
    private DBConnect() { }

    // ── getConnection() ────────────────────────────────────
    // Returns the active connection, or creates a new one
    // if none exists yet.
    public static Connection getConnection() {

        try {
            // Check if a connection already exists and is still open
            if (connection == null || connection.isClosed()) {

                // Step 1: Load the MySQL JDBC driver class into memory.
                // This is required so Java knows how to talk to MySQL.
                Class.forName("com.mysql.cj.jdbc.Driver");

                // Step 2: Ask DriverManager to give us a connection
                // using the URL, username, and password defined above.
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

                System.out.println("✔ Database connected successfully.");
            }

        } catch (ClassNotFoundException e) {
            // This error means the MySQL JAR file is missing from Libraries
            JOptionPane.showMessageDialog(
                null,
                "MySQL JDBC Driver not found!\n"
                + "Please add mysql-connector-java.jar to your project Libraries.",
                "Driver Error",
                JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();

        } catch (SQLException e) {
            // This error means MySQL is not running, or credentials are wrong
            JOptionPane.showMessageDialog(
                null,
                "Cannot connect to the database!\n"
                + "Please check:\n"
                + "  1. MySQL server is running.\n"
                + "  2. Username and password in DBConnect.java are correct.\n"
                + "  3. The 'clinic_db' database has been created.\n\n"
                + "Error: " + e.getMessage(),
                "Connection Error",
                JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }

        return connection;
    }

    // ── closeConnection() ──────────────────────────────────
    // Call this only when the application is shutting down.
    // You do NOT need to call this after every query.
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("✔ Database connection closed.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    // Temporary main method to test the database connection
    public static void main(String[] args) {
        java.sql.Connection conn = getConnection(); 
        if (conn != null) {
            System.out.println("✅ Database connection is 100% successful!");
        } else {
            System.out.println("❌ Database connection failed. Check XAMPP/MySQL or password.");
        }
    }
}
