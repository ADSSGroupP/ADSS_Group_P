package Domain;

import java.time.LocalDate;
import java.util.List;
/**
 * Abstract base class for all discounts in the system.
 * Handles the percentage and date-based activation logic.
 */
public abstract class Discount {
    protected int id;
    protected float discountPercent;
    protected LocalDate startDate;
    protected LocalDate endDate;

    public Discount(int id, float percent, LocalDate start, LocalDate end) {
        this.id = id;
        this.discountPercent = percent;
        this.startDate = start;
        this.endDate = end;
    }

    /**
     * Requirement: Promotions are activated automatically by start and end dates.
     * @return true if the current date is within the discount's range.
     */
    public boolean isActive() {
        LocalDate today = LocalDate.now();
        return !today.isBefore(startDate) && !today.isAfter(endDate);
    }

    public float apply(float price) {
        return price * (1 - (discountPercent / 100));
    }

    /**
     * Requirement: Check if a given product belongs to this discount.
     * Implemented differently by ProductDiscount and CategoryDiscount.
     */
    public abstract boolean isProductEligible(Product p);
}