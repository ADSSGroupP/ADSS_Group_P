package DataAccessLayer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    private static final String DB_URL = "jdbc:sqlite:superlee.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void createTables() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {

            // 1. Employees Table
            stmt.execute("CREATE TABLE IF NOT EXISTS employees (" +
                    "id INTEGER PRIMARY KEY, " +
                    "name TEXT, " +
                    "bank_num INTEGER, " +
                    "branch_num INTEGER, " +
                    "account_num INTEGER, " +
                    "is_shift_manager INTEGER, " +
                    "day_off TEXT, " +
                    "branch_id INTEGER, " +
                    "start_date TEXT, " +
                    "job_scope TEXT, " +
                    "global_wage REAL, " +
                    "hourly_wage REAL, " +
                    "is_driver INTEGER, " +
                    "license TEXT, " +
                    "roles TEXT)");

            // 2. Constraints Table
            stmt.execute("CREATE TABLE IF NOT EXISTS constraints (" +
                    "employee_id INTEGER, " +
                    "date TEXT, " +
                    "start_time TEXT, " +
                    "end_time TEXT, " +
                    "double_shift INTEGER, " +
                    "extra_hours INTEGER, " +
                    "is_changeable INTEGER, " +
                    "PRIMARY KEY (employee_id, date))");

            // 3. Shifts Table
            stmt.execute("CREATE TABLE IF NOT EXISTS shifts (" +
                    "date TEXT, " +
                    "type TEXT, " +
                    "manager_id INTEGER, " +
                    "branch_id INTEGER, " +
                    "PRIMARY KEY (date, type))");

            // 4. Shift Requirements Table (To store requirements per shift)
            stmt.execute("CREATE TABLE IF NOT EXISTS shift_requirements (" +
                    "shift_date TEXT, " +
                    "shift_type TEXT, " +
                    "role TEXT, " +
                    "amount INTEGER, " +
                    "PRIMARY KEY (shift_date, shift_type, role))");

            // 5. Shift Assignments Table (To store employee role assignments)
            stmt.execute("CREATE TABLE IF NOT EXISTS shift_assignments (" +
                    "shift_date TEXT, " +
                    "shift_type TEXT, " +
                    "employee_id INTEGER, " +
                    "role TEXT, " +
                    "extra_hours INTEGER, " +
                    "PRIMARY KEY (shift_date, shift_type, employee_id))");

        } catch (SQLException e) {
            System.err.println("Error initializing database tables: " + e.getMessage());
        }
    }
}