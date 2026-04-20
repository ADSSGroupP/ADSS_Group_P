package Presentation;

import Service.InventoryService;
import Domain.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Command Line Interface for the Super-Li Inventory Management System.
 * Handles user interactions and input validation for inventory operations.
 */
public class InventoryMenu {
    private final InventoryService service;
    private final Scanner scanner;

    public InventoryMenu(InventoryService service) {
        this.service = service;
        this.scanner = new Scanner(System.in);
    }

    /**
     * Main application loop.
     */
    public void start() {
        boolean running = true;
        while (running) {
            System.out.println("\n===== SUPER-LI INVENTORY MANAGEMENT SYSTEM =====");
            System.out.println("1. Reports Menu");
            System.out.println("2. Update Stock Levels (Warehouse & Store)");
            System.out.println("3. Log Defective or Expired Item");
            System.out.println("4. Manage Supplier Pricing");
            System.out.println("5. Create Bulk Discount (Products or Categories)");
            System.out.println("6. Exit System");
            System.out.print("Please select an option: ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1": showReportsSubMenu(); break;
                case "2": handleStockUpdate(); break;
                case "3": handleDefectiveLog(); break;
                case "4": handleAddSupplierPrice(); break;
                case "5": handleCreateDiscount(); break;
                case "6": running = false; break;
                default: System.out.println("Invalid selection. Try again.");
            }
        }
    }

    /**
     * Sub-menu for generating various inventory reports.
     */
    private void showReportsSubMenu() {
        System.out.println("\n--- REPORTS MENU ---");
        System.out.println("1. Periodic Categorized Inventory Report");
        System.out.println("2. Stock Shortage Alert Report");
        System.out.println("3. Periodic Defective Items Report");
        System.out.print("Selection: ");

        String reportChoice = scanner.nextLine();
        switch (reportChoice) {
            case "1":
                System.out.print("Enter category names to include (comma separated): ");
                String input = scanner.nextLine();
                List<String> names = Arrays.asList(input.split("\\s*,\\s*"));
                service.generateCategorizedReport(names);
                break;
            case "2": service.generateShortageReport(); break;
            case "3": service.generateDefectiveReport(); break;
            default: System.out.println("Returning to main menu...");
        }
    }

    /**
     * Handles bulk discount creation for products or categories.
     */
    public void handleCreateDiscount() {
        try {
            System.out.print("Discount Percentage: ");
            float pct = Float.parseFloat(scanner.nextLine());
            System.out.print("Start Date (YYYY-MM-DD): ");
            LocalDate start = LocalDate.parse(scanner.nextLine());
            System.out.print("End Date (YYYY-MM-DD): ");
            LocalDate end = LocalDate.parse(scanner.nextLine());

            System.out.println("Apply discount to:\n1) List of Product SKUs\n2) List of Category Names");
            System.out.print("Choice: ");
            String target = scanner.nextLine();

            if (target.equals("1")) {
                System.out.print("Enter SKUs (separated by commas): ");
                List<Integer> requestedSkus = Arrays.stream(scanner.nextLine().split(","))
                        .map(String::trim).map(Integer::parseInt).collect(Collectors.toList());
                List<Product> foundProducts = service.getProductsBySkus(requestedSkus);

                if (!foundProducts.isEmpty()) {
                    service.createDiscount(new ProductDiscount(new Random().nextInt(1000), pct, start, end, foundProducts));
                    System.out.println("SUCCESS: Discount applied to: " + foundProducts.stream().map(Product::getName).collect(Collectors.joining(", ")));
                }

                List<Integer> foundIds = foundProducts.stream().map(Product::getId).collect(Collectors.toList());
                List<Integer> missing = requestedSkus.stream().filter(id -> !foundIds.contains(id)).collect(Collectors.toList());
                if (!missing.isEmpty()) System.out.println("WARNING: SKUs NOT found: " + missing);
            } else {
                System.out.print("Enter Category Names (separated by commas): ");
                List<String> requestedNames = Arrays.stream(scanner.nextLine().split(",")).map(String::trim).collect(Collectors.toList());
                List<Category> foundCategories = service.getCategoriesByNames(requestedNames);

                if (!foundCategories.isEmpty()) {
                    service.createDiscount(new CategoryDiscount(new Random().nextInt(1000), pct, start, end, foundCategories));
                    System.out.println("SUCCESS: Discount applied to categories: " + foundCategories.stream().map(Category::getName).collect(Collectors.joining(", ")));
                }

                List<String> foundNames = foundCategories.stream().map(c -> c.getName().toLowerCase()).collect(Collectors.toList());
                List<String> missing = requestedNames.stream().filter(n -> !foundNames.contains(n.toLowerCase())).collect(Collectors.toList());
                if (!missing.isEmpty()) System.out.println("WARNING: Categories NOT found: " + missing);
            }
        } catch (Exception e) { System.out.println("Invalid input format."); }
    }

    /**
     * Updates stock levels for a product after verifying its existence.
     */
    private void handleStockUpdate() {
        try {
            System.out.print("Enter Product SKU: ");
            int id = Integer.parseInt(scanner.nextLine());

            if (service.getProduct(id) == null) {
                System.out.println("ERROR: Product with SKU " + id + " does not exist.");
                return;
            }

            System.out.print("Warehouse Amount: ");
            int w = Integer.parseInt(scanner.nextLine());
            System.out.print("Store Shelf Amount: ");
            int s = Integer.parseInt(scanner.nextLine());

            service.updateProductStock(id, w, s);
            System.out.println("Stock updated successfully.");
        } catch (Exception e) { System.out.println("Error: Use numbers only."); }
    }

    /**
     * Logs a defective item for a product after verifying its existence.
     */
    private void handleDefectiveLog() {
        try {
            System.out.print("Enter Product SKU: ");
            int sku = Integer.parseInt(scanner.nextLine());
            Product p = service.getProduct(sku);

            if (p != null) {
                System.out.print("Quantity Defective: ");
                int q = Integer.parseInt(scanner.nextLine());
                System.out.print("Location (Store/Storage): ");
                String l = scanner.nextLine();
                service.addDefectiveItem(new DefectiveItem(p, q, l));
                System.out.println("Defect recorded successfully.");
            } else {
                System.out.println("ERROR: Product SKU " + sku + " not found.");
            }
        } catch (Exception e) { System.out.println("Error logging defect."); }
    }

    /**
     * Adds purchase cost from a supplier to a specific product.
     */
    private void handleAddSupplierPrice() {
        try {
            System.out.print("SKU: ");
            int sku = Integer.parseInt(scanner.nextLine());
            if (service.getProduct(sku) == null) {
                System.out.println("ERROR: SKU not found.");
                return;
            }
            System.out.print("Supplier ID: ");
            int sid = Integer.parseInt(scanner.nextLine());
            System.out.print("Unit Cost: ");
            float pr = Float.parseFloat(scanner.nextLine());
            service.addSupplierPrice(sku, sid, pr);
            System.out.println("Supplier price updated.");
        } catch (Exception e) { System.out.println("Error."); }
    }
}