package UnitTests;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import Domain.*;
import Service.*;

/**
 * Updated Unit tests to support Abstract Discount structure and
 * hierarchical price calculations.
 */
public class InventoryDomainTests {
    private Product product;
    private Category dairy;
    private List<Discount> systemDiscounts;

    @Before
    public void setUp() {
        Manufacturer manu = new Manufacturer(1, "Tnuva");
        dairy = new Category(1, "Dairy");
        // SKU 101, Min Stock 10
        product = new Product(101, "Milk 3%", manu, 10, 2, 5);
        product.setCategory(dairy);
        systemDiscounts = new ArrayList<>();
    }

    /**
     * Requirement 9: Verifies best discount logic using the new polymorphism structure.
     */
    @Test
    public void testBestDiscountSelection() {
        product.addSalePrice(10.0f); // Base price

        // Creating specific product discounts instead of abstract Discount
        Discount tenPercent = new ProductDiscount(1, 10, LocalDate.now(), LocalDate.now().plusDays(5), Arrays.asList(product));
        Discount twentyPercent = new ProductDiscount(2, 20, LocalDate.now(), LocalDate.now().plusDays(5), Arrays.asList(product));

        systemDiscounts.add(tenPercent);
        systemDiscounts.add(twentyPercent);

        float result = product.updateAndGetCurrentBestPrice(systemDiscounts);
        assertEquals("System must apply the most profitable discount (20%).", 8.0f, result, 0.001);
    }

    /**
     * Requirement: Verifies that expired discounts are ignored.
     */
    @Test
    public void testExpiredDiscountNotApplied() {
        product.addSalePrice(10.0f);
        LocalDate past = LocalDate.now().minusDays(10);
        Discount expired = new ProductDiscount(1, 50, past, past.plusDays(2), Arrays.asList(product));

        systemDiscounts.add(expired);

        float result = product.updateAndGetCurrentBestPrice(systemDiscounts);
        assertEquals("Expired discounts must not affect the price.", 10.0f, result, 0.001);
    }

    /**
     * Requirement: Verifies hierarchical category discounts.
     */
    @Test
    public void testCategoryDiscountEffect() {
        product.addSalePrice(100.0f);
        // Discount on the Dairy category
        Discount catDiscount = new CategoryDiscount(3, 30, LocalDate.now(), LocalDate.now().plusDays(1), Arrays.asList(dairy));

        systemDiscounts.add(catDiscount);

        float result = product.updateAndGetCurrentBestPrice(systemDiscounts);
        assertEquals("Product should inherit the 30% discount from its category.", 70.0f, result, 0.001);
    }

    @Test
    public void testShortageDetection() {
        product.setStorage_amount(2);
        product.setShelf_amount(2);
        assertTrue("Should detect shortage alert.", product.isBelowMinStock());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeStockThrowsException() {
        product.setStorage_amount(-5);
    }
}