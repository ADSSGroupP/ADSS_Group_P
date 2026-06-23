package devInventory.Domain;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Abstract base class for all discounts in the system.
 * Handles the percentage and date-based activation logic.
 */
public abstract class Discount {
    private static final AtomicInteger idCounter = new AtomicInteger(1);
    protected int id;
    protected float discountPercent;
    protected LocalDate startDate;
    protected LocalDate endDate;

    /**
     * Constructor for Discount.
     * Includes "Fail-Fast" validation to ensure dates and percentage are valid upon creation.
     */
    public Discount(int id, float percent, LocalDate start, LocalDate end) {
        LocalDate today = LocalDate.now();

        // --- Date Validation Logic ---
        if (start.isBefore(today)) {
            throw new IllegalArgumentException("Start date cannot be in the past.");
        }
        if (end.isBefore(today)) {
            throw new IllegalArgumentException("End date cannot be in the past.");
        }
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("End date cannot be before start date.");
        }
        // -----------------------------

        // --- Percentage Validation Logic ---
        if (percent < 0 || percent > 100) {
            throw new IllegalArgumentException("Discount percentage must be between 0 and 100.");
        }
        // -----------------------------------

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

    public static int getNextId() {
        return idCounter.getAndIncrement();
    }

    public int getId(){
        return this.id;
    }

    public float getDiscountPercent() { return discountPercent; }
    public LocalDate getStartDate()   { return startDate; }
    public LocalDate getEndDate()     { return endDate; }

}