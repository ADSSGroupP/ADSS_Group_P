package Domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a product in the inventory management system.
 * Handles categories, location tracking, stock levels, sale prices,
 * supplier costs, and discount calculations.
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

    private Map<Integer, ArrayList<Float>> supplierCosts;
    private ArrayList<Float> salesHistory;
    private List<Discount> specificDiscounts;

    /**
     * Constructor.
     * Parameter order is:
     * id, name, manufacturer, min stock, aisle, shelf
     */
    public Product(int id, String name, Manufacturer manufacturer, int minStock, int aisle, int shelf) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }
        if (manufacturer == null) {
            throw new IllegalArgumentException("Manufacturer cannot be null");
        }
        if (minStock < 0) {
            throw new IllegalArgumentException("Min stock cannot be negative");
        }
        if (aisle < 0) {
            throw new IllegalArgumentException("Aisle cannot be negative");
        }
        if (shelf < 0) {
            throw new IllegalArgumentException("Shelf cannot be negative");
        }

        this.id = id;
        this.name = name;
        this.manufacturer = manufacturer;
        this.min_stock = minStock;
        this.aisle = aisle;
        this.shelf = shelf;

        this.storage_amount = 0;
        this.shelf_amount = 0;

        this.category = null;
        this.sub_category = null;
        this.sub_sub_category = null;

        this.supplierCosts = new HashMap<>();
        this.salesHistory = new ArrayList<>();
        this.specificDiscounts = new ArrayList<>();
    }

    /**
     * Total inventory amount = storage + shelf.
     */
    public int getGeneral_amount() {
        return storage_amount + shelf_amount;
    }

    /**
     * Returns true if the total stock is at or below the minimum stock.
     */
    public boolean isBelowMinStock() {
        return getGeneral_amount() <= min_stock;
    }

    /**
     * Prints a warning if stock is low.
     */
    public void checkStockStatus() {
        if (isBelowMinStock()) {
            System.out.println("WARNING: your inventory of " + this.name + " is short!");
        }
    }

    /**
     * Returns the best active price after checking:
     * 1. specific discounts on the product
     * 2. discounts from the category hierarchy
     */
    public float getBestPrice() {
        float originalPrice = getCurrentSalePrice();
        float bestPrice = originalPrice;

        List<Discount> potentialDiscounts = new ArrayList<>(specificDiscounts);

        Category current = sub_sub_category;
        while (current != null) {
            potentialDiscounts.addAll(current.getCategoryDiscounts());
            current = current.getParentCategory();
        }

        for (Discount discount : potentialDiscounts) {
            if (discount != null && discount.isActive()) {
                float discountedPrice = discount.applyDiscount(originalPrice);
                if (discountedPrice < bestPrice) {
                    bestPrice = discountedPrice;
                }
            }
        }

        return bestPrice;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Manufacturer getManufacturer() {
        return manufacturer;
    }

    public int getMin_stock() {
        return min_stock;
    }

    public int getShelf() {
        return shelf;
    }

    public int getAisle() {
        return aisle;
    }

    public int getStorage_amount() {
        return storage_amount;
    }

    public int getShelf_amount() {
        return shelf_amount;
    }

    public Category getCategory() {
        return category;
    }

    public Category getSub_category() {
        return sub_category;
    }

    public Category getSub_sub_category() {
        return sub_sub_category;
    }

    public float getCurrentSalePrice() {
        if (salesHistory.isEmpty()) {
            return 0;
        }
        return salesHistory.get(salesHistory.size() - 1);
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
            throw new IllegalArgumentException("Shelf cannot be negative");
        }
        this.shelf = shelf;
    }

    public void setAisle(int aisle) {
        if (aisle < 0) {
            throw new IllegalArgumentException("Aisle cannot be negative");
        }
        this.aisle = aisle;
    }

    public void setCategory(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        this.category = category;
    }

    public void setSub_category(Category subCategory) {
        if (subCategory == null) {
            throw new IllegalArgumentException("Sub category cannot be null");
        }
        this.sub_category = subCategory;
    }

    public void setSub_sub_category(Category subSubCategory) {
        if (subSubCategory == null) {
            throw new IllegalArgumentException("Sub sub category cannot be null");
        }
        this.sub_sub_category = subSubCategory;
    }

    public void addPurchasePrice(int supplierId, float cost) {
        if (supplierId < 0) {
            throw new IllegalArgumentException("Supplier id cannot be negative");
        }
        if (cost < 0) {
            throw new IllegalArgumentException("Cost cannot be negative");
        }

        supplierCosts.putIfAbsent(supplierId, new ArrayList<>());
        supplierCosts.get(supplierId).add(cost);
    }

    public void addSalePrice(float price) {
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        salesHistory.add(price);
    }

    public void addSpecificDiscount(Discount discount) {
        if (discount == null) {
            throw new IllegalArgumentException("Discount cannot be null");
        }
        specificDiscounts.add(discount);
    }

    public void printProductSummary() {
        System.out.println("Product Name: " + name);
        System.out.println("Product SKU: " + id);
        System.out.println("Location: " + buildLocationDescription());
        System.out.println("Total Amount: " + getGeneral_amount());
    }

    public String buildLocationDescription() {
        String location = "";

        if (storage_amount > 0) {
            location += "Storage (" + storage_amount + ")";
        }

        if (shelf_amount > 0) {
            if (!location.isEmpty()) {
                location += ", ";
            }
            location += "Store - Aisle " + aisle + ", Shelf " + shelf + " (" + shelf_amount + ")";
        }

        if (location.isEmpty()) {
            location = "No location data";
        }

        return location;
    }
}