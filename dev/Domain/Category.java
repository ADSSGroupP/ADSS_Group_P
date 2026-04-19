package Domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a product category in a strict tree hierarchy.
 * This ensures that a product belongs to a clear path (Category -> Sub -> Sub-Sub)
 * and prevents conflicting category assignments.
 */
public class Category {
    // --- Fields ---
    private int id;                         // Unique ID for the category
    private String name;                    // Category name (e.g., "Dairy", "Milk")
    private Category parentCategory;        // Reference to the parent (null if top-level)
    private List<Discount> categoryDiscounts; // List of discounts applied to this category

    /**
     * Constructor for a Category.
     * @param id Unique identifier.
     * @param name The name of the category.
     */
    public Category(int id, String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty.");
        }
        this.id = id;
        this.name = name;
        this.categoryDiscounts = new ArrayList<>();
        this.parentCategory = null; // Default to no parent
    }

    // --- Business Logic Methods ---

    /**
     * Adds a discount to this category.
     * According to requirements, this discount will apply to all products
     * within this category and its sub-categories.
     * @param d The discount to add.
     */
    public void addDiscount(Discount d) {
        if (d != null) {
            this.categoryDiscounts.add(d);
        }
    }

    /**
     * Retrieves all discounts directly associated with this category.
     * @return A copy of the list of discounts.
     */
    public List<Discount> getCategoryDiscounts() {
        return new ArrayList<>(this.categoryDiscounts);
    }

    // --- Getters & Setters ---

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isEmpty()) throw new IllegalArgumentException("Invalid name.");
        this.name = name;
    }

    /**
     * Requirement: Categories must follow a strict hierarchy to avoid contradictions. [cite: 6, 13]
     * Returns the parent of this category.
     */
    public Category getParentCategory() {
        return parentCategory;
    }

    /**
     * Sets the parent category.
     * Crucial for building the (Main Category -> Sub Category -> Sub-Sub Category) structure.
     * @param parent The parent category.
     */
    public void setParentCategory(Category parent) {
        // Safety check: a category cannot be its own parent
        if (parent != null && parent.getId() == this.id) {
            throw new IllegalArgumentException("A category cannot be its own parent.");
        }
        this.parentCategory = parent;
    }

    /**
     * Overriding toString to display the category name in reports.
     */
    @Override
    public String toString() {
        return this.name;
    }
}