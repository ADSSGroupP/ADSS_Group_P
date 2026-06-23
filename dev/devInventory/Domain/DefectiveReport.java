package Domain;

import java.util.List;

/**
 * Represents a specialized report for listing defective and expired items in the system.
 * Inherits the core reporting functionality from the Report class.
 */
public class DefectiveReport extends Report {

    /**
     * Generates and prints the report to the console based on a list of defective items.
     * * @param defectiveItems The list of {@link DefectiveItem} objects to be displayed.
     * If the list is empty, a specific "no items" message is printed.
     */
    public void printReport(List<DefectiveItem> defectiveItems) {
        printHeader("DEFECTIVE & EXPIRED ITEMS REPORT");

        if (defectiveItems.isEmpty()) {
            System.out.println("No defective items reported.");
            return;
        }

        for (DefectiveItem item : defectiveItems) {
            Product p = item.getProduct();
            System.out.println("Product: " + p.getName() + " | SKU: " + p.getId());
            System.out.println("Defective Quantity: " + item.getDefectiveQuantity());

            String location = item.getDefectiveLocation();
            if (location.equalsIgnoreCase("Store")) {
                System.out.println("Found in: Store - Aisle " + p.getAisle() + ", Shelf " + p.getShelf());
            } else {
                System.out.println("Found in: " + location);
            }
            System.out.println("--------------------------------------------------");
        }
    }
}