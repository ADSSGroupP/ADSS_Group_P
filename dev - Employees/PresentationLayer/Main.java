package PresentationLayer;

import DataAccessLayer.*;
import ServiceLayer.EmployeeService;
import ServiceLayer.ShiftService;
import ServiceLayer.DataInitializer;
import DomainLayer.HRRepository;
import DomainLayer.HRRepositoryImpl;

public class Main {
    public static void main(String[] args) {

        try { DataAccessLayer.Database.getConnection().close(); } catch (Exception e) {}

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