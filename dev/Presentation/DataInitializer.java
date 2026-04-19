package Presentation;

import Service.InventoryService;
import Domain.*;

/**
 * Enhanced utility class to seed the system with broad sample data for testing.
 */
public class DataInitializer {

    public static void seedData(InventoryService service) {
        // 1. Manufacturers
        Manufacturer tnuva = new Manufacturer(1, "Tnuva");
        Manufacturer osem = new Manufacturer(2, "Osem");
        Manufacturer strauss = new Manufacturer(3, "Strauss");

        // 2. Category Hierarchy: Dairy -> Milk -> Fresh Milk
        Category dairy = new Category(1, "Dairy");
        Category milk = new Category(11, "Milk");
        Category freshMilk = new Category(111, "Fresh Milk");

        milk.setParentCategory(dairy);
        freshMilk.setParentCategory(milk);

        // Category Hierarchy: Cleaning -> Kitchen -> Soaps
        Category cleaning = new Category(2, "Cleaning");
        Category kitchen = new Category(22, "Kitchen");
        Category soaps = new Category(222, "Soaps");

        kitchen.setParentCategory(cleaning);
        soaps.setParentCategory(kitchen);

        // Register categories in service
        service.addCategory(dairy); service.addCategory(milk); service.addCategory(freshMilk);
        service.addCategory(cleaning); service.addCategory(kitchen); service.addCategory(soaps);

        // 3. Products
        // Product 1: Milk 3% (Under Dairy -> Milk -> Fresh Milk)
        Product milk3 = new Product(101, "Milk 3% Tnuva", tnuva, 10, 5, 2);
        milk3.setCategory(dairy);
        milk3.setSub_category(milk);
        milk3.setSub_sub_category(freshMilk);
        milk3.setStorage_amount(50);
        milk3.setShelf_amount(5);
        milk3.addSalePrice(6.50f); // Base Price
        service.addProduct(milk3);

        // Product 2: Bamba (No sub-categories)
        Product bamba = new Product(102, "Bamba Large", osem, 20, 3, 1);
        bamba.setStorage_amount(100);
        bamba.setShelf_amount(30);
        bamba.addSalePrice(4.00f); // Base Price
        service.addProduct(bamba);

        // Product 3: Dish Soap (Under Cleaning -> Kitchen -> Soaps)
        Product soap = new Product(103, "Dish Soap 1L", strauss, 5, 8, 4);
        soap.setCategory(cleaning);
        soap.setSub_category(kitchen);
        soap.setSub_sub_category(soaps);
        soap.setStorage_amount(15);
        soap.setShelf_amount(2);
        soap.addSalePrice(12.00f); // Base Price
        service.addProduct(soap);

        System.out.println("[System] Broad data seeded successfully.");
    }
}