package devInventory.UnitTests;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import Domain.*;

/**
 * Unit tests for the Domain layer.
 * Covers discounts, stock management, defective items, and general domain logic.
 */
public class InventoryDomainTests {
    private Product product;
    private Category dairy;
    private Manufacturer manu;
    private List<Discount> systemDiscounts;

    @Before
    public void setUp() {
        manu    = new Manufacturer(1, "Tnuva");
        dairy   = new Category(1, "Dairy");
        product = new Product(101, "Milk 3%", manu, 10, 2, 5);
        product.setCategory(dairy);
        systemDiscounts = new ArrayList<>();
    }

    // ─── Discount & Price Tests ───────────────────────────────────────────────

    /**
     * Verifies that the best (highest) discount is selected when multiple apply.
     */
    @Test
    public void testBestDiscountSelection() {
        product.addSalePrice(10.0f);
        Discount tenPercent    = new ProductDiscount(1, 10, LocalDate.now(), LocalDate.now().plusDays(5), Arrays.asList(product));
        Discount twentyPercent = new ProductDiscount(2, 20, LocalDate.now(), LocalDate.now().plusDays(5), Arrays.asList(product));
        systemDiscounts.add(tenPercent);
        systemDiscounts.add(twentyPercent);

        float result = product.updateAndGetCurrentBestPrice(systemDiscounts);
        assertEquals("Should apply 20% (best price).", 8.0f, result, 0.001);
    }

    /**
     * Verifies that an inactive discount (future start date) is ignored when calculating price.
     * Uses a discount that starts tomorrow so isActive() returns false today.
     */
    @Test
    public void testInactiveDiscountIgnored() {
        product.addSalePrice(10.0f);
        // Discount starts tomorrow — not yet active, so it must be ignored
        Discount future = new ProductDiscount(1, 50,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(5),
                Arrays.asList(product));
        systemDiscounts.add(future);

        float result = product.updateAndGetCurrentBestPrice(systemDiscounts);
        assertEquals("Inactive (future) discount must be ignored.", 10.0f, result, 0.001);
    }

    /**
     * Verifies that a category discount is inherited by all products in that category.
     */
    @Test
    public void testCategoryDiscountInheritance() {
        product.addSalePrice(100.0f);
        Discount catDiscount = new CategoryDiscount(3, 30, LocalDate.now(), LocalDate.now().plusDays(1), Arrays.asList(dairy));
        systemDiscounts.add(catDiscount);

        float result = product.updateAndGetCurrentBestPrice(systemDiscounts);
        assertEquals("Should inherit 30% from category.", 70.0f, result, 0.001);
    }

    // ─── Stock & Shortage Tests ───────────────────────────────────────────────

    /**
     * Verifies that a shortage is detected when total stock is below minimum.
     */
    @Test
    public void testShortageDetectionTrue() {
        product.setStorage_amount(2);
        product.setShelf_amount(2); // Total 4 < Min 10
        assertTrue("Shortage should be detected.", product.isBelowMinStock());
    }

    /**
     * Verifies that no shortage is reported when total stock exceeds minimum.
     */
    @Test
    public void testShortageDetectionFalse() {
        product.setStorage_amount(10);
        product.setShelf_amount(5); // Total 15 > Min 10
        assertFalse("No shortage should be detected.", product.isBelowMinStock());
    }

    /**
     * Verifies that setting a negative stock amount throws an exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testNegativeStockThrowsException() {
        product.setStorage_amount(-5);
    }

    // ─── Defective Items Tests ────────────────────────────────────────────────

    /**
     * Verifies that a defective quantity within storage stock is considered valid.
     */
    @Test
    public void testDefectiveItemValidStorage() {
        product.setStorage_amount(10);
        DefectiveItem item = new DefectiveItem(product, 5, "Storage");
        assertTrue("Should be valid (5 <= 10).", item.isValidQuantity());
    }

    /**
     * Verifies that a defective quantity exceeding shelf stock is considered invalid.
     */
    @Test
    public void testDefectiveItemInvalidStore() {
        product.setShelf_amount(3);
        DefectiveItem item = new DefectiveItem(product, 10, "Store");
        assertFalse("Should be invalid (10 > 3).", item.isValidQuantity());
    }

    /**
     * Verifies that creating a DefectiveItem with a null product throws an exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testDefectiveItemNullProductThrows() {
        new DefectiveItem(null, 5, "Storage");
    }

    // ─── General Domain Logic Tests ───────────────────────────────────────────

    /**
     * Verifies that total stock is the sum of storage and shelf amounts.
     */
    @Test
    public void testTotalAmountCalculation() {
        product.setStorage_amount(100);
        product.setShelf_amount(50);
        assertEquals("Total must be sum of shelf and storage.", 150, product.getGeneral_amount());
    }
}