package Presentation;

import Service.DatabaseManager;
import Service.InventoryService;
import Domain.*;
import java.sql.SQLException;

/**
 * Seeds the SQLite database with initial sample data.
 *
 * <p>Uses {@link DatabaseManager#tableHasData(String)} to avoid duplicate seeding
 * on subsequent runs — data persists across restarts in the SQLite file.</p>
 *
 * <p><b>Default DB entities:</b>
 * <ul>
 *   <li>Manufacturers: Tnuva (1), Osem (2)</li>
 *   <li>Categories: Dairy(1) → Milk(2) → FullFat(3); Snacks(4)</li>
 *   <li>Products: SKU 101 (Milk 3%, price 6.90), SKU 102 (Cottage, price 8.50), SKU 201 (Bamba, price 4.90)</li>
 *   <li>Suppliers: Each product has multiple suppliers at different prices to enable cheapest-supplier selection</li>
 * </ul>
 * Data persists across restarts (SQLite file: superli_inventory.db).
 * </p>
 */
public class DataInitializer {

    /**
     * Seeds the default dataset only if the products table is empty.
     * Safe to call on every startup.
     *
     * @param service The InventoryService to populate.
     */
    public static void seedData(InventoryService service) {
        try {
            if (DatabaseManager.tableHasData("products")) {
                System.out.println("[DB] Data already exists — skipping seed.");
                return;
            }
        } catch (SQLException e) {
            System.err.println("[DB] Could not check table: " + e.getMessage());
        }

        Manufacturer tnuva = new Manufacturer(1, "Tnuva");
        Manufacturer osem  = new Manufacturer(2, "Osem");

        // Categories
        Category dairy   = new Category(1, "Dairy");
        Category milk    = new Category(2, "Milk");
        Category fullFat = new Category(3, "FullFat");
        milk.setParentCategory(dairy);
        fullFat.setParentCategory(milk);
        Category snacks  = new Category(4, "Snacks");

        service.addCategory(dairy);
        service.addCategory(milk);
        service.addCategory(fullFat);
        service.addCategory(snacks);

        // SKU 101 — Milk 3%
        // Supplier 10: 4.50 NIS/unit, Supplier 11: 4.20 NIS/unit (cheaper), Supplier 12: 4.80 NIS/unit
        Product milk3 = new Product(101, "Milk 3% Tnuva", tnuva, 20, 5, 2);
        milk3.setCategory(dairy);
        milk3.setSub_category(milk);
        milk3.setSub_sub_category(fullFat);
        milk3.setStorage_amount(50);
        milk3.setShelf_amount(5);
        milk3.setDeliveryDay(2);
        milk3.setTargetQuantity(100);
        milk3.addSalePrice(6.90f);
        milk3.addPurchasePrice(10, 4.50f);  // Supplier 10: Tnuva Supplies Ltd.
        milk3.addPurchasePrice(11, 4.20f);  // Supplier 11: DairyDirect Ltd. (cheapest)
        milk3.addPurchasePrice(12, 4.80f);  // Supplier 12: FreshMilk Co.
        service.addProduct(milk3);

        // SKU 102 — Cottage Cheese
        // Supplier 10: 5.20 NIS/unit, Supplier 11: 4.90 NIS/unit (cheaper)
        Product cottage = new Product(102, "Cottage Cheese", tnuva, 15, 5, 3);
        cottage.setCategory(dairy);
        cottage.setSub_category(milk);
        cottage.setStorage_amount(30);
        cottage.setShelf_amount(10);
        cottage.setDeliveryDay(3);
        cottage.setTargetQuantity(80);
        cottage.addSalePrice(8.50f);
        cottage.addPurchasePrice(10, 5.20f);  // Supplier 10: Tnuva Supplies Ltd.
        cottage.addPurchasePrice(11, 4.90f);  // Supplier 11: DairyDirect Ltd. (cheapest)
        service.addProduct(cottage);

        // SKU 201 — Bamba
        // Supplier 20: 2.80 NIS/unit, Supplier 21: 3.10 NIS/unit, Supplier 22: 2.60 NIS/unit (cheapest)
        Product bamba = new Product(201, "Bamba", osem, 10, 2, 1);
        bamba.setCategory(snacks);
        bamba.setStorage_amount(100);
        bamba.setShelf_amount(20);
        bamba.setDeliveryDay(4);
        bamba.setTargetQuantity(150);
        bamba.addSalePrice(4.90f);
        bamba.addPurchasePrice(20, 2.80f);  // Supplier 20: Osem Direct
        bamba.addPurchasePrice(21, 3.10f);  // Supplier 21: SnackWorld Ltd.
        bamba.addPurchasePrice(22, 2.60f);  // Supplier 22: BulkSnacks Co. (cheapest)
        service.addProduct(bamba);

        System.out.println("[DB] Default data seeded: 3 products, 4 categories, multiple suppliers per product.");
    }

    /**
     * Seeds a minimal custom dataset (only if DB is empty).
     *
     * @param service The InventoryService to populate.
     */
    public static void seedNewCustomData(InventoryService service) {
        try {
            if (DatabaseManager.tableHasData("products")) {
                System.out.println("[DB] Data already exists — skipping seed.");
                return;
            }
        } catch (SQLException e) {
            System.err.println("[DB] Could not check table: " + e.getMessage());
        }

        Manufacturer fresh = new Manufacturer(3, "FreshCo");
        Category freshCat  = new Category(5, "Fresh");
        service.addCategory(freshCat);

        Product p = new Product(301, "Test Product", fresh, 10, 1, 1);
        p.setCategory(freshCat);
        p.setDeliveryDay(3);
        p.setTargetQuantity(50);
        p.addSalePrice(12.00f);
        p.addPurchasePrice(30, 8.00f);  // Supplier 30
        p.addPurchasePrice(31, 7.50f);  // Supplier 31 (cheapest)
        service.addProduct(p);

        System.out.println("[DB] Custom data seeded: 1 product.");
    }
}
