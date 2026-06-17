package Domain;

/**
 * Represents a manufacturer of products in the inventory system.
 */
public class Manufacturer {
    private String name;
    private int id;

    /**
     * Constructor for Manufacturer.
     * @param id Unique identifier for the manufacturer.
     * @param name The name of the manufacturer.
     */
    public Manufacturer(int id, String name) {
        // Validation: Manufacturer name cannot be empty [cite: 5]
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Manufacturer name is required");
        }
        // Validation: ID must be a positive number
        if (id < 0) {
            throw new IllegalArgumentException("ID must be positive");
        }

        this.id = id;
        this.name = name;
    }

    // --- Getters ---

    public int getId() { return id; }
    public String getName() { return name; }
}