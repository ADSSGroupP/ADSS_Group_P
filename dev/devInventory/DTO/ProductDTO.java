package devInventory.DTO;

/**
 * Data Transfer Object for Product.
 * Carries product display data between Service and Presentation layers.
 * No business logic, no SQL - data only.
 */
public record ProductDTO(
        int id,
        String name,
        int minStock,
        int storageAmount,
        int shelfAmount,
        int totalAmount,
        int aisle,
        int shelf,
        int deliveryDay,
        int targetQuantity,
        float basePrice,
        String categoryName,
        String subCategoryName,
        String subSubCategoryName,
        String manufacturerName
) {}