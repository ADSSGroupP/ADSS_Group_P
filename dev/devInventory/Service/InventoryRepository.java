package devInventory.Service;

import Domain.*;
import java.util.List;
import java.util.Map;

/**
 * Repository implementation for the Inventory module.
 *
 * <p>Implements {@link Domain.IInventoryRepository} — the domain-level contract.
 * Delegates all SQL operations to {@link InventoryDAO}, maintaining a clean
 * separation between domain logic and data access.</p>
 *
 * <p>Following GRASP Low Coupling and High Cohesion principles:
 * this class speaks domain language; {@link InventoryDAO} speaks SQL.</p>
 */
public class InventoryRepository implements Domain.IInventoryRepository {

    private final InventoryDAO dao;

    /**
     * Constructs an InventoryRepository backed by a new {@link InventoryDAO}.
     */
    public InventoryRepository() {
        this.dao = new InventoryDAO();
    }

    // ─── Manufacturers ────────────────────────────────────────────────────────

    /**
     * Persists a manufacturer (insert or replace).
     * @param m the manufacturer to save; must not be null.
     */
    @Override
    public void saveManufacturer(Manufacturer m) {
        dao.saveManufacturer(m);
    }

    /**
     * Loads a manufacturer by primary key.
     * @param id the manufacturer ID.
     * @return the matching {@link Manufacturer}, or {@code null} if not found.
     */
    @Override
    public Manufacturer loadManufacturer(int id) {
        return dao.loadManufacturer(id);
    }

    // ─── Categories ───────────────────────────────────────────────────────────

    /**
     * Persists a category (insert or replace).
     * @param c the category to save; must not be null.
     */
    @Override
    public void saveCategory(Category c) {
        dao.saveCategory(c);
    }

    /**
     * Loads all categories and rebuilds parent-child references.
     * @return map of category ID → {@link Category}.
     */
    @Override
    public Map<Integer, Category> loadAllCategories() {
        return dao.loadAllCategories();
    }

    // ─── Products ─────────────────────────────────────────────────────────────

    /**
     * Persists a product and its supplier costs (insert or replace).
     * @param p the product to save; must not be null.
     */
    @Override
    public void saveProduct(Product p) {
        dao.saveProduct(p);
    }

    /**
     * Loads all products with their categories and manufacturer data.
     * @param categories pre-loaded category map used to resolve FK references.
     * @return map of product ID → {@link Product}.
     */
    @Override
    public Map<Integer, Product> loadAllProducts(Map<Integer, Category> categories) {
        return dao.loadAllProducts(categories);
    }

    /**
     * Deletes a product from the store by SKU.
     * @param productId the SKU to remove.
     */
    @Override
    public void deleteProduct(int productId) {
        dao.deleteProduct(productId);
    }

    // ─── Defective Items ──────────────────────────────────────────────────────

    /**
     * Persists a defective-item record.
     * @param item the defective item to save; must not be null.
     */
    @Override
    public void saveDefectiveItem(DefectiveItem item) {
        dao.saveDefectiveItem(item);
    }

    /**
     * Loads all defective-item records.
     * @param products product map used to resolve product references.
     * @return list of {@link DefectiveItem} objects.
     */
    @Override
    public List<DefectiveItem> loadAllDefectiveItems(Map<Integer, Product> products) {
        return dao.loadAllDefectiveItems(products);
    }

    // ─── Low Stock Alerts ─────────────────────────────────────────────────────

    /**
     * Persists a low-stock alert for historical tracking.
     * @param alert the alert to save; must not be null.
     */
    @Override
    public void saveLowStockAlert(LowStockAlert alert) {
        dao.saveLowStockAlert(alert);
    }

    // ─── Discounts ────────────────────────────────────────────────────────────

    /**
     * Persists a discount record (insert or replace).
     * @param d the discount to save; must not be null.
     */
    @Override
    public void saveDiscount(Discount d) {
        dao.saveDiscount(d);
    }
}
