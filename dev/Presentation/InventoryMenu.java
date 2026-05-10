package Presentation;

import Service.InventoryService;
import Domain.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Command Line Interface (CLI) for the Super-Li Inventory Management System.
 * This class handles all user input/output interactions and routes them to the service layer.
 */
public class InventoryMenu {
    // The service layer instance used to perform business logic operations
    private final InventoryService service;
    // Scanner used to capture input from the standard input stream
    private final Scanner scanner;

    /**
     * Initializes the menu with the required service layer.
     * @param service The InventoryService instance.
     */
    public InventoryMenu(InventoryService service) {
        this.service = service;
        this.scanner = new Scanner(System.in);
    }

    /**
     * The main execution loop of the application.
     * Displays the menu options and processes user selections until exit.
     */
    public void start() {
        boolean running = true;
        while (running) {
            System.out.println("\n===== SUPER-LI INVENTORY MANAGEMENT SYSTEM =====");
            System.out.println("1. Reports Menu");
            System.out.println("2. Update Stock Levels");
            System.out.println("3. Log Defective Item");
            System.out.println("4. Manage Supplier Pricing");
            System.out.println("5. Create Bulk Discount");
            System.out.println("6. Add New Product");
            System.out.println("7. Add New Category");
            System.out.println("8. Exit System");
            System.out.print("Please select an option: ");

            String choice = scanner.nextLine();
            // Switch case to handle menu routing
            switch (choice) {
                case "1": showReportsSubMenu(); break;
                case "2": handleStockUpdate(); break;
                case "3": handleDefectiveLog(); break;
                case "4": handleAddSupplierPrice(); break;
                case "5": handleCreateDiscount(); break;
                case "6": handleAddProduct(); break;
                case "7": handleAddCategory(); break;
                case "8": running = false; break;
                default: System.out.println("Invalid selection.");
            }
        }
    }

    /**
     * Guides the user through adding a category and its potential sub-categories.
     * Validates input for Main, Sub, and Sub-Sub categories.
     */
    private void handleAddCategory() {
        try {
            System.out.print("Enter Main Category Name: ");
            String mainName = scanner.nextLine();
            Category main = new Category(Category.getNextId(), mainName);

            // Add the main category to the service
            service.addCategory(main);
            System.out.println("Main category '" + mainName + "' added.");

            System.out.print("Enter Sub Category Name (Optional - press Enter to skip): ");
            String subName = scanner.nextLine();
            if (!subName.isEmpty()) {
                Category sub = new Category(Category.getNextId(), subName);
                sub.setParentCategory(main);
                service.addCategory(sub);
                System.out.println("Sub category '" + subName + "' added.");

                System.out.print("Enter Sub-Sub Category Name (Optional - press Enter to skip): ");
                String subSubName = scanner.nextLine();
                if (!subSubName.isEmpty()) {
                    Category subSub = new Category(Category.getNextId(), subSubName);
                    subSub.setParentCategory(sub);
                    service.addCategory(subSub);
                    System.out.println("Sub-sub category '" + subSubName + "' added.");
                }
            }
            System.out.println("Category hierarchy added successfully.");
        } catch (Exception e) {
            System.out.println("ERROR: Failed to add category: " + e.getMessage());
        }
    }

    /**
     * Handles adding a new product to the inventory, including category assignment.
     */
    private void handleAddProduct() {
        try {
            System.out.print("Enter SKU (ID): ");
            int id = Integer.parseInt(scanner.nextLine());

            if (service.productExists(id)) {
                System.out.println("ERROR: Product with SKU " + id + " already exists.");
                return;
            }

            System.out.print("Enter Product Name: ");
            String name = scanner.nextLine();
            System.out.print("Enter Manufacturer ID: ");
            int manId = Integer.parseInt(scanner.nextLine());
            System.out.print("Enter Manufacturer Name: ");
            String manName = scanner.nextLine();
            System.out.print("Enter Min Stock: ");
            int minStock = Integer.parseInt(scanner.nextLine());
            System.out.print("Enter Aisle: ");
            int aisle = Integer.parseInt(scanner.nextLine());
            System.out.print("Enter Shelf: ");
            int shelf = Integer.parseInt(scanner.nextLine());

            // --- Category Selection Logic ---
            System.out.print("Enter Main Category Name (Required): ");
            String mainName = scanner.nextLine();
            List<Category> mainCats = service.getCategoriesByNames(Collections.singletonList(mainName));
            if (mainCats.isEmpty()) {
                System.out.println("ERROR: Main category '" + mainName + "' not found.");
                return;
            }
            Category mainCat = mainCats.get(0);

            Category subCat = null;
            System.out.print("Enter Sub Category Name (Optional - press Enter to skip): ");
            String subName = scanner.nextLine();
            if (!subName.isEmpty()) {
                List<Category> subCats = service.getCategoriesByNames(Collections.singletonList(subName));
                if (!subCats.isEmpty()) subCat = subCats.get(0);
                else System.out.println("WARNING: Sub category '" + subName + "' not found, skipping.");
            }

            Category subSubCat = null;
            System.out.print("Enter Sub-Sub Category Name (Optional - press Enter to skip): ");
            String subSubName = scanner.nextLine();
            if (!subSubName.isEmpty()) {
                List<Category> subSubCats = service.getCategoriesByNames(Collections.singletonList(subSubName));
                if (!subSubCats.isEmpty()) subSubCat = subSubCats.get(0);
                else System.out.println("WARNING: Sub-Sub category '" + subSubName + "' not found, skipping.");
            }
            // --------------------------------

            // Create and configure the product
            Product newProduct = new Product(id, name, new Manufacturer(manId, manName), minStock, aisle, shelf);

            // Assuming Product has setters for categories based on your Product.java file
            newProduct.setCategory(mainCat);
            if (subCat != null) newProduct.setSub_category(subCat);
            if (subSubCat != null) newProduct.setSub_sub_category(subSubCat);

            service.addProduct(newProduct);
            System.out.println("SUCCESS: Product '" + name + "' added successfully with categories.");

        } catch (NumberFormatException e) {
            System.out.println("ERROR: Invalid number format. Please ensure numeric fields are correct.");
        } catch (Exception e) {
            System.out.println("ERROR: Failed to add product: " + e.getMessage());
        }
    }
    /**
     * Displays a sub-menu specifically for generating various inventory reports.
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
     * Handles bulk discount creation process with immediate validation for each input.
     * If an input is invalid, it prints an error and returns to the menu immediately.
     */
    public void handleCreateDiscount() {
        try {
            // 1. Percentage Validation
            System.out.print("Discount Percentage: ");
            float pct = Float.parseFloat(scanner.nextLine());
            if (pct < 0 || pct > 100) {
                System.out.println("ERROR: Discount percentage must be between 0 and 100.");
                return;
            }

            // 2. Start Date Validation
            System.out.print("Start Date (YYYY-MM-DD): ");
            LocalDate start = LocalDate.parse(scanner.nextLine());
            if (start.isBefore(LocalDate.now())) {
                System.out.println("ERROR: Start date cannot be in the past.");
                return;
            }

            // 3. End Date Validation
            System.out.print("End Date (YYYY-MM-DD): ");
            LocalDate end = LocalDate.parse(scanner.nextLine());

            // Validate sequence of dates
            if (end.isBefore(start)) {
                System.out.println("ERROR: End date cannot be before start date.");
                return;
            }
            if (end.isBefore(LocalDate.now())) {
                System.out.println("ERROR: End date cannot be in the past.");
                return;
            }

            // If all validations passed, proceed with logic
            System.out.println("Apply discount to:\n1) List of Product SKUs\n2) List of Category Names");
            System.out.print("Choice: ");
            String target = scanner.nextLine();

            if (target.equals("1")) {
                System.out.print("Enter SKUs (separated by commas): ");
                List<Integer> requestedSkus = Arrays.stream(scanner.nextLine().split(","))
                        .map(String::trim).map(Integer::parseInt).collect(Collectors.toList());
                List<Product> foundProducts = service.getProductsBySkus(requestedSkus);

                if (!foundProducts.isEmpty()) {
                    service.createDiscount(new ProductDiscount(Discount.getNextId(), pct, start, end, foundProducts));
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
                    service.createDiscount(new CategoryDiscount(Discount.getNextId(), pct, start, end, foundCategories));
                    System.out.println("SUCCESS: Discount applied to categories: " + foundCategories.stream().map(Category::getName).collect(Collectors.joining(", ")));
                }

                List<String> foundNames = foundCategories.stream().map(c -> c.getName().toLowerCase()).collect(Collectors.toList());
                List<String> missing = requestedNames.stream().filter(n -> !foundNames.contains(n.toLowerCase())).collect(Collectors.toList());
                if (!missing.isEmpty()) System.out.println("WARNING: Categories NOT found: " + missing);
            }
        } catch (Exception e) {
            System.out.println("ERROR: Invalid input format.");
        }
    }

    /**
     * Prompts for SKU and stock amounts to perform an update on a specific product.
     */
    private void handleStockUpdate() {
        try {
            System.out.print("Enter Product SKU: ");
            int id = Integer.parseInt(scanner.nextLine());

            // Validate product exists before updating
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
     * Logic to record defects for a product.
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
     * Allows adding a new supplier price point for an existing product.
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