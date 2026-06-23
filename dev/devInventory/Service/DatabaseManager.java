package Service;

import java.sql.*;

/**
 * Manages the SQLite database connection and schema initialization
 * for the Super-Li Inventory Management System.
 *
 * <p>This class follows the Singleton pattern to ensure only one
 * database connection exists throughout the application lifecycle.</p>
 *
 * <p>Tables created:
 * <ul>
 *   <li>manufacturers</li>
 *   <li>categories</li>
 *   <li>products</li>
 *   <li>supplier_costs</li>
 *   <li>discounts</li>
 *   <li>product_discounts</li>
 *   <li>category_discounts</li>
 *   <li>defective_items</li>
 *   <li>low_stock_alerts</li>
 * </ul>
 * </p>
 */
public class DatabaseManager {

    /** Path to the SQLite database file (created in project root). */
    private static final String DB_URL = "jdbc:sqlite:superli_inventory.db";

    /** The single shared connection instance. */
    private static Connection connection = null;

    /**
     * Private constructor — use {@link #getConnection()} instead.
     */
    private DatabaseManager() {}

    /**
     * Returns the shared SQLite connection, creating it if necessary.
     * Also initializes the schema on first call.
     *
     * @return The active {@link Connection} to the database.
     * @throws SQLException if the connection or schema setup fails.
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
            connection.setAutoCommit(true);
            initializeSchema();
        }
        return connection;
    }

    /**
     * Creates all required tables if they do not already exist.
     * Safe to call multiple times (uses IF NOT EXISTS).
     *
     * @throws SQLException if any table creation fails.
     */
    private static void initializeSchema() throws SQLException {
        try (Statement stmt = connection.createStatement()) {

            // Manufacturers
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS manufacturers (
                    id   INTEGER PRIMARY KEY,
                    name TEXT NOT NULL
                )
            """);

            // Categories (self-referencing for hierarchy)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS categories (
                    id        INTEGER PRIMARY KEY,
                    name      TEXT NOT NULL,
                    parent_id INTEGER,
                    FOREIGN KEY (parent_id) REFERENCES categories(id)
                )
            """);

            // Products
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS products (
                    id               INTEGER PRIMARY KEY,
                    name             TEXT NOT NULL,
                    manufacturer_id  INTEGER NOT NULL,
                    min_stock        INTEGER NOT NULL DEFAULT 0,
                    storage_amount   INTEGER NOT NULL DEFAULT 0,
                    shelf_amount     INTEGER NOT NULL DEFAULT 0,
                    aisle            INTEGER NOT NULL DEFAULT 0,
                    shelf            INTEGER NOT NULL DEFAULT 0,
                    delivery_day     INTEGER NOT NULL DEFAULT 0,
                    target_quantity  INTEGER NOT NULL DEFAULT 0,
                    base_price       REAL    NOT NULL DEFAULT 0.0,
                    category_id      INTEGER,
                    sub_category_id  INTEGER,
                    sub_sub_category_id INTEGER,
                    FOREIGN KEY (manufacturer_id)      REFERENCES manufacturers(id),
                    FOREIGN KEY (category_id)          REFERENCES categories(id),
                    FOREIGN KEY (sub_category_id)      REFERENCES categories(id),
                    FOREIGN KEY (sub_sub_category_id)  REFERENCES categories(id)
                )
            """);

            // Supplier costs per product
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS supplier_costs (
                    id          INTEGER PRIMARY KEY AUTOINCREMENT,
                    product_id  INTEGER NOT NULL,
                    supplier_id INTEGER NOT NULL,
                    cost        REAL    NOT NULL,
                    FOREIGN KEY (product_id) REFERENCES products(id)
                )
            """);

            // Discounts (base table)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS discounts (
                    id               INTEGER PRIMARY KEY,
                    discount_percent REAL    NOT NULL,
                    start_date       TEXT    NOT NULL,
                    end_date         TEXT    NOT NULL,
                    type             TEXT    NOT NULL
                )
            """);

            // Product-level discount links
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS product_discounts (
                    discount_id INTEGER NOT NULL,
                    product_id  INTEGER NOT NULL,
                    PRIMARY KEY (discount_id, product_id),
                    FOREIGN KEY (discount_id) REFERENCES discounts(id),
                    FOREIGN KEY (product_id)  REFERENCES products(id)
                )
            """);

            // Category-level discount links
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS category_discounts (
                    discount_id INTEGER NOT NULL,
                    category_id INTEGER NOT NULL,
                    PRIMARY KEY (discount_id, category_id),
                    FOREIGN KEY (discount_id) REFERENCES discounts(id),
                    FOREIGN KEY (category_id) REFERENCES categories(id)
                )
            """);

            // Defective items log
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS defective_items (
                    id                 INTEGER PRIMARY KEY AUTOINCREMENT,
                    product_id         INTEGER NOT NULL,
                    defective_quantity INTEGER NOT NULL,
                    defective_location TEXT    NOT NULL,
                    reported_at        TEXT    NOT NULL DEFAULT (datetime('now')),
                    FOREIGN KEY (product_id) REFERENCES products(id)
                )
            """);

            // Low stock alert history
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS low_stock_alerts (
                    id            INTEGER PRIMARY KEY AUTOINCREMENT,
                    product_id    INTEGER NOT NULL,
                    product_name  TEXT    NOT NULL,
                    current_stock INTEGER NOT NULL,
                    min_stock     INTEGER NOT NULL,
                    generated_at  TEXT    NOT NULL DEFAULT (datetime('now')),
                    FOREIGN KEY (product_id) REFERENCES products(id)
                )
            """);

            System.out.println("[DB] Schema initialized successfully.");
        }
    }

    /**
     * Closes the database connection gracefully.
     * Should be called when the application exits.
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("[DB] Connection closed.");
            } catch (SQLException e) {
                System.err.println("[DB] Error closing connection: " + e.getMessage());
            }
        }
    }

    /**
     * Checks whether the given table already has rows.
     * Used by DataInitializer to avoid duplicate seeding.
     *
     * @param tableName The table to check.
     * @return true if the table contains at least one row.
     * @throws SQLException if the query fails.
     */
    public static boolean tableHasData(String tableName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + tableName;
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            return rs.next() && rs.getInt(1) > 0;
        }
    }
}