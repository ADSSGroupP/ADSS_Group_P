package Service;

import Domain.*;
import java.sql.*;
import java.util.*;

/**
 * JDBC-based Data Access Object for the Inventory module.
 *
 * <p>Implements {@link Domain.IInventoryRepository} — the domain-level contract.
 * All SQL and JDBC details are contained here; no other class touches raw SQL.</p>
 *
 * <p>Used exclusively by {@link InventoryRepository}, which delegates all SQL operations here.</p>
 */
public class InventoryDAO {

    // ─── Manufacturers ────────────────────────────────────────────────────────

    /**
     * Inserts or replaces a manufacturer in the database.
     * @param m the manufacturer to save.
     */
    public void saveManufacturer(Manufacturer m) {
        String sql = "INSERT OR REPLACE INTO manufacturers (id, name) VALUES (?, ?)";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, m.getId());
            ps.setString(2, m.getName());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] saveManufacturer error: " + e.getMessage());
        }
    }

    /**
     * Loads a manufacturer by ID.
     * @param id the manufacturer ID.
     * @return the matching {@link Manufacturer}, or {@code null} if not found.
     */
    public Manufacturer loadManufacturer(int id) {
        String sql = "SELECT * FROM manufacturers WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return new Manufacturer(rs.getInt("id"), rs.getString("name"));
        } catch (SQLException e) {
            System.err.println("[DB] loadManufacturer error: " + e.getMessage());
        }
        return null;
    }

    // ─── Categories ───────────────────────────────────────────────────────────

    /**
     * Inserts or replaces a category in the database.
     * @param c the category to save.
     */
    public void saveCategory(Category c) {
        String sql = "INSERT OR REPLACE INTO categories (id, name, parent_id) VALUES (?, ?, ?)";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, c.getId());
            ps.setString(2, c.getName());
            if (c.getParentCategory() != null) ps.setInt(3, c.getParentCategory().getId());
            else ps.setNull(3, Types.INTEGER);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] saveCategory error: " + e.getMessage());
        }
    }

    /**
     * Loads all categories from the database and rebuilds parent references.
     * @return map of category ID → {@link Category}.
     */
    public Map<Integer, Category> loadAllCategories() {
        Map<Integer, Category> map       = new HashMap<>();
        Map<Integer, Integer>  parentIds = new HashMap<>();

        String sql = "SELECT * FROM categories";
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int id      = rs.getInt("id");
                Category c  = new Category(id, rs.getString("name"));
                map.put(id, c);
                int parentId = rs.getInt("parent_id");
                if (!rs.wasNull()) parentIds.put(id, parentId);
            }
        } catch (SQLException e) {
            System.err.println("[DB] loadAllCategories error: " + e.getMessage());
        }

        // Rebuild parent references
        for (Map.Entry<Integer, Integer> entry : parentIds.entrySet()) {
            Category child  = map.get(entry.getKey());
            Category parent = map.get(entry.getValue());
            if (child != null && parent != null) child.setParentCategory(parent);
        }
        return map;
    }

    // ─── Products ─────────────────────────────────────────────────────────────

    /**
     * Inserts or replaces a product in the database, including supplier costs.
     * @param p the product to save.
     */
    public void saveProduct(Product p) {
        String sql = """
            INSERT OR REPLACE INTO products
            (id, name, manufacturer_id, min_stock, storage_amount, shelf_amount,
             aisle, shelf, delivery_day, target_quantity, base_price,
             category_id, sub_category_id, sub_sub_category_id)
            VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)
        """;
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, p.getId());
            ps.setString(2, p.getName());
            ps.setInt(3, p.getManufacturer().getId());
            ps.setInt(4, p.getMin_stock());
            ps.setInt(5, p.getStorage_amount());
            ps.setInt(6, p.getShelf_amount());
            ps.setInt(7, p.getAisle());
            ps.setInt(8, p.getShelf());
            ps.setInt(9, p.getDeliveryDay());
            ps.setInt(10, p.getTargetQuantity());
            ps.setFloat(11, p.getBasePrice());
            setNullableInt(ps, 12, p.getCategory());
            setNullableInt(ps, 13, p.getSub_category());
            setNullableInt(ps, 14, p.getSub_sub_category());
            ps.executeUpdate();

            saveManufacturer(p.getManufacturer());
            saveSupplierCosts(p);

        } catch (SQLException e) {
            System.err.println("[DB] saveProduct error: " + e.getMessage());
        }
    }

    /** Sets an integer column to NULL when the category reference is absent. */
    private void setNullableInt(PreparedStatement ps, int idx, Category c) throws SQLException {
        if (c != null) ps.setInt(idx, c.getId());
        else           ps.setNull(idx, Types.INTEGER);
    }

    /**
     * Replaces all supplier-cost rows for the given product.
     * @param p the product whose supplier costs to persist.
     */
    private void saveSupplierCosts(Product p) throws SQLException {
        String del = "DELETE FROM supplier_costs WHERE product_id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(del)) {
            ps.setInt(1, p.getId());
            ps.executeUpdate();
        }
        String ins = "INSERT INTO supplier_costs (product_id, supplier_id, cost) VALUES (?,?,?)";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(ins)) {
            for (Map.Entry<Integer, ArrayList<Float>> entry : p.getSupplierCosts().entrySet()) {
                for (float cost : entry.getValue()) {
                    ps.setInt(1, p.getId());
                    ps.setInt(2, entry.getKey());
                    ps.setFloat(3, cost);
                    ps.addBatch();
                }
            }
            ps.executeBatch();
        }
    }

    /**
     * Loads all products, resolving categories and manufacturer from the DB.
     * @param categories pre-loaded category map for FK resolution.
     * @return map of product ID → {@link Product}.
     */
    public Map<Integer, Product> loadAllProducts(Map<Integer, Category> categories) {
        Map<Integer, Product> map = new HashMap<>();
        String sql = "SELECT p.*, m.name AS man_name FROM products p " +
                "JOIN manufacturers m ON p.manufacturer_id = m.id";
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Manufacturer man = new Manufacturer(rs.getInt("manufacturer_id"), rs.getString("man_name"));
                Product p = new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        man,
                        rs.getInt("min_stock"),
                        rs.getInt("aisle"),
                        rs.getInt("shelf")
                );
                p.setStorage_amount(rs.getInt("storage_amount"));
                p.setShelf_amount(rs.getInt("shelf_amount"));
                p.setDeliveryDay(rs.getInt("delivery_day"));
                p.setTargetQuantity(rs.getInt("target_quantity"));
                float basePrice = rs.getFloat("base_price");
                if (basePrice > 0) p.addSalePrice(basePrice);

                int catId = rs.getInt("category_id");
                if (!rs.wasNull()) p.setCategory(categories.get(catId));
                int subId = rs.getInt("sub_category_id");
                if (!rs.wasNull() && categories.containsKey(subId)) p.setSub_category(categories.get(subId));
                int subSubId = rs.getInt("sub_sub_category_id");
                if (!rs.wasNull() && categories.containsKey(subSubId)) p.setSub_sub_category(categories.get(subSubId));

                map.put(p.getId(), p);
            }
        } catch (SQLException e) {
            System.err.println("[DB] loadAllProducts error: " + e.getMessage());
        }

        loadSupplierCosts(map);
        return map;
    }

    /**
     * Loads and attaches supplier costs for all products in the given map.
     * @param products the product map to populate.
     */
    private void loadSupplierCosts(Map<Integer, Product> products) {
        String sql = "SELECT * FROM supplier_costs";
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Product p = products.get(rs.getInt("product_id"));
                if (p != null) p.addPurchasePrice(rs.getInt("supplier_id"), rs.getFloat("cost"));
            }
        } catch (SQLException e) {
            System.err.println("[DB] loadSupplierCosts error: " + e.getMessage());
        }
    }

    /**
     * Deletes a product from the database by SKU.
     * @param productId the SKU to delete.
     */
    public void deleteProduct(int productId) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] deleteProduct error: " + e.getMessage());
        }
    }

    // ─── Defective Items ──────────────────────────────────────────────────────

    /**
     * Persists a defective-item record.
     * @param item the defective item to save.
     */
    public void saveDefectiveItem(DefectiveItem item) {
        String sql = "INSERT INTO defective_items (product_id, defective_quantity, defective_location) VALUES (?,?,?)";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, item.getProduct().getId());
            ps.setInt(2, item.getDefectiveQuantity());
            ps.setString(3, item.getDefectiveLocation());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] saveDefectiveItem error: " + e.getMessage());
        }
    }

    /**
     * Loads all defective-item records, resolving product references.
     * @param products product map for FK resolution.
     * @return list of {@link DefectiveItem} objects.
     */
    public List<DefectiveItem> loadAllDefectiveItems(Map<Integer, Product> products) {
        List<DefectiveItem> list = new ArrayList<>();
        String sql = "SELECT * FROM defective_items";
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Product p = products.get(rs.getInt("product_id"));
                if (p != null) list.add(new DefectiveItem(
                        p,
                        rs.getInt("defective_quantity"),
                        rs.getString("defective_location")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[DB] loadAllDefectiveItems error: " + e.getMessage());
        }
        return list;
    }

    // ─── Low Stock Alerts ─────────────────────────────────────────────────────

    /**
     * Persists a low-stock alert for historical tracking.
     * @param alert the alert to save.
     */
    public void saveLowStockAlert(LowStockAlert alert) {
        String sql = "INSERT INTO low_stock_alerts (product_id, product_name, current_stock, min_stock) VALUES (?,?,?,?)";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, alert.getProductId());
            ps.setString(2, alert.getProductName());
            ps.setInt(3, alert.getCurrentStock());
            ps.setInt(4, alert.getMinStock());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] saveLowStockAlert error: " + e.getMessage());
        }
    }

    // ─── Discounts ────────────────────────────────────────────────────────────

    /**
     * Persists a discount record (insert or replace).
     * @param d the discount to save.
     */
    public void saveDiscount(Discount d) {
        String type = (d instanceof ProductDiscount) ? "PRODUCT" : "CATEGORY";
        String sql  = "INSERT OR REPLACE INTO discounts (id, discount_percent, start_date, end_date, type) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, d.getId());
            ps.setFloat(2, d.getDiscountPercent());
            ps.setString(3, d.getStartDate().toString());
            ps.setString(4, d.getEndDate().toString());
            ps.setString(5, type);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] saveDiscount error: " + e.getMessage());
        }
    }
}