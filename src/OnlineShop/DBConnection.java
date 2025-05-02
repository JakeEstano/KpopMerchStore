package OnlineShop;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/kpop_merch_store";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection connect() throws SQLException { // Changed signature to throw SQLException
        try {
            // Ensure MySQL Connector/J driver is loaded
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found. Include it in your library path.");
            e.printStackTrace();
            throw new SQLException("JDBC Driver not found", e); // Re-throw as SQLException
        }
        // Establish and return the connection
        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            // System.out.println("Database Connected Successfully!"); // Optional: Remove for cleaner logs
            return conn;
        } catch (SQLException e) {
             System.err.println("Database Connection Failed: " + e.getMessage());
             throw e; // Re-throw the SQLException to be handled by the caller
        }
    }
}