package devEmployees.PresentationLayer;
import devEmployees.DataAccessLayer.*;
import devEmployees.DomainLayer.HRRepository;
import devEmployees.DomainLayer.HRRepositoryImpl;
import devEmployees.ServiceLayer.EmployeeService;
import devEmployees.ServiceLayer.ShiftService;


public class Main {
    public static void main(String[] args) {

        try { devEmployees.DataAccessLayer.Database.getConnection().close(); } catch (Exception e) {}

        EmployeeDAO employeeDAO = new JdbcEmployeeDAO();
        ConstraintDAO constraintDAO = new JdbcConstraintDAO();
        ShiftDAO shiftDAO = new JdbcShiftDAO();

        HRRepository hrRepository = new HRRepositoryImpl(employeeDAO, constraintDAO, shiftDAO);

        // Step 1: Initialize Services
        EmployeeService empService = new EmployeeService(hrRepository);
        ShiftService shiftService = new ShiftService(hrRepository, empService);

        // Step 2: Initialize UI with the new name
        UserInterface ui = new UserInterface(empService, shiftService);

        // Step 3: Run the application
        ui.start();
    }
}