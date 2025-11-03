package database;

import java.sql.*;

public class DBConnection {
    private static final String url = "jdbc:mysql://localhost:3306/WorldLeadersTracker";
    private static final String user = "root";
    private static final String pass = "joshua";

    public static Connection getConnection() {
        Connection con = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(url, user, pass);
        } catch (ClassNotFoundException e) {
            System.out.println("JDBC Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
        }
        return con;
    }
}
