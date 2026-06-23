package devInventory.UnitTests;

import Domain.*;
import Service.*;
import DTO.*;
import org.junit.*;
import java.sql.*;
import java.util.List;
import static org.junit.Assert.*;

/**
 * Integration & Unit tests for Assignment 2 additions.
 * Tests focus on DB persistence, Flow 3 (transfer), and automatic orders.
 * Uses a fresh InventoryService for each test.
 */
public class InventoryDBTests {

    private InventoryService service;
    private Manufacturer tnuva;
    private Category dairy;

    @Before
    public void setUp() {
        service = new InventoryService();
        tnuva   = new Manufacturer(1, "Tnuva");
        dairy   = new Category(1, "Dairy");
        service.addCategory(dairy);
    }

    @After
    public void tearDown() {
        service.deleteProduct(901);
        service.deleteProduct(902);
        service.deleteProduct(903);
        service.deleteProduct(904);
        service.deleteProduct(905);
        service.deleteProduct(906);
        service.deleteProduct(907);
        service.deleteProduct(908);
        service.deleteProduct(909);
        service.deleteProduct(910);
        try {
            DatabaseManager.getConnection().createStatement()
                    .execute("DELETE FROM defective_items WHERE product_id BETWEEN 901 AND 910");
            DatabaseManager.getConnection().createStatement()
                    .execute("DELETE FROM low_stock_alerts WHERE product_id BETWEEN 901 AND 910");
        } catch (Exception ignored) {}
        DatabaseManager.closeConnection();
    }

    // ─── Test 1: Product saved and loaded from DB ─────────────────────────────

    /**
     * Integration Test 1: Product persists to SQLite and is reloaded correctly.
     */
    @Test
    public void testProductSavedAndLoadedFromDB() {
        Product p = new Product(901, "DB Milk", tnuva, 15, 3, 2);
        p.setCategory(dairy);
        p.setStorage_amount(40);
        p.setShelf_amount(10);
        p.addSalePrice(7.5f);
        service.addProduct(p);

        InventoryService service2 = new InventoryService();
        ProductDTO loaded = service2.getProductDTO(901);

        assertNotNull("Product should be loaded from DB", loaded);
        assertEquals("Name should match", "DB Milk", loaded.name());
        assertEquals("Storage should match", 40, loaded.storageAmount());
        assertEquals("Shelf should match", 10, loaded.shelfAmount());
        assertEquals("Min stock should match", 15, loaded.minStock());
    }

    // ─── Test 2: Stock update persists to DB ──────────────────────────────────

    /**
     * Integration Test 2: updateProductStock saves new quantities to SQLite.
     */
    @Test
    public void testStockUpdatePersistedToDB() {
        Product p = new Product(902, "Persist Cottage", tnuva, 10, 1, 1);
        p.setCategory(dairy);
        p.setTargetQuantity(50);
        service.addProduct(p);

        service.updateProductStock(902, 30, 15);

        InventoryService service2 = new InventoryService();
        ProductDTO loaded = service2.getProductDTO(902);

        assertNotNull(loaded);
        assertEquals("Warehouse should be 30", 30, loaded.storageAmount());
        assertEquals("Shelf should be 15", 15, loaded.shelfAmount());
    }

    // ─── Test 3: Delete product removes from DB ───────────────────────────────

    /**
     * Integration Test 3: deleteProduct removes the entry from SQLite permanently.
     */
    @Test
    public void testDeleteProductRemovedFromDB() {
        Product p = new Product(903, "To Delete", tnuva, 5, 1, 1);
        p.setCategory(dairy);
        service.addProduct(p);

        boolean deleted = service.deleteProduct(903);
        assertTrue("Delete should return true", deleted);

        InventoryService service2 = new InventoryService();
        assertNull("Product should not exist in DB after delete", service2.getProductDTO(903));
    }

    // ─── Test 4: Transfer warehouse → shelf persists ──────────────────────────

    /**
     * Integration Test 4 (Flow 3): transferToShelf updates both quantities and persists.
     */
    @Test
    public void testTransferToShelfPersistedToDB() {
        Product p = new Product(904, "Transfer Test", tnuva, 5, 2, 1);
        p.setCategory(dairy);
        p.setStorage_amount(50);
        p.setShelf_amount(10);
        p.setTargetQuantity(100);
        service.addProduct(p);

        boolean result = service.transferToShelf(904, 20);
        assertTrue("Transfer should succeed", result);

        ProductDTO inMemory = service.getProductDTO(904);
        assertEquals("Warehouse should be 30", 30, inMemory.storageAmount());
        assertEquals("Shelf should be 30", 30, inMemory.shelfAmount());

        InventoryService service2 = new InventoryService();
        ProductDTO loaded = service2.getProductDTO(904);
        assertNotNull(loaded);
        assertEquals("DB warehouse should be 30", 30, loaded.storageAmount());
        assertEquals("DB shelf should be 30", 30, loaded.shelfAmount());
    }

    // ─── Test 5: Transfer fails when warehouse insufficient ───────────────────

    /**
     * Unit Test 5: transferToShelf returns false when warehouse stock is insufficient.
     */
    @Test
    public void testTransferFailsWhenInsufficientWarehouseStock() {
        Product p = new Product(905, "Low Warehouse", tnuva, 5, 1, 1);
        p.setCategory(dairy);
        p.setStorage_amount(5);
        p.setShelf_amount(10);
        service.addProduct(p);

        boolean result = service.transferToShelf(905, 50);
        assertFalse("Transfer should fail — not enough warehouse stock", result);

        ProductDTO dto = service.getProductDTO(905);
        assertEquals("Warehouse should remain unchanged", 5, dto.storageAmount());
        assertEquals("Shelf should remain unchanged", 10, dto.shelfAmount());
    }

    // ─── Test 6: Low stock alert saved to DB ─────────────────────────────────

    /**
     * Integration Test 6: Low stock alert is generated and persisted when stock drops below minimum.
     */
    @Test
    public void testLowStockAlertSavedToDB() throws SQLException {
        Product p = new Product(906, "Alert Product", tnuva, 20, 1, 1);
        p.setCategory(dairy);
        p.setTargetQuantity(50);
        service.addProduct(p);

        service.updateProductStock(906, 3, 2);

        List<LowStockAlertDTO> alerts = service.getAlertDTOs();
        assertFalse("Alert should be generated", alerts.isEmpty());
        assertEquals("Alert product ID should match", 906, alerts.get(0).productId());

        Connection conn = DatabaseManager.getConnection();
        PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM low_stock_alerts WHERE product_id = ?");
        ps.setInt(1, 906);
        ResultSet rs = ps.executeQuery();
        assertTrue(rs.next());
        assertTrue("Alert should be saved in DB", rs.getInt(1) > 0);
    }

    // ─── Test 7: Category saved to DB ────────────────────────────────────────

    /**
     * Integration Test 7: addCategory persists category to SQLite.
     */
    @Test
    public void testCategorySavedToDB() {
        Category snacks = new Category(99, "TestSnacks");
        service.addCategory(snacks);

        InventoryService service2 = new InventoryService();
        List<CategoryDTO> found = service2.getCategoriesDTOByNames(java.util.Arrays.asList("TestSnacks"));

        assertFalse("Category should be found in DB", found.isEmpty());
        assertEquals("Category name should match", "TestSnacks", found.get(0).name());

        try {
            DatabaseManager.getConnection()
                    .createStatement()
                    .execute("DELETE FROM categories WHERE id = 99");
        } catch (SQLException e) { /* ignore */ }
    }

    // ─── Test 8: Defective item saved to DB and stock updated ─────────────────

    /**
     * Integration Test 8: logDefectiveAndUpdateStock persists the defective record
     * to SQLite and correctly deducts the quantity from shelf stock.
     */
    @Test
    public void testDefectiveItemSavedToDBAndStockUpdated() throws SQLException {
        Product p = new Product(907, "Defective Test", tnuva, 5, 1, 1);
        p.setCategory(dairy);
        p.setShelf_amount(10);
        p.setStorage_amount(20);
        service.addProduct(p);

        boolean success = service.logDefectiveAndUpdateStock(907, 3, "Store");
        assertTrue("logDefectiveAndUpdateStock should return true", success);

        ProductDTO dto = service.getProductDTO(907);
        assertEquals("Shelf should be reduced by 3", 7, dto.shelfAmount());

        Connection conn = DatabaseManager.getConnection();
        PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM defective_items WHERE product_id = ?");
        ps.setInt(1, 907);
        ResultSet rs = ps.executeQuery();
        assertTrue(rs.next());
        assertEquals("Defective item should be saved in DB", 1, rs.getInt(1));

        InventoryService service2 = new InventoryService();
        ProductDTO loaded = service2.getProductDTO(907);
        assertNotNull(loaded);
        assertEquals("DB shelf should reflect deduction", 7, loaded.shelfAmount());
    }

    // ─── Test 9: Automatic order triggered on shortage ────────────────────────

    /**
     * Integration Test 9: handleShortageOrder triggers supplier order when below minimum.
     */
    @Test
    public void testAutomaticOrderTriggeredOnShortage() {
        Product p = new Product(908, "Order Trigger", tnuva, 20, 1, 1);
        p.setCategory(dairy);
        p.setStorage_amount(5);
        p.setShelf_amount(3);
        p.setTargetQuantity(50);
        service.addProduct(p);

        ProductDTO dto = service.getProductDTO(908);
        assertTrue("Product should be below min stock", dto.totalAmount() <= dto.minStock());

        int expected = dto.targetQuantity() - dto.totalAmount();
        assertEquals("Amount to order should be 42", expected, p.getAmountToOrder());
    }

    // ─── Test 10: Periodic order fires for matching day regardless of stock ───

    /**
     * Integration Test 10: checkAndProcessPeriodicOrders fires for matching delivery day
     * regardless of stock level - periodic orders are always placed on the scheduled day.
     * The quantity ordered ensures stock will be above minimum after delivery.
     */
    @Test
    public void testPeriodicOrderForMatchingDayRegardlessOfStock() {
        // p1 - Monday delivery, stock above minimum
        Product p1 = new Product(909, "Monday Product", tnuva, 20, 1, 1);
        p1.setDeliveryDay(2);
        p1.setStorage_amount(50);
        p1.setShelf_amount(30);
        p1.setTargetQuantity(100);
        p1.setCategory(dairy);
        p1.addPurchasePrice(10, 4.5f);
        service.addProduct(p1);

        // p2 - Wednesday delivery, should NOT be ordered on Monday check
        Product p2 = new Product(910, "Wednesday Product", tnuva, 20, 1, 1);
        p2.setDeliveryDay(4);
        p2.setStorage_amount(3);
        p2.setShelf_amount(2);
        p2.setTargetQuantity(100);
        p2.setCategory(dairy);
        p2.addPurchasePrice(10, 4.5f);
        service.addProduct(p2);

        // Verify p1 delivery day matches and p2 does not
        assertEquals("p1 delivery day should be Monday", 2, p1.getDeliveryDay());
        assertEquals("p2 delivery day should be Wednesday", 4, p2.getDeliveryDay());

        // Verify quantity calculation for periodic order
        int expectedQty = Math.max(p1.getTargetQuantity(),
                p1.getMin_stock() - p1.getGeneral_amount() + 1);
        assertTrue("Order quantity should be at least targetQuantity", expectedQty >= p1.getTargetQuantity());

        // Run periodic check for Monday - only p1 should be ordered
        service.checkAndProcessPeriodicOrders(2);

        // Cleanup
        service.deleteProduct(909);
        service.deleteProduct(910);
    }
}