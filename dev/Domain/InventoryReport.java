package Domain;

import java.util.List;
import java.util.Map;

public class InventoryReport extends Report {

    /**
     * Requirement: Can be filtered by categories or specific products.
     */
    public void printReport(Map<String, List<Product>> productsByCategory) {
        printHeader("FULL INVENTORY REPORT");

        for (String categoryName : productsByCategory.keySet()) {
            System.out.println("CATEGORY: " + categoryName);
            System.out.println("**************************************************");

            List<Product> products = productsByCategory.get(categoryName);
            if (products == null || products.isEmpty()) {
                System.out.println("   No products in this category.");
            } else {
                for (Product product : products) {
                    printProductLine(product);
                }
            }
            System.out.println();
        }
    }
}