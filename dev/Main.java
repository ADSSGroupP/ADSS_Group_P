import java.util.Scanner;

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
                devInventory.Presentation.Main.main(args);
                break;
            case "2":
                devEmployees.PresentationLayer.Main.main(args);
                break;
            case "3":
                System.out.println("Goodbye!");
                break;
            default:
                System.out.println("Invalid choice. Exiting.");
        }
    }
}
