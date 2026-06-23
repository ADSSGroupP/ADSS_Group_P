import java.util.Scanner;
import Presentation.InventoryMenu;
import Service.DatabaseManager;
import Service.IntegrationDummyFunctions;
import Service.InventoryService;
import devEmployees.DataAccessLayer.JdbcEmployeeDAO;
import devEmployees.DataAccessLayer.JdbcConstraintDAO;
import devEmployees.DataAccessLayer.JdbcShiftDAO;
import devEmployees.DomainLayer.HRRepository;
import devEmployees.DomainLayer.HRRepositoryImpl;
import devEmployees.ServiceLayer.EmployeeService;
import devEmployees.ServiceLayer.ShiftService;
import devEmployees.PresentationLayer.UserInterface;

/**
 * Main entry point for the Super-Li Supermarket Management System.
 */
public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("========================================");
        System.out.println("   SUPER-LI MANAGEMENT SYSTEM          ");
        System.out.println("========================================");
        System.out.println("Select Module:");
        System.out.println("1. Inventory Management");
        System.out.println("2. Employee & HR Management");
        System.out.println("3. Exit");
        System.out.print("Choice: ");

        String choice = sc.nextLine().trim();

        switch (choice) {
            case "1":
                InventoryService service = new InventoryService();
                IntegrationDummyFunctions.init(service);
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    DatabaseManager.closeConnection();
                    System.out.println("System shutdown complete.");
                }));
                new InventoryMenu(service).start();
                break;

            case "2":
                JdbcEmployeeDAO employeeDAO = new JdbcEmployeeDAO();
                JdbcConstraintDAO constraintDAO = new JdbcConstraintDAO();
                JdbcShiftDAO shiftDAO = new JdbcShiftDAO();
                HRRepository hrRepository = new HRRepositoryImpl(employeeDAO, constraintDAO, shiftDAO);
                EmployeeService empService = new EmployeeService(hrRepository);
                ShiftService shiftService = new ShiftService(hrRepository, empService);
                UserInterface ui = new UserInterface(empService, shiftService);
                ui.start();
                break;

            case "3":
                System.out.println("Goodbye!");
                break;

            default:
                System.out.println("Invalid choice. Exiting.");
        }
    }
}
