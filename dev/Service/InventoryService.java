package Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import Domain.*;
import DTO.*;

public class InventoryService {

    private Map<Integer, Product>  products;
    private Map<Integer, Category> categories;
    private List<DefectiveItem>    defectiveItems;
    private List<Discount>         systemDiscounts;
    private List<LowStockAlert>    alerts;

    private final Domain.IInventoryRepository repo;

    public InventoryService() {
        this.repo = new InventoryRepository();
        this.systemDiscounts = new ArrayList<>();
        this.alerts          = new ArrayList<>();
        loadFromDatabase();
    }

    private void loadFromDatabase() {
        this.categories     = repo.loadAllCategories();
        this.products       = repo.loadAllProducts(categories);
        this.defectiveItems = repo.loadAllDefectiveItems(products);
        System.out.println("[DB] Loaded " + products.size() + " products, " +
                categories.size() + " categories, " +
                defectiveItems.size() + " defective items.");
    }

    // ─── Discount Management ──────────────────────────────────────────────────

    public void applyDiscountToSystem(Discount d) {
        systemDiscounts.add(d);
        repo.saveDiscount(d);
        for (Product p : products.values()) {
            if (d.isProductEligible(p)) p.addSpecificDiscount(d.getId());
        }
        if (d instanceof CategoryDiscount) {
            for (Category c : categories.values()) {
                if (isCategoryEligible(c, (CategoryDiscount) d)) c.addDiscount(d.getId());
            }
        }
    }

    /**
     * Creates and applies a product discount from a list of SKUs.
     * Returns list of SKUs that were not found.
     */
    public List<Integer> applyProductDiscountBySkus(float pct, LocalDate start, LocalDate end, List<Integer> skus) {
        List<Integer> notFound = skus.stream()
                .filter(s -> !products.containsKey(s))
                .collect(Collectors.toList());
        List<Product> found = skus.stream()
                .map(products::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (!found.isEmpty()) {
            int newId = Discount.getNextId();
            applyDiscountToSystem(new ProductDiscount(newId, pct, start, end, found));
        }
        return notFound;
    }

    /**
     * Creates and applies a category discount from a list of category names.
     * Returns list of names that were not found.
     */
    public List<String> applyCategoryDiscountByNames(float pct, LocalDate start, LocalDate end, List<String> names) {
        List<String> notFound = names.stream()
                .filter(n -> getCategoriesByNames(Collections.singletonList(n)).isEmpty())
                .collect(Collectors.toList());
        List<Category> found = getCategoriesByNames(names);
        if (!found.isEmpty()) {
            int newId = Discount.getNextId();
            applyDiscountToSystem(new CategoryDiscount(newId, pct, start, end, found));
        }
        return notFound;
    }

    private boolean isCategoryEligible(Category c, CategoryDiscount cd) {
        Product dummy = new Product(-1, "dummy", new Manufacturer(-1, "m"), 0, 0, 0);
        dummy.setCategory(c);
        return cd.isProductEligible(dummy);
    }

    public float getBestPriceForProductId(int id) {
        Product p = products.get(id);
        if (p == null) return 0;
        return p.updateAndGetCurrentBestPrice(systemDiscounts);
    }

    // ─── Report Generation ────────────────────────────────────────────────────

    public void generateCategorizedReport(List<String> categoryNames) {
        InventoryReport report = new InventoryReport();
        Map<String, List<Product>> filteredMap = new LinkedHashMap<>();
        List<String> notFound = new ArrayList<>();

        for (String name : categoryNames) {
            String search = name.trim();
            List<Product> matches = products.values().stream()
                    .filter(p -> isProductInCategory(p, search))
                    .collect(Collectors.toList());
            if (!matches.isEmpty()) filteredMap.put(search, matches);
            else notFound.add(search);
        }
        if (!notFound.isEmpty()) System.out.println("NOTICE: >>> Categories not found: " + notFound);
        report.printReport(filteredMap);
    }

    public void generateShortageReport() {
        ShortageReport report = new ShortageReport();
        List<Product> shortages = products.values().stream()
                .filter(Product::isBelowMinStock)
                .collect(Collectors.toList());
        report.printReport(shortages);
    }

    public void generateDefectiveReport() {
        DefectiveReport report = new DefectiveReport();
        report.printReport(defectiveItems);
    }

    private boolean isProductInCategory(Product p, String catName) {
        return (p.getCategory() != null && p.getCategory().getName().equalsIgnoreCase(catName)) ||
                (p.getSub_category() != null && p.getSub_category().getName().equalsIgnoreCase(catName)) ||
                (p.getSub_sub_category() != null && p.getSub_sub_category().getName().equalsIgnoreCase(catName));
    }

    // ─── Stock Management ─────────────────────────────────────────────────────

    public void updateProductStock(int id, int w, int s) {
        Product p = products.get(id);
        if (p != null) {
            p.setStorage_amount(w);
            p.setShelf_amount(s);
            repo.saveProduct(p);
            checkAndAlert(p);
            handleShortageOrder(id);
        }
    }

    public boolean transferToShelf(int productId, int quantity) {
        Product p = products.get(productId);
        if (p == null || quantity <= 0 || p.getStorage_amount() < quantity) return false;
        p.setStorage_amount(p.getStorage_amount() - quantity);
        p.setShelf_amount(p.getShelf_amount() + quantity);
        repo.saveProduct(p);
        checkAndAlert(p);
        return true;
    }

    public boolean logDefectiveAndUpdateStock(int sku, int quantity, String location) {
        Product p = products.get(sku);
        if (p == null) return false;

        DefectiveItem item = new DefectiveItem(p, quantity, location);
        defectiveItems.add(item);
        repo.saveDefectiveItem(item);

        if (location.equalsIgnoreCase("Store")) {
            p.setShelf_amount(p.getShelf_amount() - quantity);
        } else {
            p.setStorage_amount(p.getStorage_amount() - quantity);
        }

        repo.saveProduct(p);
        checkAndAlert(p);
        return true;
    }

    private void checkAndAlert(Product p) {
        if (p.isBelowMinStock()) {
            LowStockAlert alert = new LowStockAlert(p);
            alerts.add(alert);
            repo.saveLowStockAlert(alert);
            alert.print();
        }
    }

    // ─── Product CRUD ─────────────────────────────────────────────────────────

    public void addProduct(Product p) {
        products.put(p.getId(), p);
        repo.saveProduct(p);
    }

    public ProductDTO getProductDTO(int id) {
        Product p = products.get(id);
        return p != null ? toProductDTO(p) : null;
    }

    Product getProduct(int id) { return products.get(id); }

    public boolean productExists(int id) { return products.containsKey(id); }

    public boolean updateProduct(int id, String newName, int newMinStock) {
        Product p = products.get(id);
        if (p == null) return false;
        if (newName != null && !newName.trim().isEmpty()) p.setName(newName);
        if (newMinStock >= 0) p.setMinStock(newMinStock);
        repo.saveProduct(p);
        return true;
    }

    public boolean deleteProduct(int id) {
        if (products.remove(id) != null) {
            repo.deleteProduct(id);
            return true;
        }
        return false;
    }

    // ─── Category Management ──────────────────────────────────────────────────

    public void addCategory(Category c) {
        categories.put(c.getId(), c);
        repo.saveCategory(c);
    }

    public List<CategoryDTO> getCategoriesDTOByNames(List<String> names) {
        return categories.values().stream()
                .filter(c -> names.stream().anyMatch(n -> n.trim().equalsIgnoreCase(c.getName())))
                .map(this::toCategoryDTO)
                .collect(Collectors.toList());
    }

    public List<Category> getCategoriesByNames(List<String> names) {
        return categories.values().stream()
                .filter(c -> names.stream().anyMatch(n -> n.trim().equalsIgnoreCase(c.getName())))
                .collect(Collectors.toList());
    }

    // ─── Supplier & Defective ─────────────────────────────────────────────────

    public void addSupplierPrice(int sku, int sid, float price) {
        Product p = products.get(sku);
        if (p != null) {
            p.addPurchasePrice(sid, price);
            repo.saveProduct(p);
        }
    }

    public void addDefectiveItem(DefectiveItem item) {
        defectiveItems.add(item);
        repo.saveDefectiveItem(item);
    }

    public List<ProductDTO> getProductsDTOBySkus(List<Integer> skus) {
        return skus.stream()
                .map(products::get)
                .filter(Objects::nonNull)
                .map(this::toProductDTO)
                .collect(Collectors.toList());
    }

    List<Product> getProductsBySkus(List<Integer> skus) {
        return skus.stream().map(products::get).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public List<LowStockAlertDTO> getAlertDTOs() {
        return alerts.stream()
                .map(this::toLowStockAlertDTO)
                .collect(Collectors.toUnmodifiableList());
    }

    List<LowStockAlert> getAlerts() { return Collections.unmodifiableList(alerts); }

    // ─── Supplier Integration ─────────────────────────────────────────────────

    public void handleShortageOrder(int productId) {
        Product p = products.get(productId);
        if (p != null && p.isBelowMinStock()) {
            SuppliersServiceDummy.createAutomaticOrder(p, p.getAmountToOrder());
        }
    }

    public void checkAndProcessPeriodicOrders(int tomorrowDay) {
        for (Product p : products.values()) {
            if (p.getDeliveryDay() == tomorrowDay && p.isBelowMinStock()) {
                SuppliersServiceDummy.createAutomaticOrder(p, p.getAmountToOrder());
            }
        }
    }

    // ─── DTO Mappers ──────────────────────────────────────────────────────────

    private ProductDTO toProductDTO(Product p) {
        return new ProductDTO(
                p.getId(),
                p.getName(),
                p.getMin_stock(),
                p.getStorage_amount(),
                p.getShelf_amount(),
                p.getGeneral_amount(),
                p.getAisle(),
                p.getShelf(),
                p.getDeliveryDay(),
                p.getTargetQuantity(),
                p.getBasePrice(),
                p.getCategory()         != null ? p.getCategory().getName()         : null,
                p.getSub_category()     != null ? p.getSub_category().getName()     : null,
                p.getSub_sub_category() != null ? p.getSub_sub_category().getName() : null,
                p.getManufacturer().getName()
        );
    }

    private CategoryDTO toCategoryDTO(Category c) {
        return new CategoryDTO(
                c.getId(),
                c.getName(),
                c.getParentCategory() != null ? c.getParentCategory().getName() : null
        );
    }

    private LowStockAlertDTO toLowStockAlertDTO(LowStockAlert a) {
        return new LowStockAlertDTO(
                a.getProductId(),
                a.getProductName(),
                a.getCurrentStock(),
                a.getMinStock(),
                a.getGeneratedAt()
        );
    }

    private DefectiveItemDTO toDefectiveItemDTO(DefectiveItem item) {
        return new DefectiveItemDTO(
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getDefectiveQuantity(),
                item.getDefectiveLocation()
        );
    }
}