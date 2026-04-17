package Presentation;

import Service.InventoryService;
import Domain.DefectiveItem;
import Domain.Product;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

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
            System.out.println("\n===== SUPER-LI INVENTORY MANAGEMENT =====");
            System.out.println("1. Reports Menu");
            System.out.println("2. Update Stock (Warehouse & Shop Shelves)");
            System.out.println("3. Log Defective or Expired Item");
            System.out.println("4. Exit System");
            System.out.print("Select an option: ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1": showReportsSubMenu(); break;
                case "2": handleStockUpdate(); break;
                case "3": handleDefectiveLog(); break;
                case "4": running = false; break;
                default: System.out.println("Invalid selection.");
            }
        }
    }

    private void showReportsSubMenu() {
        System.out.println("\n--- Reports Menu ---");
        System.out.println("1. Inventory Report (Filter by Categories)");
        System.out.println("2. Shortage Alert Report (Items below minimum)");
        System.out.println("3. Periodic Defective Items Report");
        System.out.print("Choose report type: ");

        String reportChoice = scanner.nextLine();

        if (reportChoice.equals("1")) {
            // תיקון: לבקש מהעובד שמות של קטגוריות (דרישה 11)
            System.out.println("Enter category names separated by commas (e.g., 'Dairy, Meat')");
            System.out.println("Or press ENTER to see ALL items:");
            String input = scanner.nextLine();

            List<String> categories = new ArrayList<>();
            if (!input.trim().isEmpty()) {
                categories = Arrays.asList(input.split(","));
            }
            service.generateCategorizedReport(categories); // שים לב: צריך לעדכן גם ב-Service שיקבל רשימה

        } else if (reportChoice.equals("2")) {
            service.generateShortageReport();
        } else if (reportChoice.equals("3")) {
            service.generateDefectiveReport();
        }
    }

    private void handleDefectiveLog() {
        try {
            System.out.println("\n--- Log Defective Item ---");
            System.out.print("Enter Product SKU: ");
            int id = Integer.parseInt(scanner.nextLine());

            Product p = service.getProduct(id);
            if (p == null) {
                System.out.println("Product not found.");
                return;
            }

            System.out.print("Enter Quantity Found Defective: ");
            int qty = Integer.parseInt(scanner.nextLine());

            // תיקון: לבקש מיקום ספציפי (חנות/מחסן) לפי הפורום
            System.out.print("Where was it found? (Store / Storage): ");
            String loc = scanner.nextLine();

            DefectiveItem item = new DefectiveItem(p, qty, loc);
            service.addDefectiveItem(item);
            System.out.println("Defective item recorded.");
        } catch (Exception e) {
            System.out.println("Error: Invalid input.");
        }
    }

    private void handleStockUpdate() {
        try {
            System.out.print("Enter SKU: ");
            int id = Integer.parseInt(scanner.nextLine());
            System.out.print("Warehouse Qty: ");
            int storage = Integer.parseInt(scanner.nextLine());
            System.out.print("Shop Shelves Qty: ");
            int shelf = Integer.parseInt(scanner.nextLine());

            service.updateProductStock(id, storage, shelf);
            System.out.println("Stock updated.");
        } catch (Exception e) {
            System.out.println("Invalid numeric input.");
        }
    }
}