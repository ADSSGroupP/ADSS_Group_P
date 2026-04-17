package UnitTests;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import java.time.LocalDate;

/**
 * Unit tests for the Domain layer using JUnit 4.
 * These tests verify core business logic including stock validation,
 * discount calculations, and hierarchical category management.
 */
public class InventoryDomainTests {
    private Product product;
    private Category dairy;

    /**
     * Sets up the test environment before each test case.
     * Uses the @Before annotation compatible with JUnit 4.
     */
    @Before
    public void setUp() {
        Manufacturer manu = new Manufacturer(1, "Tnuva");
        dairy = new Category(1, "Dairy");
        // Initializing product with SKU 101, Min Stock 10, Aisle 2, Shelf 5
        product = new Product(101, "Milk 3%", manu, 10, 2, 5);
        product.setSub_sub_category(dairy);
    }

    /**
     * Requirement 1 & 3: Verifies shortage detection.
     */
    @Test
    public void testShortageDetection() {
        product.setStorage_amount(2);
        product.setShelf_amount(2);
        assertTrue("Should detect shortage alert.", product.getGeneral_amount() <= product.getMin_stock());
    }

    @Test
    public void testNoShortageWhenStockIsHigh() {
        product.setStorage_amount(20);
        assertFalse("Should not flag shortage.", product.getGeneral_amount() <= product.getMin_stock());
    }

    /**
     * Requirement 9: Verifies best discount logic.
     */
    @Test
    public void testBestDiscountSelection() {
        product.addSalePrice(10.0f);
        Discount tenPercent = new Discount(1, 10, LocalDate.now(), LocalDate.now().plusDays(5));
        Discount twentyPercent = new Discount(2, 20, LocalDate.now(), LocalDate.now().plusDays(5));
        product.addSpecificDiscount(tenPercent);
        product.addSpecificDiscount(twentyPercent);

        assertEquals("System must apply the 20% discount.", 8.0f, product.getBestPrice(), 0.001);
    }

    @Test
    public void testExpiredDiscountNotApplied() {
        product.addSalePrice(10.0f);
        LocalDate past = LocalDate.now().minusDays(10);
        Discount expired = new Discount(1, 50, past, past.plusDays(2));
        product.addSpecificDiscount(expired);

        assertEquals("Expired discounts must be ignored.", 10.0f, product.getBestPrice(), 0.001);
    }

    @Test
    public void testCategoryHierarchyAssignment() {
        assertEquals("Dairy", product.getSub_sub_category().getName());
    }

    /**
     * Testing Exception: In JUnit 4, we use the 'expected' parameter in @Test.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testNegativeStockThrowsException() {
        product.setStorage_amount(-5);
    }

    @Test
    public void testDefectiveItemCreation() {
        DefectiveItem item = new DefectiveItem(product, 5, "Store");
        assertEquals(5, item.getDefectiveQuantity());
        assertEquals("Store", item.getDefectiveLocation());
    }

    @Test
    public void testProductLocationData() {
        assertEquals(2, product.getAisle());
        assertEquals(5, product.getShelf());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testManufacturerValidation() {
        new Manufacturer(2, "");
    }

    @Test
    public void testTotalAmountCalculation() {
        product.setStorage_amount(10);
        product.setShelf_amount(5);
        assertEquals(15, product.getGeneral_amount());
    }
}