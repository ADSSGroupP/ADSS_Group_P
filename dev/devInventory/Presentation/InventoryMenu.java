package devInventory.Presentation;

import Service.InventoryService;
import Domain.*;
import DTO.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class InventoryMenu {

    private final InventoryService service;
    private final Scanner scanner;

    public InventoryMenu(InventoryService service) {
        this.service = service;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        boolean running = true;
        while (running) {
            System.out.println("\n===== SUPER-LI INVENTORY MANAGEMENT SYSTEM =====");
            System.out.println("1.  Reports Menu");
            System.out.println("2.  Update Stock Levels (Warehouse & Store)");
            System.out.println("3.  Log Defective or Expired Item");
            System.out.println("4.  Manage Supplier Pricing");
            System.out.println("5.  Create Bulk Discount (Products or Categories)");
            System.out.println("6.  Add New Product");
            System.out.println("7.  Update Product Details");
            System.out.println("8.  Delete Product");
            System.out.println("9.  Transfer Units: Warehouse -> Shelf");
            System.out.println("10. Add New Category");
            System.out.println("11. View Product Details");
            System.out.println("12. Run Periodic Order Check");
            System.out.println("13. Exit System");
            System.out.print("Choice: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":  showReportsSubMenu();         break;
                case "2":  handleStockUpdate();          break;
                case "3":  handleDefectiveLog();         break;
                case "4":  handleAddSupplierPrice();     break;
                case "5":  handleCreateDiscount();       break;
                case "6":  handleAddProduct();           break;
                case "7":  handleUpdateProduct();        break;
                case "8":  handleDeleteProduct();        break;
                case "9":  handleTransferToShelf();      break;
                case "10": handleAddCategory();          break;
                case "11": handleGetProductDetails();    break;
                case "12": handlePeriodicOrderCheck();   break;
                case "13": running = false; System.out.println("Goodbye!"); break;
                default:   System.out.println("Invalid selection. Please enter 1-13.");
            }
        }
    }

    // ─── Reports ──────────────────────────────────────────────────────────────

    private void showReportsSubMenu() {
        System.out.println("\n--- REPORTS MENU ---");
        System.out.println("1. Periodic Categorized Inventory Report");
        System.out.println("2. Stock Shortage Alert Report");
        System.out.println("3. Periodic Defective Items Report");
        System.out.print("Selection: ");
        String reportChoice = scanner.nextLine().trim();
        switch (reportChoice) {
            case "1":
                System.out.print("Enter category names (comma separated): ");
                List<String> names = Arrays.asList(scanner.nextLine().split("\\s*,\\s*"));
                service.generateCategorizedReport(names);
                break;
            case "2": service.generateShortageReport();  break;
            case "3": service.generateDefectiveReport(); break;
            default:  System.out.println("Invalid selection.");
        }
    }

    // ─── Stock Update ─────────────────────────────────────────────────────────

    private void handleStockUpdate() {
        try {
            System.out.print("Enter Product SKU: ");
            int id = Integer.parseInt(scanner.nextLine());
            if (!service.productExists(id)) {
                System.out.println("ERROR: Product SKU " + id + " not found.");
                return;
            }
            System.out.print("Warehouse Amount: ");
            int w = Integer.parseInt(scanner.nextLine());
            System.out.print("Store Shelf Amount: ");
            int s = Integer.parseInt(scanner.nextLine());
            service.updateProductStock(id, w, s);
            System.out.println("Stock updated successfully.");
        } catch (NumberFormatException e) {
            System.out.println("ERROR: Please enter numeric values.");
        }
    }

    // ─── Transfer Warehouse → Shelf ───────────────────────────────────────────

    private void handleTransferToShelf() {
        try {
            System.out.print("Enter Product SKU: ");
            int id = Integer.parseInt(scanner.nextLine());
            ProductDTO p = service.getProductDTO(id);
            if (p == null) {
                System.out.println("ERROR: Product SKU " + id + " not found.");
                return;
            }
            System.out.println("Current Warehouse Stock: " + p.storageAmount());
            System.out.println("Current Shelf Stock:     " + p.shelfAmount());
            System.out.print("Enter quantity to transfer from Warehouse to Shelf: ");
            int qty = Integer.parseInt(scanner.nextLine());

            boolean success = service.transferToShelf(id, qty);
            if (success) {
                ProductDTO updated = service.getProductDTO(id);
                System.out.println("SUCCESS: " + qty + " units transferred to shelf.");
                System.out.println("New Warehouse Stock: " + updated.storageAmount());
                System.out.println("New Shelf Stock:     " + updated.shelfAmount());
            } else {
                System.out.println("ERROR: Transfer failed. Quantity must be > 0 and <= warehouse stock.");
            }
        } catch (NumberFormatException e) {
            System.out.println("ERROR: Please enter numeric values.");
        }
    }

    // ─── Defective Log ────────────────────────────────────────────────────────

    private void handleDefectiveLog() {
        try {
            System.out.print("Enter Product SKU: ");
            int sku = Integer.parseInt(scanner.nextLine());
            ProductDTO p = service.getProductDTO(sku);
            if (p == null) {
                System.out.println("ERROR: Product SKU " + sku + " not found.");
                return;
            }

            System.out.print("Location (Store/Storage): ");
            String location = scanner.nextLine().trim();

            if (!location.equalsIgnoreCase("Store") && !location.equalsIgnoreCase("Storage")) {
                System.out.println("ERROR: Invalid location. Please enter 'Store' or 'Storage'.");
                return;
            }

            System.out.print("Quantity Defective: ");
            int quantity = Integer.parseInt(scanner.nextLine());

            int available = location.equalsIgnoreCase("Store")
                    ? p.shelfAmount()
                    : p.storageAmount();

            if (quantity > available) {
                System.out.println("ERROR: Cannot log " + quantity +
                        " defective items. Only " + available +
                        " units available in " + location + ".");
                return;
            }

            boolean success = service.logDefectiveAndUpdateStock(sku, quantity, location);
            if (success) {
                System.out.println("Defect recorded successfully. Stock in " + location + " updated.");
            } else {
                System.out.println("ERROR: Could not record defective item.");
            }

        } catch (NumberFormatException e) {
            System.out.println("ERROR: Please use numeric values for SKU and Quantity.");
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    // ─── Supplier Pricing ─────────────────────────────────────────────────────

    private void handleAddSupplierPrice() {
        try {
            System.out.print("Product SKU: ");
            int sku = Integer.parseInt(scanner.nextLine());
            if (!service.productExists(sku)) {
                System.out.println("ERROR: Product not found.");
                return;
            }
            System.out.print("Supplier ID: ");
            int sid = Integer.parseInt(scanner.nextLine());
            System.out.print("Unit Cost: ");
            float pr = Float.parseFloat(scanner.nextLine());
            service.addSupplierPrice(sku, sid, pr);
            System.out.println("Supplier price updated.");
        } catch (NumberFormatException e) {
            System.out.println("ERROR: Please enter numeric values.");
        }
    }

    // ─── Product CRUD ─────────────────────────────────────────────────────────

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

            System.out.println("\n--- Main Category Assignment ---");
            System.out.println("1) Link to Existing Main Category");
            System.out.println("2) Create New Main Category");
            System.out.print("Choice: ");
            Category mainCat = handleCategoryLogic(scanner.nextLine().trim(), null);
            if (mainCat == null) return;

            Category subCat = null;
            String subInput;
            do {
                System.out.print("\nAssign Sub-Category? (y/n): ");
                subInput = scanner.nextLine().toLowerCase().trim();
            } while (!subInput.equals("y") && !subInput.equals("n"));

            if (subInput.equals("y")) {
                System.out.println("1) Link to Existing\n2) Create New");
                subCat = handleCategoryLogic(scanner.nextLine().trim(), mainCat);
            }

            Category subSubCat = null;
            if (subCat != null) {
                String subSubInput;
                do {
                    System.out.print("\nAssign Sub-Sub-Category? (y/n): ");
                    subSubInput = scanner.nextLine().toLowerCase().trim();
                } while (!subSubInput.equals("y") && !subSubInput.equals("n"));

                if (subSubInput.equals("y")) {
                    System.out.println("1) Link to Existing\n2) Create New");
                    subSubCat = handleCategoryLogic(scanner.nextLine().trim(), subCat);
                }
            }

            Product newProduct = new Product(id, name, new Manufacturer(manId, manName), minStock, aisle, shelf);
            newProduct.setCategory(mainCat);
            if (subCat != null)    newProduct.setSub_category(subCat);
            if (subSubCat != null) newProduct.setSub_sub_category(subSubCat);

            service.addProduct(newProduct);
            System.out.println("SUCCESS: Product '" + name + "' (SKU: " + id + ") added.");

        } catch (NumberFormatException e) {
            System.out.println("ERROR: Invalid number format.");
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    private void handleUpdateProduct() {
        try {
            System.out.print("Enter Product SKU to update: ");
            int id = Integer.parseInt(scanner.nextLine());
            ProductDTO p = service.getProductDTO(id);
            if (p == null) {
                System.out.println("ERROR: Product SKU " + id + " not found.");
                return;
            }

            System.out.println("Current Name: " + p.name() + " | Current Min Stock: " + p.minStock());
            System.out.print("New Name (press Enter to keep current): ");
            String newName = scanner.nextLine().trim();
            System.out.print("New Min Stock (press Enter to keep current): ");
            String minInput = scanner.nextLine().trim();

            int newMin       = minInput.isEmpty() ? p.minStock()  : Integer.parseInt(minInput);
            String nameToSet = newName.isEmpty()  ? p.name()      : newName;

            service.updateProduct(id, nameToSet, newMin);
            System.out.println("SUCCESS: Product updated.");
        } catch (NumberFormatException e) {
            System.out.println("ERROR: Invalid number format.");
        }
    }

    private void handleDeleteProduct() {
        try {
            System.out.print("Enter Product SKU to delete: ");
            int id = Integer.parseInt(scanner.nextLine());
            if (!service.productExists(id)) {
                System.out.println("ERROR: Product SKU " + id + " not found.");
                return;
            }
            System.out.print("Are you sure you want to delete SKU " + id + "? (y/n): ");
            String confirm = scanner.nextLine().trim().toLowerCase();
            if (confirm.equals("y")) {
                service.deleteProduct(id);
                System.out.println("SUCCESS: Product SKU " + id + " deleted.");
            } else {
                System.out.println("Delete cancelled.");
            }
        } catch (NumberFormatException e) {
            System.out.println("ERROR: Invalid SKU.");
        }
    }

    // ─── Product View ─────────────────────────────────────────────────────────

    private void handleGetProductDetails() {
        try {
            System.out.print("Enter Product SKU: ");
            int id = Integer.parseInt(scanner.nextLine());
            ProductDTO p = service.getProductDTO(id);
            if (p == null) {
                System.out.println("ERROR: Product SKU " + id + " not found.");
                return;
            }
            System.out.println("\n--- PRODUCT DETAILS ---");
            System.out.println("Name:         " + p.name());
            System.out.println("SKU:          " + p.id());
            System.out.println("Manufacturer: " + p.manufacturerName());
            System.out.println("Category:     " + p.categoryName());
            if (p.subCategoryName()    != null) System.out.println("Sub-Category:     " + p.subCategoryName());
            if (p.subSubCategoryName() != null) System.out.println("Sub-Sub-Category: " + p.subSubCategoryName());
            System.out.println("Warehouse Qty: " + p.storageAmount());
            System.out.println("Shelf Qty:     " + p.shelfAmount());
            System.out.println("Total Qty:     " + p.totalAmount());
            System.out.println("Min Stock:     " + p.minStock());
            System.out.println("Base Price:    " + p.basePrice());
            System.out.println("Delivery Day:  " + p.deliveryDay());
            System.out.println("Target Qty:    " + p.targetQuantity());
            float best = service.getBestPriceForProductId(id);
            if (best > 0) System.out.println("Best Current Price: " + best);
        } catch (NumberFormatException e) {
            System.out.println("ERROR: Invalid SKU.");
        }
    }

    // ─── Periodic Order Check ─────────────────────────────────────────────────

    /**
     * Triggers periodic order check for tomorrow's delivery day.
     * Orders are always placed for products scheduled for tomorrow,
     * with quantity ensuring stock will be above minimum after delivery.
     */
    private void handlePeriodicOrderCheck() {
        try {
            System.out.println("Enter tomorrow's delivery day:");
            System.out.println("1=Sunday, 2=Monday, 3=Tuesday, 4=Wednesday, 5=Thursday, 6=Friday, 7=Saturday");
            System.out.print("Day: ");
            int day = Integer.parseInt(scanner.nextLine());
            if (day < 1 || day > 7) {
                System.out.println("ERROR: Day must be between 1 and 7.");
                return;
            }
            service.checkAndProcessPeriodicOrders(day);
            System.out.println("Periodic order check completed for day " + day + ".");
        } catch (NumberFormatException e) {
            System.out.println("ERROR: Please enter a numeric value.");
        }
    }

    // ─── Discount ─────────────────────────────────────────────────────────────

    public void handleCreateDiscount() {
        try {
            System.out.print("Discount Percentage (0-100): ");
            float pct = Float.parseFloat(scanner.nextLine());
            if (pct < 0 || pct > 100) { System.out.println("ERROR: Must be 0-100."); return; }

            System.out.print("Start Date (YYYY-MM-DD): ");
            LocalDate start = LocalDate.parse(scanner.nextLine().trim());
            System.out.print("End Date (YYYY-MM-DD): ");
            LocalDate end = LocalDate.parse(scanner.nextLine().trim());

            if (start.isBefore(LocalDate.now()) || end.isBefore(start)) {
                System.out.println("ERROR: Start must be today or later; End must be after Start.");
                return;
            }

            System.out.println("Apply to:\n1) Product SKUs\n2) Categories");
            String t = scanner.nextLine().trim();

            if (t.equals("1")) {
                System.out.print("Enter SKUs (comma separated): ");
                List<Integer> skus = Arrays.stream(scanner.nextLine().split(","))
                        .map(String::trim).map(Integer::parseInt).collect(Collectors.toList());
                List<Integer> notFound = service.applyProductDiscountBySkus(pct, start, end, skus);
                if (!notFound.isEmpty()) System.out.println("WARNING: SKUs not found (skipped): " + notFound);
                System.out.println("Discount applied successfully.");
            } else {
                System.out.print("Enter Categories (comma separated): ");
                List<String> names = Arrays.stream(scanner.nextLine().split(","))
                        .map(String::trim).collect(Collectors.toList());
                List<String> notFound = service.applyCategoryDiscountByNames(pct, start, end, names);
                if (!notFound.isEmpty()) System.out.println("WARNING: Categories not found (skipped): " + notFound);
                System.out.println("Discount applied successfully.");
            }
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    // ─── Category ─────────────────────────────────────────────────────────────

    private void handleAddCategory() {
        try {
            System.out.print("Enter Main Category Name: ");
            String mainName = scanner.nextLine().trim();
            Category main = new Category(Category.getNextId(), mainName);
            service.addCategory(main);
            System.out.println("Main category '" + mainName + "' added.");

            System.out.print("Enter Sub Category (press Enter to skip): ");
            String subName = scanner.nextLine().trim();
            if (!subName.isEmpty()) {
                Category sub = new Category(Category.getNextId(), subName);
                sub.setParentCategory(main);
                service.addCategory(sub);
                System.out.println("Sub-category '" + subName + "' added.");

                System.out.print("Enter Sub-Sub Category (press Enter to skip): ");
                String subSubName = scanner.nextLine().trim();
                if (!subSubName.isEmpty()) {
                    Category subSub = new Category(Category.getNextId(), subSubName);
                    subSub.setParentCategory(sub);
                    service.addCategory(subSub);
                    System.out.println("Sub-sub-category '" + subSubName + "' added.");
                }
            }
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    private Category handleCategoryLogic(String choice, Category parent) {
        System.out.print("Enter Category Name: ");
        String catName = scanner.nextLine().trim();

        if (choice.equals("1")) {
            List<Category> found = service.getCategoriesByNames(Collections.singletonList(catName));
            if (found.isEmpty()) {
                System.out.println("ERROR: Category '" + catName + "' not found.");
                return null;
            }
            return found.get(0);
        } else {
            Category newCat = new Category(Category.getNextId(), catName);
            if (parent != null) newCat.setParentCategory(parent);
            service.addCategory(newCat);
            System.out.println("Registered new category: " + catName);
            return newCat;
        }
    }
}