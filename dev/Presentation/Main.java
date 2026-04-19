package Presentation;

import Service.InventoryService;

/**
 * Main class to launch the Inventory module.
 */
public class Main {
    public static void main(String[] args) {
        // Step 1: Initialize the Service layer (Memory-based logic)
        InventoryService service = new InventoryService();

        // Step 2: Initialize sample data (External data seeding)

        DataInitializer.seedData(service);

        // Step 3: Launch the UI (Presentation Layer)
        InventoryMenu ui = new InventoryMenu(service);
        ui.start();
    }
}