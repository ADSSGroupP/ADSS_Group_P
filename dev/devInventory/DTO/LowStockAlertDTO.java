package devInventory.DTO;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for LowStockAlert.
 * Carries alert display data between Service and Presentation layers.
 * No business logic, no SQL - data only.
 */
public record LowStockAlertDTO(
        int productId,
        String productName,
        int currentStock,
        int minStock,
        LocalDateTime generatedAt
) {}