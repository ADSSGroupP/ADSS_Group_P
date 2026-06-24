package devInventory.Service;

import devInventory.Domain.Product;

/**
 * Logic-based testing class to verify integration between modules.
 */
public class IntegrationTester {

    /**
     * Runs integration scenarios and prints results to console.
     * @param service The active InventoryService.
     */
    public static void runAllTests(InventoryService service) {
        System.out.println("\n--- [RUNNING INTEGRATION TESTS] ---");

        // Test 1: Automatic shortage order
        Product p = service.getProduct(101);
        if (p != null) {
            System.out.println("[Test] Triggering shortage for: " + p.getName());
            service.updateProductStock(101, 2, 3); // Triggers Supplier Order
        }

        // Test 2: Periodic check (tomorrow is Monday)
        System.out.println("\n[Test] Running periodic check for tomorrow...");
        service.checkAndProcessPeriodicOrders(2);

        System.out.println("--- [TESTS COMPLETED] ---\n");
    }
}