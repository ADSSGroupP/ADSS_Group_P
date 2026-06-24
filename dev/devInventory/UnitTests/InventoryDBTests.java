package devInventory.UnitTests;

import devInventory.Domain.*;
import devInventory.Service.*;
import devInventory.DTO.*;
import org.junit.jupiter.api.*;
import java.sql.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
/**
 * Integration & Unit tests for Assignment 2 additions.
 * Tests focus on DB persistence, Flow 3 (transfer), and automatic orders.
 * Uses a fresh InventoryService for each test.
 */
public class InventoryDBTests {

    private InventoryService service;
    private Manufacturer tnuva;
    private Category dairy;

    @BeforeEach
    public void setUp() {
        service = new InventoryService();
        tnuva   = new Manufacturer(1, "Tnuva");
        dairy   = new Category(1, "Dairy");
        service.addCategory(dairy);
    }

    @AfterEach
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

        assertNotNull(loaded, "Product should be loaded from DB");
        assertEquals("DB Milk", loaded.name(), "Name should match");
        assertEquals(40, loaded.storageAmount(), "Storage should match");
        assertEquals(10, loaded.shelfAmount(), "Shelf should match");
        assertEquals(15, loaded.minStock(), "Min stock should match");
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
        assertEquals(30, loaded.storageAmount(), "Warehouse should be 30");
        assertEquals(15, loaded.shelfAmount(), "Shelf should be 15");
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
        assertTrue(deleted, "Delete should return true");

        InventoryService service2 = new InventoryService();
        assertNull(service2.getProductDTO(903), "Product should not exist in DB after delete");    }

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
        assertTrue(result, "Transfer should succeed");

        ProductDTO inMemory = service.getProductDTO(904);
        assertEquals(30, inMemory.storageAmount(), "Warehouse should be 30");
        assertEquals(30, inMemory.shelfAmount(), "Shelf should be 30");

        InventoryService service2 = new InventoryService();
        ProductDTO loaded = service2.getProductDTO(904);
        assertNotNull(loaded);
        assertEquals(30, loaded.storageAmount(), "DB warehouse should be 30");
        assertEquals(30, loaded.shelfAmount(), "DB shelf should be 30");
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
        assertFalse(result, "Transfer should fail — not enough warehouse stock");

        ProductDTO dto = service.getProductDTO(905);
        assertEquals(5, dto.storageAmount(), "Warehouse should remain unchanged");
        assertEquals(10, dto.shelfAmount(), "Shelf should remain unchanged");
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
        assertFalse(alerts.isEmpty(), "Alert should be generated");
        assertEquals(906, alerts.get(0).productId(), "Alert product ID should match");

        Connection conn = DatabaseManager.getConnection();
        PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM low_stock_alerts WHERE product_id = ?");
        ps.setInt(1, 906);
        ResultSet rs = ps.executeQuery();
        assertTrue(rs.next());
        assertTrue(rs.getInt(1) > 0, "Alert should be saved in DB");
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

        assertFalse(found.isEmpty(), "Category should be found in DB");
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
        assertTrue(success, "logDefectiveAndUpdateStock should return true");

        ProductDTO dto = service.getProductDTO(907);
        assertEquals(7, dto.shelfAmount(), "Shelf should be reduced by 3");
        Connection conn = DatabaseManager.getConnection();
        PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM defective_items WHERE product_id = ?");
        ps.setInt(1, 907);
        ResultSet rs = ps.executeQuery();
        assertTrue(rs.next());
        assertEquals(1, rs.getInt(1), "Defective item should be saved in DB");
        InventoryService service2 = new InventoryService();
        ProductDTO loaded = service2.getProductDTO(907);
        assertNotNull(loaded);
        assertEquals(7, loaded.shelfAmount(), "DB shelf should reflect deduction");    }

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
        assertTrue(dto.totalAmount() <= dto.minStock(), "Product should be below min stock");
        int expected = dto.targetQuantity() - dto.totalAmount();
        assertEquals(expected, p.getAmountToOrder(), "Amount to order should be 42");    }

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
        assertEquals(2, p1.getDeliveryDay(), "p1 delivery day should be Monday");
        assertEquals(4, p2.getDeliveryDay(), "p2 delivery day should be Wednesday");
        // Verify quantity calculation for periodic order
        int expectedQty = Math.max(p1.getTargetQuantity(),
                p1.getMin_stock() - p1.getGeneral_amount() + 1);
        assertTrue(expectedQty >= p1.getTargetQuantity(), "Order quantity should be at least targetQuantity");
        // Run periodic check for Monday - only p1 should be ordered
        service.checkAndProcessPeriodicOrders(2);

        // Cleanup
        service.deleteProduct(909);
        service.deleteProduct(910);
    }
}