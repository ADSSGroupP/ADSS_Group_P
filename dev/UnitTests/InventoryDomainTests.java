package UnitTests;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import Domain.*;

/**
 * Unit tests for the Domain layer
 */
public class InventoryDomainTests {
    private Product product;
    private Category dairy;
    private Manufacturer manu;
    private List<Discount> systemDiscounts;

    @Before
    public void setUp() {
        manu = new Manufacturer(1, "Tnuva");
        dairy = new Category(1, "Dairy");
        // SKU 101, Min Stock 10, Aisle 2, Shelf 5
        product = new Product(101, "Milk 3%", manu, 10, 2, 5);
        product.setCategory(dairy);
        systemDiscounts = new ArrayList<>();
    }

    // --- Discount & Price Tests (Requirement 9) ---

    @Test
    public void testBestDiscountSelection() {
        product.addSalePrice(10.0f);
        Discount tenPercent = new ProductDiscount(1, 10, LocalDate.now(), LocalDate.now().plusDays(5), Arrays.asList(product));
        Discount twentyPercent = new ProductDiscount(2, 20, LocalDate.now(), LocalDate.now().plusDays(5), Arrays.asList(product));
        systemDiscounts.add(tenPercent);
        systemDiscounts.add(twentyPercent);

        float result = product.updateAndGetCurrentBestPrice(systemDiscounts);
        assertEquals("Should apply 20% (best price).", 8.0f, result, 0.001);
    }

    @Test
    public void testExpiredDiscountIgnored() {
        product.addSalePrice(10.0f);
        LocalDate past = LocalDate.now().minusDays(10);
        Discount expired = new ProductDiscount(1, 50, past, past.plusDays(2), Arrays.asList(product));
        systemDiscounts.add(expired);

        float result = product.updateAndGetCurrentBestPrice(systemDiscounts);
        assertEquals("Expired discount must be ignored.", 10.0f, result, 0.001);
    }

    @Test
    public void testCategoryDiscountInheritance() {
        product.addSalePrice(100.0f);
        Discount catDiscount = new CategoryDiscount(3, 30, LocalDate.now(), LocalDate.now().plusDays(1), Arrays.asList(dairy));
        systemDiscounts.add(catDiscount);

        float result = product.updateAndGetCurrentBestPrice(systemDiscounts);
        assertEquals("Should inherit 30% from category.", 70.0f, result, 0.001);
    }

    // --- Stock & Shortage Tests ---

    @Test
    public void testShortageDetectionTrue() {
        product.setStorage_amount(2);
        product.setShelf_amount(2); // Total 4 < Min 10
        assertTrue("Shortage should be detected.", product.isBelowMinStock());
    }

    @Test
    public void testShortageDetectionFalse() {
        product.setStorage_amount(10);
        product.setShelf_amount(5); // Total 15 > Min 10
        assertFalse("No shortage should be detected.", product.isBelowMinStock());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeStockThrowsException() {
        product.setStorage_amount(-5); // Boundary test
    }

    // --- Defective Items Tests ---

    @Test
    public void testDefectiveItemValidStorage() {
        product.setStorage_amount(10);
        DefectiveItem item = new DefectiveItem(product, 5, "Storage");
        assertTrue("Should be valid (5 <= 10).", item.isValidQuantity());
    }

    @Test
    public void testDefectiveItemInvalidStore() {
        product.setShelf_amount(3);
        DefectiveItem item = new DefectiveItem(product, 10, "Store");
        assertFalse("Should be invalid (10 > 3).", item.isValidQuantity());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDefectiveItemNullProductThrows() {
        new DefectiveItem(null, 5, "Storage");
    }

    // --- General Domain Logic Tests ---

    @Test
    public void testTotalAmountCalculation() {
        product.setStorage_amount(100);
        product.setShelf_amount(50);
        assertEquals("Total must be sum of shelf and storage.", 150, product.getGeneral_amount());
    }
}