package Service;

import java.util.*;

/**
 * Service layer responsible for managing inventory operations.
 * It acts as the orchestrator between the Domain entities and the Presentation layer.
 * This class maintains rapid access maps for products, categories, and hierarchical relations.
 */
public class InventoryService {
    // Fast lookup for products by their ID (SKU)
    private Map<Integer, Product> products;

    // Fast lookup for categories by their ID
    private Map<Integer, Category> categories;

    // Requirement: Map linking Category ID to a list of its associated Products [cite: 6]
    private Map<Integer, List<Product>> categoryProductsMap;

    // Tracking defective or expired items for reporting [cite: 9, 12]
    private List<DefectiveItem> defectiveItems;

    /**
     * Initializes the service with empty data structures.
     */
    public InventoryService() {
        this.products = new HashMap<>();
        this.categories = new HashMap<>();
        this.categoryProductsMap = new HashMap<>();
        this.defectiveItems = new ArrayList<>();
    }

    // --- Data Management Methods ---

    /**
     * Adds a category to the system.
     * @param c The Category object to be stored.
     */
    public void addCategory(Category c) {
        if (c != null) {
            categories.put(c.getId(), c);
        }
    }

    /**
     * Adds a product and automatically updates the Category-Product mapping. [cite: 5, 6]
     * @param p The Product object to be stored.
     */
    public void addProduct(Product p) {
        if (p == null) return;
        products.put(p.getId(), p);

        Category[] cats = {p.getCategory(), p.getSub_category(), p.getSub_sub_category()};
        for (Category cat : cats) {
            if (cat != null) {
                categoryProductsMap.putIfAbsent(cat.getId(), new ArrayList<>());
                if (!categoryProductsMap.get(cat.getId()).contains(p)) {
                    categoryProductsMap.get(cat.getId()).add(p);
                }
            }
        }
    }

    /**
     * Updates stock levels for a specific product and triggers automatic alerts if low. [cite: 1, 5]
     * @param productId The ID of the product.
     * @param warehouseQty Quantity in the back storage.
     * @param shopQty Quantity on the store shelves.
     */
    public void updateProductStock(int productId, int warehouseQty, int shopQty) {
        Product p = products.get(productId);
        if (p != null) {
            p.setStorage_amount(warehouseQty);
            p.setShelf_amount(shopQty);

            // Automatic stock alert check
            p.checkStockStatus();
        }
    }

    /**
     * Records a defective item found in a specific location.
     * @param item The DefectiveItem details.
     */
    public void addDefectiveItem(DefectiveItem item) {
        if (item != null) {
            this.defectiveItems.add(item);
        }
    }

    // --- Reporting Methods ---

    /**
     * Generates a full inventory report grouped by categories.
     * Requirement 11: Allows viewing items based on their categories.
     */
    public void generateCategorizedReport(List<String> categoryNames) {
        InventoryReport reporter = new InventoryReport();
        Map<String, List<Product>> reportData = new HashMap<>();

        if (categoryNames == null || categoryNames.isEmpty()) {
            for (Map.Entry<Integer, List<Product>> entry : categoryProductsMap.entrySet()) {
                Category cat = categories.get(entry.getKey());
                if (cat != null) reportData.put(cat.getName(), entry.getValue());
            }
        } else {
            for (String name : categoryNames) {
                for (Category cat : categories.values()) {
                    if (cat.getName().equalsIgnoreCase(name.trim())) {
                        reportData.put(cat.getName(), categoryProductsMap.getOrDefault(cat.getId(), new ArrayList<>()));
                    }
                }
            }
        }

        if (reportData.isEmpty()) {
            System.out.println("No matching categories or products found.");
        } else {
            reporter.printReport(reportData);
        }
    }

    /**
     * Generates a report of all products currently below their minimum stock threshold.
     */
    public void generateShortageReport() {
        List<Product> shortages = new ArrayList<>();
        for (Product p : products.values()) {
            if (p.getGeneral_amount() <= p.getMin_stock()) {
                shortages.add(p);
            }
        }
        ShortageReport reporter = new ShortageReport();
        reporter.printReport(shortages);
    }

    /**
     * Generates a report of items marked as defective or expired. [cite: 12]
     */
    public void generateDefectiveReport() {
        DefectiveReport reporter = new DefectiveReport();
        reporter.printReport(new ArrayList<>(defectiveItems));
    }

    // --- Getters ---

    /**
     * Retrieves a product by its ID.
     * @param id Product SKU/ID.
     * @return The Product object or null if not found.
     */
    public Product getProduct(int id) {
        return products.get(id);
    }
}