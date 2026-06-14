package Presentation;

import Service.DatabaseManager;
import Service.IntegrationDummyFunctions;
import Service.InventoryService;
import java.util.Scanner;

/**
 * Entry point for the Super-Li Inventory Management System (Assignment 2).
 * Uses SQLite for persistent storage.
 * Initializes the integration bridge with the Suppliers module.
 */
public class Main {

    /**
     * Application entry point.
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("========================================");
        System.out.println("  SUPER-LI INVENTORY MANAGEMENT SYSTEM ");
        System.out.println("  (Persistent DB Mode - SQLite)         ");
        System.out.println("========================================");
        System.out.println("Select Data Source:");
        System.out.println("1. Default DB  (Milk, Cottage, Bamba)");
        System.out.println("2. Custom DB   (1 test product)");
        System.out.println("3. Start Empty");
        System.out.print("Choice: ");

        String choice = sc.nextLine().trim();

        // Connect to DB and load existing data
        InventoryService service = new InventoryService();

        // Initialize integration bridge — Suppliers module can now query Inventory
        IntegrationDummyFunctions.init(service);

        switch (choice) {
            case "1": DataInitializer.seedData(service);          break;
            case "2": DataInitializer.seedNewCustomData(service); break;
            case "3": System.out.println("Starting with existing DB data."); break;
            default:  System.out.println("Invalid — loading existing DB data.");
        }

        // Shutdown hook to close DB cleanly
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            DatabaseManager.closeConnection();
            System.out.println("System shutdown complete.");
        }));

        // Launch UI
        new InventoryMenu(service).start();
    }
}