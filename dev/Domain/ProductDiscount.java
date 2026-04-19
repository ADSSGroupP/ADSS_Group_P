package Domain;

import java.time.LocalDate;
import java.util.List;

/**
 * Implementation for discounts that target a specific list of products.
 */
public class ProductDiscount extends Discount {
    private List<Product> eligibleProducts;

    public ProductDiscount(int id, float percent, LocalDate start, LocalDate end, List<Product> products) {
        super(id, percent, start, end);
        this.eligibleProducts = products;
    }

    @Override
    public boolean isProductEligible(Product p) {
        return eligibleProducts != null && eligibleProducts.contains(p);
    }
}