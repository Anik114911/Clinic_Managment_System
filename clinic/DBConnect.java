package clinic;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class DBConnect {
    private static final String DB_URL      = "jdbc:mysql://localhost:3306/clinic_db";
    private static final String DB_USER     = "root";
    private static final String DB_PASSWORD = "Your_password_here";

    private static Connection connection = null;
    private DBConnect() { }

    public static Connection getConnection() {

        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                System.out.println("✔ Database connected successfully.");
            }

        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(
                null,
                "MySQL JDBC Driver not found!\n"
                + "Please add mysql-connector-java.jar to your project Libraries.",
                "Driver Error",
                JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();

        } catch (SQLException e) {
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
    
    public static void main(String[] args) {
        java.sql.Connection conn = getConnection(); 
        if (conn != null) {
            System.out.println("✅ Database connection is 100% successful!");
        } else {
            System.out.println("❌ Database connection failed. Check XAMPP/MySQL or password.");
        }
    }
}
