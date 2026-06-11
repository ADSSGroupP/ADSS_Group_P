package DataAccessLayer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    private static final String CONNECTION_URL = "jdbc:sqlite:superlee.db";
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(CONNECTION_URL);
    }
    
}