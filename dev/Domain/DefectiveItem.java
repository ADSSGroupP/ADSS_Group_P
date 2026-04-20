package Domain;

/**
 * Represents an item identified as defective within the inventory.
 * This class tracks the product, quantity, and specific location of the defect.
 */
public class DefectiveItem {
    /** The product that is defective. */
    private final Product product;

    /** The quantity of the product that is defective. */
    private final int defectiveQuantity;

    /** The location where the defective item was found (e.g., "storage", "store"). */
    private final String defectiveLocation;

    /**
     * Constructs a new DefectiveItem entry.
     * * @param product The product in question.
     * @param defectiveQuantity Must be a positive integer.
     * @param defectiveLocation Cannot be null or empty.
     * @throws IllegalArgumentException if validation fails.
     */
    public DefectiveItem(Product product, int defectiveQuantity, String defectiveLocation) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        if (defectiveQuantity <= 0) {
            throw new IllegalArgumentException("Defective quantity must be positive");
        }
        if (defectiveLocation == null || defectiveLocation.isEmpty()) {
            throw new IllegalArgumentException("Defective location cannot be empty");
        }

        this.product = product;
        this.defectiveQuantity = defectiveQuantity;
        this.defectiveLocation = defectiveLocation;
    }

    /** @return the associated product. */
    public Product getProduct() {
        return product;
    }

    /** @return the quantity of defective units. */
    public int getDefectiveQuantity() {
        return defectiveQuantity;
    }

    /** @return the location string. */
    public String getDefectiveLocation() {
        return defectiveLocation;
    }

    /**
     * Validates if the defective quantity exists within the current stock levels
     * of the specified location.
     * * @return true if quantity is within available stock, false otherwise.
     */
    public boolean isValidQuantity() {
        if (defectiveLocation.equalsIgnoreCase("storage")) {
            return defectiveQuantity <= product.getStorage_amount();
        }

        if (defectiveLocation.equalsIgnoreCase("store")) {
            return defectiveQuantity <= product.getShelf_amount();
        }

        return false;
    }
}