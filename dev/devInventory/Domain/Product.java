package devInventory.Domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a product in the Super-Li inventory management system.
 *
 * <p>A product tracks its physical location (aisle and shelf), stock levels
 * in both the warehouse and store shelf, category hierarchy (up to three levels),
 * pricing history, and supplier cost records.</p>
 *
 * <p>Core requirement: each product has a minimum stock threshold. When total
 * stock drops to or below this threshold, an alert must be generated.</p>
 */
public class Product {

    private int id;
    private String name;
    private Manufacturer manufacturer;
    private int min_stock;
    private Category category;
    private Category sub_category;
    private Category sub_sub_category;
    private int shelf;
    private int aisle;
    private int storage_amount;
    private int shelf_amount;


    /**
     * Maps supplier ID → list of historical purchase prices from that supplier.
     * Allows tracking cost history per supplier for integration with the Suppliers module.
     */
    private Map<Integer, ArrayList<Float>> supplierCosts;

    /**
     * Chronological list of sale prices. The first entry is the original base price;
     * subsequent entries are discounted prices applied over time.
     */
    private ArrayList<Float> salesHistory;

    /** List of discount IDs (from {@link Discount}) that apply to this product. */
    private List<Integer> specificDiscounts;

    /** Day of the week on which this product is typically delivered (1=Sunday … 7=Saturday). */
    private int deliveryDay;

    /** The desired stock level after a replenishment order arrives. */
    private int targetQuantity;

    // ─── Constructor ──────────────────────────────────────────────────────────────

    /**
     * Constructs a new Product with mandatory fields.
     * Stock amounts default to zero and must be set separately via setters.
     *
     * @param id           Unique SKU for the product.
     * @param name         Display name; must not be null or blank.
     * @param manufacturer The product's manufacturer; must not be null.
     * @param minStock     Minimum stock threshold; must be non-negative.
     * @param aisle        Aisle number in the store; must be non-negative.
     * @param shelf        Shelf number within the aisle; must be non-negative.
     * @throws IllegalArgumentException if any argument fails validation.
     */
    public Product(int id, String name, Manufacturer manufacturer, int minStock, int aisle, int shelf) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Product name cannot be empty");
        if (manufacturer == null)                  throw new IllegalArgumentException("Manufacturer cannot be null");
        if (minStock < 0 || aisle < 0 || shelf < 0) throw new IllegalArgumentException("Negative values not allowed");

        this.id               = id;
        this.name             = name;
        this.manufacturer     = manufacturer;
        this.min_stock        = minStock;
        this.aisle            = aisle;
        this.shelf            = shelf;
        this.storage_amount   = 0;
        this.shelf_amount     = 0;
        this.supplierCosts    = new HashMap<>();
        this.salesHistory     = new ArrayList<>();
        this.specificDiscounts = new ArrayList<>();
    }

    // ─── Business Logic ───────────────────────────────────────────────────────────

    /**
     * Iterates over all active system discounts, finds the one that yields the lowest
     * price for this product, records that price in the sales history, and returns it.
     *
     * <p>Discount application is always calculated from the original base price
     * (index 0 of salesHistory) to avoid compounding errors.</p>
     *
     * @param allSystemDiscounts The full list of discounts registered in the system.
     * @return The best (lowest) applicable price for this product today.
     */
    public float updateAndGetCurrentBestPrice(List<Discount> allSystemDiscounts) {
        float basePrice = getBasePrice();
        float bestPrice = basePrice;

        if (allSystemDiscounts != null) {
            for (Discount d : allSystemDiscounts) {
                if (d.isActive() && d.isProductEligible(this)) {
                    float discountedPrice = d.apply(basePrice);
                    if (discountedPrice < bestPrice) {
                        bestPrice = discountedPrice;
                    }
                }
            }
        }
        addSalePrice(bestPrice);
        return bestPrice;
    }

    /**
     * Records a new purchase cost from a specific supplier.
     * Multiple costs can be recorded per supplier over time.
     *
     * @param supplierId The ID of the supplier; must be non-negative.
     * @param cost       The unit price charged by the supplier; must be non-negative.
     * @throws IllegalArgumentException if supplierId or cost is negative.
     */
    public void addPurchasePrice(int supplierId, float cost) {
        if (supplierId < 0 || cost < 0) throw new IllegalArgumentException("ID or Cost cannot be negative");
        supplierCosts.putIfAbsent(supplierId, new ArrayList<>());
        supplierCosts.get(supplierId).add(cost);
    }

    // ─── Getters ──────────────────────────────────────────────────────────────────

    /** @return number of units currently in the warehouse. */
    public int getStorage_amount() { return storage_amount; }

    /** @return number of units currently on the store shelf. */
    public int getShelf_amount() { return shelf_amount; }

    /** @return the product's unique SKU. */
    public int getId() { return id; }

    /** @return the product's display name. */
    public String getName() { return name; }

    /** @return the product's manufacturer. */
    public Manufacturer getManufacturer() { return manufacturer; }

    /** @return the minimum stock threshold. */
    public int getMin_stock() { return min_stock; }

    /** @return the shelf number within the store aisle. */
    public int getShelf() { return shelf; }

    /** @return the aisle number in the store. */
    public int getAisle() { return aisle; }

    /** @return the top-level category, or null if not set. */
    public Category getCategory() { return category; }

    /** @return the sub-category, or null if not set. */
    public Category getSub_category() { return sub_category; }

    /** @return the sub-sub-category, or null if not set. */
    public Category getSub_sub_category() { return sub_sub_category; }

    /** @return the scheduled delivery day (1=Sunday … 7=Saturday). */
    public int getDeliveryDay() { return deliveryDay; }

    /** @return the target replenishment quantity. */
    public int getTargetQuantity() { return targetQuantity; }

    /**
     * Returns the full supplier cost map for integration with the Suppliers module.
     * @return map of supplierId → list of historical costs.
     */
    public Map<Integer, ArrayList<Float>> getSupplierCosts() { return this.supplierCosts; }

    /**
     * Returns the most recently recorded sale price.
     * @return last entry in salesHistory, or 0 if no price has been set.
     */
    public float getCurrentSalePrice() {
        if (salesHistory == null || salesHistory.isEmpty()) return 0;
        return salesHistory.get(salesHistory.size() - 1);
    }

    /**
     * Returns the original base (selling) price — the first entry in salesHistory.
     * @return base price, or 0 if salesHistory is empty.
     */
    public float getBasePrice() {
        if (salesHistory == null || salesHistory.isEmpty()) return 0;
        return salesHistory.get(0);
    }

    // ─── Setters ──────────────────────────────────────────────────────────────────

    /**
     * Updates the product's display name.
     * @param name New name; must not be null or blank.
     * @throws IllegalArgumentException if name is null or empty.
     */
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Name cannot be empty");
        this.name = name;
    }

    /**
     * Updates the product's minimum stock threshold.
     * @param minStock New threshold; must be non-negative.
     * @throws IllegalArgumentException if minStock is negative.
     */
    public void setMinStock(int minStock) {
        if (minStock < 0) throw new IllegalArgumentException("Min stock cannot be negative");
        this.min_stock = minStock;
    }

    /**
     * Appends a new price to the sales history.
     * @param price The price to record; must be non-negative.
     * @throws IllegalArgumentException if price is negative.
     */
    public void addSalePrice(float price) {
        if (price < 0) throw new IllegalArgumentException("Price cannot be negative");
        this.salesHistory.add(price);
    }

    /**
     * Sets the scheduled delivery day for this product.
     * @param deliveryDay Day of week (1=Sunday, 2=Monday, … 7=Saturday).
     */
    public void setDeliveryDay(int deliveryDay) { this.deliveryDay = deliveryDay; }

    /**
     * Sets the target stock quantity to be reached after a replenishment order.
     * @param targetQuantity Desired quantity; should exceed min_stock.
     */
    public void setTargetQuantity(int targetQuantity) { this.targetQuantity = targetQuantity; }

    /**
     * Sets the number of units on the store shelf.
     * @param amount New shelf quantity; must be non-negative.
     * @throws IllegalArgumentException if amount is negative.
     */
    public void setShelf_amount(int amount) {
        if (amount < 0) throw new IllegalArgumentException("Shelf quantity cannot be negative");
        this.shelf_amount = amount;
    }

    /**
     * Sets the number of units in the warehouse.
     * @param amount New warehouse quantity; must be non-negative.
     * @throws IllegalArgumentException if amount is negative.
     */
    public void setStorage_amount(int amount) {
        if (amount < 0) throw new IllegalArgumentException("Storage quantity cannot be negative");
        this.storage_amount = amount;
    }

    /**
     * Sets the shelf number in the store.
     * @param shelf Shelf number; must be non-negative.
     */
    public void setShelf(int shelf) {
        if (shelf < 0) throw new IllegalArgumentException("Shelf number cannot be negative");
        this.shelf = shelf;
    }

    /**
     * Sets the aisle number in the store.
     * @param aisle Aisle number; must be non-negative.
     */
    public void setAisle(int aisle) {
        if (aisle < 0) throw new IllegalArgumentException("Aisle number cannot be negative");
        this.aisle = aisle;
    }

    /**
     * Assigns the top-level (main) category to this product.
     * @param c The main category; must not be null.
     */
    public void setCategory(Category c) {
        if (c == null) throw new IllegalArgumentException("Category cannot be null");
        this.category = c;
    }

    /**
     * Assigns the sub-category to this product.
     * @param sc The sub-category; must not be null.
     */
    public void setSub_category(Category sc) {
        if (sc == null) throw new IllegalArgumentException("Sub category cannot be null");
        this.sub_category = sc;
    }

    /**
     * Assigns the sub-sub-category to this product.
     * @param ssc The sub-sub-category; must not be null.
     */
    public void setSub_sub_category(Category ssc) {
        if (ssc == null) throw new IllegalArgumentException("Sub sub category cannot be null");
        this.sub_sub_category = ssc;
    }

    // ─── Stock Utilities ──────────────────────────────────────────────────────────

    /**
     * Returns the combined stock across warehouse and shelf.
     * @return storage_amount + shelf_amount.
     */
    public int getGeneral_amount() { return storage_amount + shelf_amount; }

    /**
     * Checks whether total stock has reached or fallen below the minimum threshold.
     * @return true if total stock {@code <= min_stock}.
     */
    public boolean isBelowMinStock() { return getGeneral_amount() <= min_stock; }

    /**
     * Prints a console warning if stock is at or below the minimum threshold.
     * Used for immediate in-session feedback.
     */
    public void checkStockStatus() {
        if (isBelowMinStock()) System.out.println("WARNING: inventory of " + name + " is short!");
    }

    /**
     * Links a discount ID to this product so it can be quickly identified as eligible.
     * @param d The discount ID to associate; ignored if null.
     */
    public void addSpecificDiscount(Integer d) { if (d != null) specificDiscounts.add(d); }

    /**
     * Builds a human-readable string describing where units of this product are located.
     * Shows warehouse count and/or store aisle/shelf information.
     *
     * @return A formatted location string, or "No location data" if both amounts are zero.
     */
    public String buildLocationDescription() {
        String loc = "";
        if (storage_amount > 0) loc += "Storage (" + storage_amount + ")";
        if (shelf_amount > 0) {
            if (!loc.isEmpty()) loc += ", ";
            loc += "Store - Aisle " + aisle + ", Shelf " + shelf + " (" + shelf_amount + ")";
        }
        return loc.isEmpty() ? "No location data" : loc;
    }

    /**
     * Calculates how many units need to be ordered to reach the target quantity.
     * Returns 0 if current stock already meets or exceeds the target.
     *
     * @return Units to order, or 0 if no order is needed.
     */
    public int getAmountToOrder() {
        int current = getGeneral_amount();
        if (current >= targetQuantity) return 0;
        return targetQuantity - current;
    }

    /**
     * Prints a formatted summary of this product to the console, including
     * name, SKU, manufacturer, location, warehouse/shelf quantities, and min stock.
     */
    public void printProductSummary() {
        System.out.println("Product Name: " + name + " | SKU: " + id);
        System.out.println("Manufacturer: " + manufacturer.getName());
        System.out.println("Location: " + buildLocationDescription());
        System.out.println("Warehouse Qty: " + storage_amount + " | Shelf Qty: " + shelf_amount);
        System.out.println("Total Amount: " + getGeneral_amount() + " | Min Stock: " + min_stock);
    }
}