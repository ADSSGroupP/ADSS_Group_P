package Service;

import java.util.*;
import java.util.stream.Collectors;
import Domain.*;

/**
 * Service layer orchestrating inventory logic and data access.
 * Handles product mapping, system-wide discounts, and report generation.
 * This version includes full validation for SKUs and categories.
 */
public class InventoryService {
    /** Map of products indexed by their unique SKU (ID). */
    private Map<Integer, Product> products;

    /** Map of categories indexed by their unique ID. */
    private Map<Integer, Category> categories;

    /** List of all reported defective or expired items. */
    private List<DefectiveItem> defectiveItems;

    /** List of active system-wide discounts. */
    private List<Discount> systemDiscounts;

    public InventoryService() {
        this.products = new HashMap<>();
        this.categories = new HashMap<>();
        this.defectiveItems = new ArrayList<>();
        this.systemDiscounts = new ArrayList<>();
    }

    /**
     * Generates a full inventory report grouped by categories.
     * Provides console feedback if requested categories are not found or are empty.
     * @param categoryNames List of strings representing category names to filter by.
     */
    public void generateCategorizedReport(List<String> categoryNames) {
        InventoryReport report = new InventoryReport();
        Map<String, List<Product>> filteredMap = new HashMap<>();
        List<String> missingCategories = new ArrayList<>();

        for (String name : categoryNames) {
            String search = name.trim();
            List<Product> matches = products.values().stream()
                    .filter(p -> isProductInCategory(p, search))
                    .collect(Collectors.toList());

            if (!matches.isEmpty()) {
                filteredMap.put(search, matches);
            } else {
                missingCategories.add(search);
            }
        }

        if (!missingCategories.isEmpty() && !categoryNames.isEmpty() && !categoryNames.get(0).isEmpty()) {
            System.out.println("\n>>> NOTICE: The following requested categories were not found: " + missingCategories);
        }

        if (!filteredMap.isEmpty()) {
            report.printReport(filteredMap);
        } else if (!categoryNames.isEmpty() && !categoryNames.get(0).isEmpty()) {
            System.out.println(">>> ERROR: No data found for any of the specified categories.");
        }
    }

    /**
     * Generates a report for all products currently below their minimum stock level.
     */
    public void generateShortageReport() {
        ShortageReport report = new ShortageReport();
        List<Product> shortages = products.values().stream()
                .filter(Product::isBelowMinStock)
                .collect(Collectors.toList());
        report.printReport(shortages);
    }

    /**
     * Generates a report of all items marked as defective or expired.
     */
    public void generateDefectiveReport() {
        DefectiveReport report = new DefectiveReport();
        report.printReport(defectiveItems);
    }

    /**
     * Checks if a product belongs to a category by name (case-insensitive).
     */
    private boolean isProductInCategory(Product p, String catName) {
        return (p.getCategory() != null && p.getCategory().getName().equalsIgnoreCase(catName)) ||
                (p.getSub_category() != null && p.getSub_category().getName().equalsIgnoreCase(catName)) ||
                (p.getSub_sub_category() != null && p.getSub_sub_category().getName().equalsIgnoreCase(catName));
    }

    /**
     * Updates stock levels for a specific product.
     * @return true if the product exists and was updated, false otherwise.
     */
    public boolean updateProductStock(int id, int w, int s) {
        Product p = products.get(id);
        if (p != null) {
            p.setStorage_amount(w);
            p.setShelf_amount(s);
            p.checkStockStatus();
            return true;
        }
        return false;
    }

    /**
     * Adds a defective item to the tracking list if the product is valid.
     * @return true if successful, false if the item or product is null.
     */
    public boolean addDefectiveItem(DefectiveItem item) {
        if (item != null && item.getProduct() != null) {
            defectiveItems.add(item);
            return true;
        }
        return false;
    }

    // --- Data Access & Management ---

    public void addProduct(Product p) { products.put(p.getId(), p); }
    public Product getProduct(int id) { return products.get(id); }
    public void addCategory(Category c) { categories.put(c.getId(), c); }
    public Category getCategory(int id) { return categories.get(id); }
    public void createDiscount(Discount d) { systemDiscounts.add(d); }

    public List<Category> getCategoriesByNames(List<String> names) {
        return categories.values().stream()
                .filter(c -> names.stream().anyMatch(name -> name.trim().equalsIgnoreCase(c.getName())))
                .collect(Collectors.toList());
    }

    public List<Product> getProductsBySkus(List<Integer> skus) {
        return skus.stream().map(products::get).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public void addSupplierPrice(int pId, int sId, float price) {
        Product p = products.get(pId);
        if (p != null) p.addPurchasePrice(sId, price);
    }
}