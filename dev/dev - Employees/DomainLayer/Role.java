package DomainLayer;

public enum Role {
    CASHIER("Cashier"),
    STOREKEEPER("storeKeeper"),
    DRIVER("Driver"),
    SHIFTMANAGER("shiftManager");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
