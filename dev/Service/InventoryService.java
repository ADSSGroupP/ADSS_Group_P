package Service;

import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import Domain.*;

/**
 * Service layer orchestrating inventory logic and data access.
 * Manages product mapping, system-wide discounts, and automated reports with timestamps.
 * Supports bulk operations for products and categories.
 * @author Hadas
 */
public class InventoryService {
    private Map<Integer, Product> products;
    private Map<Integer, Category> categories;
    private List<DefectiveItem> defectiveItems;
    private List<Discount> systemDiscounts;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public InventoryService() {
        this.products = new HashMap<>();
        this.categories = new HashMap<>();
        this.defectiveItems = new ArrayList<>();
        this.systemDiscounts = new ArrayList<>();
    }

    public List<Category> getCategoriesByNames(List<String> names) {
        return categories.values().stream()
                .filter(c -> names.stream().anyMatch(name -> name.trim().equalsIgnoreCase(c.getName())))
                .collect(Collectors.toList());
    }

    public List<Product> getProductsBySkus(List<Integer> skus) {
        return skus.stream()
                .map(products::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public void generateCategorizedReport(List<String> categoryNames) {
        printReportHeader("CATEGORIZED INVENTORY REPORT");
        for (String catName : categoryNames) {
            boolean found = false;
            String search = catName.trim();
            System.out.println("\n>> CATEGORY: " + search.toUpperCase());
            for (Product p : products.values()) {
                if (isProductInCategory(p, search)) {
                    p.updateAndGetCurrentBestPrice(systemDiscounts);
                    System.out.println("   Product: " + p.getName());
                    System.out.println("   SKU: " + p.getId());
                    System.out.println("   Location: " + p.buildLocationDescription());
                    System.out.println("   Current Stock: " + p.getGeneral_amount() + " | Sale Price: " + p.getCurrentSalePrice());
                    System.out.println("   --------------------------------------");
                    found = true;
                }
            }
            if (!found) System.out.println("   (No products found for this category)");
        }
    }

    public void generateShortageReport() {
        printReportHeader("STOCK SHORTAGE REPORT");
        boolean found = false;
        for (Product p : products.values()) {
            if (p.isBelowMinStock()) {
                System.out.println("!!! SHORTAGE ALERT: " + p.getName() + " (SKU: " + p.getId() + ")");
                System.out.println("    Stock: " + p.getGeneral_amount() + " | Required Minimum: " + p.getMin_stock());
                System.out.println("    Location: " + p.buildLocationDescription());
                System.out.println("    --------------------------------------");
                found = true;
            }
        }
        if (!found) System.out.println("No items are currently below minimum stock levels.");
    }

    public void generateDefectiveReport() {
        printReportHeader("PERIODIC DEFECTIVE ITEMS REPORT");
        if (defectiveItems.isEmpty()) {
            System.out.println("No defective or expired items reported.");
            return;
        }
        for (DefectiveItem item : defectiveItems) {
            Product p = item.getProduct();
            System.out.println("   Product: " + p.getName() + " (SKU: " + p.getId() + ")");
            System.out.println("   Quantity: " + item.getDefectiveQuantity());
            String loc = item.getDefectiveLocation();
            if (loc.equalsIgnoreCase("Store")) {
                System.out.println("   Location: Store (Aisle: " + p.getAisle() + ", Shelf: " + p.getShelf() + ")");
            } else {
                System.out.println("   Location: " + loc);
            }
            System.out.println("   --------------------------------------");
        }
    }

    private void printReportHeader(String title) {
        System.out.println("\n==================================================");
        System.out.println("REPORT: " + title);
        System.out.println("DATE: " + LocalDateTime.now().format(dateFormatter));
        System.out.println("==================================================");
    }

    private boolean isProductInCategory(Product p, String catName) {
        return (p.getCategory() != null && p.getCategory().getName().equalsIgnoreCase(catName)) ||
                (p.getSub_category() != null && p.getSub_category().getName().equalsIgnoreCase(catName)) ||
                (p.getSub_sub_category() != null && p.getSub_sub_category().getName().equalsIgnoreCase(catName));
    }

    public void updateProductStock(int id, int w, int s) {
        Product p = products.get(id);
        if (p != null) { p.setStorage_amount(w); p.setShelf_amount(s); p.checkStockStatus(); }
    }

    public void addProduct(Product p) { products.put(p.getId(), p); }
    public Product getProduct(int id) { return products.get(id); }
    public void addCategory(Category c) { categories.put(c.getId(), c); }
    public Category getCategory(int id) { return categories.get(id); }
    public void createDiscount(Discount d) { systemDiscounts.add(d); }
    public void addDefectiveItem(DefectiveItem item) { defectiveItems.add(item); }
    public void addSupplierPrice(int pId, int sId, float price) {
        Product p = products.get(pId);
        if (p != null) p.addPurchasePrice(sId, price);
    }
}