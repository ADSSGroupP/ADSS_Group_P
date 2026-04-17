package Presentation;

import Service.InventoryService;

/**
 * Main class to launch the Inventory module.
 */
public class Main {
    public static void main(String[] args) {
        // Step 1: Initialize the Service layer (Memory-based logic) [cite: 241, 265]
        InventoryService service = new InventoryService();

        // Step 2: Initialize sample data (External data seeding) [cite: 266, 268]

        DataInitializer.seedData(service);

        // Step 3: Launch the UI (Presentation Layer) [cite: 231]
        InventoryMenu ui = new InventoryMenu(service);
        ui.start();

        System.out.print("Rotem");
    }
}