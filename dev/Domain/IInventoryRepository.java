package Domain;

import java.util.List;
import java.util.Map;

/**
 * Domain-level repository interface for the Inventory module.
 *
 * <p>Defines the contract for all persistence operations on inventory entities.
 * The domain layer depends only on this interface — never on SQL or JDBC directly.
 * Concrete implementations (e.g., {@code InventoryDAO}) live in the Service layer.</p>
 *
 * <p>Following GRASP Information Expert and Low Coupling principles:
 * the domain declares <em>what</em> it needs persisted; the DAO decides <em>how</em>.</p>
 */
public interface IInventoryRepository {

    // ─── Manufacturers ────────────────────────────────────────────────────────

    /**
     * Persists a manufacturer (insert or replace).
     * @param m the manufacturer to save; must not be null.
     */
    void saveManufacturer(Manufacturer m);

    /**
     * Loads a manufacturer by primary key.
     * @param id the manufacturer ID.
     * @return the matching {@link Manufacturer}, or {@code null} if not found.
     */
    Manufacturer loadManufacturer(int id);

    // ─── Categories ───────────────────────────────────────────────────────────

    /**
     * Persists a category (insert or replace).
     * @param c the category to save; must not be null.
     */
    void saveCategory(Category c);

    /**
     * Loads all categories and rebuilds parent-child references.
     * @return map of category ID → {@link Category}.
     */
    Map<Integer, Category> loadAllCategories();

    // ─── Products ─────────────────────────────────────────────────────────────

    /**
     * Persists a product and its supplier costs (insert or replace).
     * @param p the product to save; must not be null.
     */
    void saveProduct(Product p);

    /**
     * Loads all products with their categories and manufacturer data.
     * @param categories pre-loaded category map used to resolve FK references.
     * @return map of product ID → {@link Product}.
     */
    Map<Integer, Product> loadAllProducts(Map<Integer, Category> categories);

    /**
     * Deletes a product from the store by SKU.
     * @param productId the SKU to remove.
     */
    void deleteProduct(int productId);

    // ─── Defective Items ──────────────────────────────────────────────────────

    /**
     * Persists a defective-item record.
     * @param item the defective item to save; must not be null.
     */
    void saveDefectiveItem(DefectiveItem item);

    /**
     * Loads all defective-item records.
     * @param products product map used to resolve product references.
     * @return list of {@link DefectiveItem} objects.
     */
    List<DefectiveItem> loadAllDefectiveItems(Map<Integer, Product> products);

    // ─── Low Stock Alerts ─────────────────────────────────────────────────────

    /**
     * Persists a low-stock alert for historical tracking.
     * @param alert the alert to save; must not be null.
     */
    void saveLowStockAlert(LowStockAlert alert);

    // ─── Discounts ────────────────────────────────────────────────────────────

    /**
     * Persists a discount record (insert or replace).
     * @param d the discount to save; must not be null.
     */
    void saveDiscount(Discount d);
}