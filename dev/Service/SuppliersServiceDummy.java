package Service;

import Domain.Product;
import Domain.Supplier;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Dummy implementation simulating the Suppliers Module.
 *
 * <p>This class represents the integration point between the Inventory module
 * and the Suppliers module. It uses real data from the Inventory module
 * (via {@link IntegrationDummyFunctions}) to:
 * <ul>
 *   <li>Find the cheapest supplier for a given product and quantity</li>
 *   <li>Create automatic orders when stock is low</li>
 *   <li>Create periodic orders based on delivery schedules</li>
 * </ul>
 * </p>
 */
public class SuppliersServiceDummy {

    /** Auto-incrementing order number counter. */
    private static final AtomicInteger orderCounter = new AtomicInteger(1000);

    /**
     * Finds the best (cheapest) supplier for a given product and quantity.
     * Uses the product's real supplierCosts map from the Inventory module.
     *
     * @param product  The product to order.
     * @param quantity The quantity needed.
     * @return The supplier ID with the lowest unit cost, or -1 if none found.
     */
    public static int findBestSupplier(Product product, int quantity) {
        Map<Integer, ArrayList<Float>> supplierCosts = product.getSupplierCosts();

        if (supplierCosts == null || supplierCosts.isEmpty()) {
            System.out.println("[SUPPLIERS] No supplier data found for product: " + product.getName());
            return -1;
        }

        int bestSupplierId = -1;
        float bestPrice    = Float.MAX_VALUE;

        for (Map.Entry<Integer, ArrayList<Float>> entry : supplierCosts.entrySet()) {
            int supplierId     = entry.getKey();
            List<Float> prices = entry.getValue();
            if (prices == null || prices.isEmpty()) continue;

            float latestPrice = prices.get(prices.size() - 1);
            if (latestPrice < bestPrice) {
                bestPrice      = latestPrice;
                bestSupplierId = supplierId;
            }
        }
        return bestSupplierId;
    }

    /**
     * Returns the unit price for the best supplier of a given product.
     *
     * @param product  The product being ordered.
     * @param quantity The quantity.
     * @return The unit cost from the cheapest supplier, or 0.0 if none found.
     */
    public static float getBestSupplierPrice(Product product, int quantity) {
        Map<Integer, ArrayList<Float>> supplierCosts = product.getSupplierCosts();
        if (supplierCosts == null || supplierCosts.isEmpty()) return 0.0f;

        float bestPrice = Float.MAX_VALUE;
        for (ArrayList<Float> prices : supplierCosts.values()) {
            if (prices == null || prices.isEmpty()) continue;
            float latest = prices.get(prices.size() - 1);
            if (latest < bestPrice) bestPrice = latest;
        }
        return bestPrice == Float.MAX_VALUE ? 0.0f : bestPrice;
    }

    /**
     * Creates an automatic purchase order and prints it in the formal order format.
     *
     * @param product The product domain object from the Inventory module.
     * @param qty     The quantity to order.
     */
    public static void createAutomaticOrder(Product product, int qty) {
        if (qty <= 0) {
            System.out.println("[SUPPLIERS] No order needed for: " + product.getName());
            return;
        }

        int bestSupplierId = findBestSupplier(product, qty);
        float unitPrice    = getBestSupplierPrice(product, qty);
        float discount     = 0.0f;
        float finalPrice   = unitPrice * (1 - discount / 100);
        float totalCost    = finalPrice * qty;
        int orderNumber    = orderCounter.getAndIncrement();
        Supplier supplier  = Supplier.getById(bestSupplierId);

        printOrderForm(orderNumber, supplier, product, qty, unitPrice, discount, finalPrice, totalCost);
    }

    /**
     * Prints the formal order form in the standard format.
     *
     * @param orderNumber Order number.
     * @param supplier    The selected supplier.
     * @param product     The product being ordered.
     * @param qty         Quantity ordered.
     * @param unitPrice   Unit price from supplier.
     * @param discount    Discount percentage.
     * @param finalPrice  Final unit price after discount.
     * @param totalCost   Total order cost.
     */
    private static void printOrderForm(int orderNumber, Supplier supplier, Product product,
                                       int qty, float unitPrice, float discount,
                                       float finalPrice, float totalCost) {
        System.out.println("\n========================================");
        System.out.println("           SUPPLIER ORDER FORM          ");
        System.out.println("========================================");
        System.out.printf("Supplier Name : %-20s Order No. : %d%n", supplier.getName(), orderNumber);
        System.out.printf("Address       : %-20s Order Date: %s%n", supplier.getAddress(), LocalDate.now());
        System.out.printf("Supplier ID   : %-20d Contact   : %s%n", supplier.getId(), supplier.getContactPhone());
        System.out.println("----------------------------------------");
        System.out.printf("%-20s %-8s %-12s %-10s %-12s%n",
                "Product Name", "Qty", "Unit Price", "Discount", "Final Price");
        System.out.println("----------------------------------------");
        System.out.printf("%-20s %-8d %-12.2f %-10.1f%% %-12.2f%n",
                product.getName(), qty, unitPrice, discount, finalPrice * qty);
        System.out.println("----------------------------------------");
        System.out.printf("Total Cost: %.2f NIS%n", totalCost);
        System.out.println("========================================\n");
    }

    /**
     * Creates a periodic order for a product scheduled for delivery tomorrow.
     * Only places the order if the product is currently below minimum stock.
     *
     * @param product The product to order periodically.
     */
    public static void createPeriodicOrder(Product product) {
        if (!product.isBelowMinStock()) {
            System.out.println("[SUPPLIERS] Stock sufficient for: " + product.getName() + " — no periodic order needed.");
            return;
        }
        int qty = product.getAmountToOrder();
        System.out.println("\n--- [SUPPLIERS MODULE: PERIODIC ORDER] ---");
        System.out.println("Product     : " + product.getName() + " | SKU: " + product.getId());
        System.out.println("Delivery Day: " + product.getDeliveryDay());
        createAutomaticOrder(product, qty);
    }
}