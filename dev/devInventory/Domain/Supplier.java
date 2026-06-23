package devInventory.Domain;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a supplier in the system.
 * Since this is a dummy implementation, default values are used for
 * fields that would normally come from the Suppliers module.
 */
public class Supplier {

    /** Unique identifier for the supplier. */
    private final int id;

    /** Display name of the supplier. */
    private final String name;

    /** Supplier's address (dummy value). */
    private final String address;

    /** Contact phone number (dummy value). */
    private final String contactPhone;

    /**
     * Static dummy registry of known suppliers.
     * In a real system this would come from the Suppliers module DB.
     */
    private static final Map<Integer, Supplier> KNOWN_SUPPLIERS = new HashMap<>();

    static {
        KNOWN_SUPPLIERS.put(10, new Supplier(10, "Tnuva Supplies Ltd.", "12 Dairy St, Tel Aviv", "03-1234567"));
        KNOWN_SUPPLIERS.put(20, new Supplier(20, "Osem Distribution", "5 Snack Ave, Jerusalem", "02-7654321"));
        KNOWN_SUPPLIERS.put(30, new Supplier(30, "FreshCo Wholesale", "8 Fresh Blvd, Haifa", "04-9876543"));
    }

    /**
     * Constructs a Supplier with all fields.
     *
     * @param id           Unique supplier ID.
     * @param name         Supplier name.
     * @param address      Supplier address.
     * @param contactPhone Contact phone number.
     */
    public Supplier(int id, String name, String address, String contactPhone) {
        this.id           = id;
        this.name         = name;
        this.address      = address;
        this.contactPhone = contactPhone;
    }

    /**
     * Returns a Supplier by ID from the known suppliers registry.
     * If not found, returns a default dummy supplier.
     *
     * @param supplierId The supplier ID to look up.
     * @return The matching Supplier, or a default dummy if not found.
     */
    public static Supplier getById(int supplierId) {
        return KNOWN_SUPPLIERS.getOrDefault(supplierId,
                new Supplier(supplierId, "Supplier #" + supplierId, "N/A", "N/A"));
    }

    public int getId()           { return id; }
    public String getName()      { return name; }
    public String getAddress()   { return address; }
    public String getContactPhone() { return contactPhone; }
}