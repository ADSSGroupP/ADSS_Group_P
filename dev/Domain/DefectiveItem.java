package Domain;

public class DefectiveItem {
    private final Product product;
    private final int defectiveQuantity;
    private final String defectiveLocation;

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

    public Product getProduct() {
        return product;
    }

    public int getDefectiveQuantity() {
        return defectiveQuantity;
    }

    public String getDefectiveLocation() {
        return defectiveLocation;
    }

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