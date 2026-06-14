package DTO;

/**
 * Data Transfer Object for Category.
 * Carries category display data between Service and Presentation layers.
 * No business logic, no SQL - data only.
 */
public record CategoryDTO(
        int id,
        String name,
        String parentCategoryName
) {}