package devInventory.Service;

import Domain.Product;

/**
 * Integration bridge between the Inventory module and the Suppliers module.
 *
 * <p>This class exposes Inventory data to the Suppliers module through
 * a clean, static interface. The Suppliers module calls these functions
 * to query inventory state without directly accessing Inventory internals.</p>
 *
 * <p>This follows the Low Coupling principle — the Suppliers module
 * depends only on this interface, not on the full InventoryService.</p>
 */
public class IntegrationDummyFunctions {

    /** Shared reference to the active InventoryService. Set at startup via {@link #init}. */
    private static InventoryService inventoryService;

    /**
     * Initializes the integration bridge with the active InventoryService instance.
     * Must be called once at application startup before any other method is used.
     *
     * @param service The active {@link InventoryService} instance.
     */
    public static void init(InventoryService service) {
        inventoryService = service;
    }

    /**
     * Checks whether a product's stock is at or below its minimum threshold.
     * Called by the Suppliers module to decide whether to trigger an automatic order.
     *
     * @param productId The SKU of the product to check.
     * @return true if the product is below minimum stock; false if sufficient or not found.
     */
    public static boolean isProductBelowMinimumStock(int productId) {
        if (inventoryService == null) return false;
        Product p = inventoryService.getProduct(productId);
        return p != null && p.isBelowMinStock();
    }

    /**
     * Returns the quantity needed to bring a product back to its target stock level.
     * Calculated as: (targetQuantity - currentStock) + buffer of 20 units.
     *
     * <p>Called by the Suppliers module to determine order size.</p>
     *
     * @param productId The SKU of the product.
     * @return The quantity to order, or 0 if the product is not found or stock is sufficient.
     */
    public static int getMissingQuantityForProduct(int productId) {
        if (inventoryService == null) return 0;
        Product p = inventoryService.getProduct(productId);
        if (p == null) return 0;
        int missing = p.getMin_stock() - p.getGeneral_amount();
        return Math.max(0, missing) + 20; // +20 buffer
    }

    /**
     * Returns the full Product object for a given SKU.
     * Used by the Suppliers module to access supplier cost data for best-price calculation.
     *
     * @param productId The SKU of the product.
     * @return The {@link Product} object, or null if not found.
     */
    public static Product getProduct(int productId) {
        if (inventoryService == null) return null;
        return inventoryService.getProduct(productId);
    }

    /**
     * Returns the current total stock (warehouse + shelf) for a product.
     * Convenience method for the Suppliers module.
     *
     * @param productId The SKU of the product.
     * @return Total stock, or 0 if not found.
     */
    public static int getCurrentStock(int productId) {
        if (inventoryService == null) return 0;
        Product p = inventoryService.getProduct(productId);
        return p != null ? p.getGeneral_amount() : 0;
    }

    /**
     * Returns the minimum stock threshold for a product.
     * Used by the Suppliers module to validate order necessity.
     *
     * @param productId The SKU of the product.
     * @return The min_stock value, or 0 if not found.
     */
    public static int getMinimumStock(int productId) {
        if (inventoryService == null) return 0;
        Product p = inventoryService.getProduct(productId);
        return p != null ? p.getMin_stock() : 0;
    }
}