package DataAccessLayer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
/**
 * Database connection manager for the application.
 * Handles the creation of SQLite connections using a centralized connection URL
 * to ensure consistent data persistence access across all DAO implementations.
 */
public class Database {
    // Centralized database URL pointing to the local SQLite file using a relative path
    private static final String CONNECTION_URL = "jdbc:sqlite:superlee.db";

    // Establishes and returns a live connection session to the SQLite database file
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(CONNECTION_URL);
    }
}