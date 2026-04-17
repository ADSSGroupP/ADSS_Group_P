package Domain;

import java.time.LocalDate;

/**
 * Represents a discount promotion in the inventory system.
 * Handles discount percentages and validity periods (start and end dates).
 * Discounts are activated automatically based on the current system date.
 */
public class Discount {
    // --- Fields ---
    private int id;                 // Unique identifier for the discount
    private float discountPercent;  // The percentage to reduce from the price
    private LocalDate startDate;    // Promotion start date
    private LocalDate endDate;      // Promotion end date

    /**
     * Full Constructor for a Discount.
     * * @param id Unique ID for managing the discount.
     * @param percent Percentage of discount (0.0 to 100.0).
     * @param start The date the discount becomes active.
     * @param end The date the discount expires.
     * @throws IllegalArgumentException if dates are inconsistent or percent is out of range.
     */
    public Discount(int id, float percent, LocalDate start, LocalDate end) {
        if (percent < 0 || percent > 100) {
            throw new IllegalArgumentException("Discount percentage must be between 0 and 100.");
        }
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("End date cannot be before start date.");
        }

        this.id = id;
        this.discountPercent = percent;
        this.startDate = start;
        this.endDate = end;
    }


    /**
     * Requirement: Promotions are activated automatically by start and end dates.
     * This method checks if today's date falls within the discount period.
     * * @return true if the discount is currently active, false otherwise.
     */
    public boolean isActive() {
        LocalDate today = LocalDate.now();
        // The discount is active if today is not before the start and not after the end.
        return !today.isBefore(startDate) && !today.isAfter(endDate);
    }

    /**
     * Requirement: Final price calculation after applying the discount.
     * * @param originalPrice The base price before discount.
     * @return The price after the discount percentage is applied.
     */
    public float applyDiscount(float originalPrice) {
        if (originalPrice < 0) return 0;
        return originalPrice * (1 - (this.discountPercent / 100));
    }

    // --- Getters & Setters ---

    public int getId() {
        return id;
    }

    public float getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(float percent) {
        if (percent < 0 || percent > 100) throw new IllegalArgumentException("Invalid percentage.");
        this.discountPercent = percent;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        if (endDate.isBefore(this.startDate)) throw new IllegalArgumentException("End date cannot be before start date.");
        this.endDate = endDate;
    }

    /**
     * Utility method to print discount details for reports.
     */
    public void printDiscountSummary() {
        System.out.println("Discount ID: " + id + " | Percent: " + discountPercent + "%");
        System.out.println("Valid from: " + startDate + " to: " + endDate);
    }
}