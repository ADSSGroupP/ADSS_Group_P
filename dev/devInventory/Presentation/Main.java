package Presentation;

import Service.DatabaseManager;
import Service.IntegrationDummyFunctions;
import Service.InventoryService;

/**
 * Entry point for the Super-Li Inventory Management System.
 * Connects to the existing SQLite database (superli_inventory.db).
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  SUPER-LI INVENTORY MANAGEMENT SYSTEM ");
        System.out.println("  (Persistent DB Mode - SQLite)         ");
        System.out.println("========================================");

        // Connect to existing DB and load data
        InventoryService service = new InventoryService();

        // Initialize integration bridge
        IntegrationDummyFunctions.init(service);

        // Shutdown hook to close DB cleanly
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            DatabaseManager.closeConnection();
            System.out.println("System shutdown complete.");
        }));

        // Launch UI
        new InventoryMenu(service).start();
    }
}