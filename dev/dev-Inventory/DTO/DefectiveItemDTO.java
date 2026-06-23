package DTO;

/**
 * Data Transfer Object for DefectiveItem.
 * Carries defective item display data between Service and Presentation layers.
 * No business logic, no SQL - data only.
 */
public record DefectiveItemDTO(
        int productId,
        String productName,
        int defectiveQuantity,
        String defectiveLocation
) {}