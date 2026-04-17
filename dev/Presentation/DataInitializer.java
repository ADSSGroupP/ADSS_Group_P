package Presentation;

import Service.InventoryService;

/**
 * Utility class to seed the system with initial sample data.
 * This ensures the system is functional upon startup for demonstration purposes.
 */
public class DataInitializer {

    /**
     * Seeds the inventory service with categories, manufacturers, and products.
     * @param service The InventoryService instance to populate.
     */
    public static void seedData(InventoryService service) {
        // 1. Create Sample Manufacturer
        Manufacturer tnuva = new Manufacturer(1, "Tnuva");

        // 2. Create Category Hierarchy (Dairy -> Milk)
        Category dairy = new Category(1, "Dairy");
        Category milkCategory = new Category(11, "Milk");
        milkCategory.setParentCategory(dairy);

        // 3. Register categories in service
        service.addCategory(dairy);
        service.addCategory(milkCategory);

        // 4. Create and add a Product
        // SKU: 101, Min Stock: 10, Aisle: 5, Shelf: 2
        Product milk = new Product(101, "Milk 3% 1L", tnuva, 10, 5, 2);

        // Set hierarchy and initial stock
        milk.setCategory(dairy);
        milk.setSub_category(milkCategory);
        milk.setStorage_amount(50); // Quantity in warehouse
        milk.setShelf_amount(5);    // Quantity in store shelves

        // Add initial sale price
        milk.addSalePrice(6.50f);

        // 5. Add product to the system
        service.addProduct(milk);

        System.out.println("[System] Initial data loaded: " + milk.getName() + " added to inventory.");
    }
}