package Domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a product in the inventory management system.
 * All original fields and accessors are preserved.
 */
public class Product {
    // --- Original Fields ---
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
    private Map<Integer, ArrayList<Float>> supplierCosts;
    private ArrayList<Float> salesHistory;
    private List<Discount> specificDiscounts;

    public Product(int id, String name, Manufacturer manufacturer, int minStock, int aisle, int shelf) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Product name cannot be empty");
        if (manufacturer == null) throw new IllegalArgumentException("Manufacturer cannot be null");
        if (minStock < 0 || aisle < 0 || shelf < 0) throw new IllegalArgumentException("Negative values not allowed");

        this.id = id;
        this.name = name;
        this.manufacturer = manufacturer;
        this.min_stock = minStock;
        this.aisle = aisle;
        this.shelf = shelf;
        this.storage_amount = 0;
        this.shelf_amount = 0;
        this.supplierCosts = new HashMap<>();
        this.salesHistory = new ArrayList<>();
        this.specificDiscounts = new ArrayList<>();
    }

    // --- Fixed Operations ---

    /**
     * Requirement: Finds the best price from global discounts and updates history.
     * Calculation is based on the initial base price to avoid compounding.
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
     * Requirement: Record a new purchase price from a specific supplier.
     */
    public void addPurchasePrice(int supplierId, float cost) {
        if (supplierId < 0 || cost < 0) throw new IllegalArgumentException("ID or Cost cannot be negative");
        supplierCosts.putIfAbsent(supplierId, new ArrayList<>());
        supplierCosts.get(supplierId).add(cost);
    }

    // --- All Getters & Setters Preserved ---

    public int getStorage_amount() { return storage_amount; }
    public int getShelf_amount() { return shelf_amount; }
    public int getId() { return id; }
    public String getName() { return name; }
    public Manufacturer getManufacturer() { return manufacturer; }
    public int getMin_stock() { return min_stock; }
    public int getShelf() { return shelf; }
    public int getAisle() { return aisle; }
    public Category getCategory() { return category; }
    public Category getSub_category() { return sub_category; }
    public Category getSub_sub_category() { return sub_sub_category; }

    public float getCurrentSalePrice() {
        if (salesHistory == null || salesHistory.isEmpty()) return 0;
        return salesHistory.get(salesHistory.size() - 1);
    }

    public float getBasePrice() {
        if (salesHistory == null || salesHistory.isEmpty()) return 0;
        return salesHistory.get(0);
    }

    public void addSalePrice(float price) {
        if (price < 0) throw new IllegalArgumentException("Price cannot be negative");
        this.salesHistory.add(price);
    }

    public void setShelf_amount(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Shelf quantity cannot be negative");
        }
        this.shelf_amount = amount;
    }

    public void setStorage_amount(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Storage quantity cannot be negative");
        }
        this.storage_amount = amount;
    }
    public void setShelf(int shelf) {
        if (shelf < 0) {
            throw new IllegalArgumentException("Shelf number cannot be negative");
        }
        this.shelf = shelf;
    }

    public void setAisle(int aisle) {
        if (aisle < 0) {
            throw new IllegalArgumentException("Aisle number cannot be negative");
        }
        this.aisle = aisle;
    }

    public void setCategory(Category c) {
        if (c == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        this.category = c;
    }

    public void setSub_category(Category sc) {
        if (sc == null) {
            throw new IllegalArgumentException("Sub category cannot be null");
        }
        this.sub_category = sc;
    }

    public void setSub_sub_category(Category ssc) {
        if (ssc == null) {
            throw new IllegalArgumentException("Sub sub category cannot be null");
        }
        this.sub_sub_category = ssc;
    }
    // --- Stock & Summary ---

    public int getGeneral_amount() { return storage_amount + shelf_amount; }
    public boolean isBelowMinStock() { return getGeneral_amount() <= min_stock; }

    public void checkStockStatus() {
        if (isBelowMinStock()) System.out.println("WARNING: inventory of " + name + " is short!");
    }

    public void addSpecificDiscount(Discount d) { if (d != null) specificDiscounts.add(d); }

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
     * Prints a summary of the product including its name, ID, location, and stock.
     */
    public void printProductSummary() {
        System.out.println("Product Name: " + name + " | SKU: " + id);
        System.out.println("Location: " + buildLocationDescription());
        System.out.println("Total Amount: " + getGeneral_amount());
    }
}