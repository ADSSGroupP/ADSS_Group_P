package Domain;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Abstract base for all reports.
 * Requirement: Includes product name, SKU (ID), quantity, and detailed location.
 */
public abstract class Report {
    private final LocalDateTime date;

    public Report() {
        this.date = LocalDateTime.now();
    }

    protected void printHeader(String reportType) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        System.out.println("==================================================");
        System.out.println("REPORT: " + reportType);
        System.out.println("DATE: " + date.format(formatter));
        System.out.println("==================================================");
    }

    /**
     * Requirement: Each line must include Name, SKU (ID), Location, and Quantity.
     */
    public void printProductLine(Product product) {
        System.out.println(
                "Product: " + product.getName() +
                        " | SKU: " + product.getId() +
                        " | Total Qty: " + product.getGeneral_amount() +
                        "\n   Location Details: " + getDetailedLocation(product)
        );
        System.out.println("--------------------------------------------------");
    }

    private String getDetailedLocation(Product product) {
        StringBuilder loc = new StringBuilder();
        if (product.getStorage_amount() > 0) {
            loc.append("Storage (").append(product.getStorage_amount()).append(")");
        }
        if (product.getShelf_amount() > 0) {
            if (loc.length() > 0) loc.append(", ");
            loc.append("Store - Aisle ").append(product.getAisle())
                    .append(", Shelf ").append(product.getShelf())
                    .append(" (").append(product.getShelf_amount()).append(")");
        }
        return loc.length() == 0 ? "No stock available" : loc.toString();
    }
}