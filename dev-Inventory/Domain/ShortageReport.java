package Domain;

import java.util.List;

public class ShortageReport extends Report {

    public void printReport(List<Product> shortageProducts) {
        printHeader("SHORTAGE ALERT REPORT");

        if (shortageProducts.isEmpty()) {
            System.out.println("Perfect! No shortages detected.");
            return;
        }

        for (Product product : shortageProducts) {
            printProductLine(product);
            System.out.println("   >>> ALERT: Current Stock (" + product.getGeneral_amount() +
                    ") is below Minimum (" + product.getMin_stock() + ")");
            System.out.println("--------------------------------------------------");
        }
    }
}