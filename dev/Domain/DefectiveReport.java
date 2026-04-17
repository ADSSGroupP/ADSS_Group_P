package Domain;

import java.util.List;

public class DefectiveReport extends Report {

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