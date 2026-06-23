package devInventory.Domain;

import java.time.LocalDateTime;

/**
 * Represents a low-stock alert generated automatically by the system
 * when a product's total quantity drops to or below its defined minimum threshold.
 *
 * <p>Unlike a simple warning message, this class persists the alert as a domain object,
 * allowing the system to maintain a history of all alerts generated during a session.</p>
 */
public class LowStockAlert {

    /** The unique SKU of the product that triggered the alert. */
    private final int productId;

    /** The name of the product that triggered the alert. */
    private final String productName;

    /** The total stock level (warehouse + shelf) at the moment the alert was generated. */
    private final int currentStock;

    /** The minimum stock threshold that was breached. */
    private final int minStock;

    /** The exact timestamp when this alert was created. */
    private final LocalDateTime generatedAt;

    /**
     * Constructs a new LowStockAlert by capturing the current state of the given product.
     * Should be called immediately after detecting that stock has fallen below the threshold.
     *
     * @param product The product whose stock level triggered the alert. Must not be null.
     */
    public LowStockAlert(Product product) {
        this.productId    = product.getId();
        this.productName  = product.getName();
        this.currentStock = product.getGeneral_amount();
        this.minStock     = product.getMin_stock();
        this.generatedAt  = LocalDateTime.now();
    }

    /**
     * Prints a formatted warning message to the console describing this alert.
     * Displays the product name, SKU, current stock, minimum threshold, and timestamp.
     */
    public void print() {
        System.out.println(">>> WARNING: Low stock alert for '" + productName +
                "' (SKU: " + productId + ") | Current: " + currentStock +
                " | Minimum: " + minStock + " | Time: " + generatedAt);
    }

    /**
     * Returns the SKU of the product that triggered this alert.
     * @return product SKU (integer ID).
     */
    public int getProductId() { return productId; }

    /**
     * Returns the name of the product that triggered this alert.
     * @return product name as a String.
     */
    public String getProductName() { return productName; }

    /**
     * Returns the stock level that was recorded when the alert was generated.
     * @return total stock (warehouse + shelf) at alert time.
     */
    public int getCurrentStock() { return currentStock; }

    /**
     * Returns the minimum stock threshold that was breached.
     * @return the product's min_stock value at alert time.
     */
    public int getMinStock() { return minStock; }

    /**
     * Returns the timestamp when this alert was generated.
     * @return a {@link LocalDateTime} representing the moment of alert creation.
     */
    public LocalDateTime getGeneratedAt() { return generatedAt; }
}