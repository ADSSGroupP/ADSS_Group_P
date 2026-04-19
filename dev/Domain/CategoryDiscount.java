package Domain;

import java.time.LocalDate;
import java.util.List;

/**
 * Implementation for discounts that target a specific list of categories.
 * Includes sub-categories and sub-sub-categories in the check.
 */
public class CategoryDiscount extends Discount {
    private List<Category> eligibleCategories;

    public CategoryDiscount(int id, float percent, LocalDate start, LocalDate end, List<Category> categories) {
        super(id, percent, start, end);
        this.eligibleCategories = categories;
    }

    @Override
    public boolean isProductEligible(Product p) {
        if (eligibleCategories == null) return false;
        // Requirement: Check hierarchy (Category, Sub-Category, Sub-Sub-Category)
        return eligibleCategories.contains(p.getCategory()) ||
                eligibleCategories.contains(p.getSub_category()) ||
                eligibleCategories.contains(p.getSub_sub_category());
    }
}
