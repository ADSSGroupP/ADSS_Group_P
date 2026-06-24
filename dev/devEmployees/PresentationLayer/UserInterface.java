package devEmployees.PresentationLayer;

import devEmployees.ServiceLayer.*;
import devEmployees.DomainLayer.*;
import devEmployees.Transportation.TransportationMock;


import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.*;

/**
 * The primary entry point for user interaction.
 * Provides a Command Line Interface (CLI) for both Personnel Managers and Employees.
 * Facilitates data flow between user input and the Service Layer.
 */
public class UserInterface {
    private EmployeeService employeeService;
    private ShiftService shiftService;
    private Scanner scanner;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private int currentBranchId;

    public UserInterface(EmployeeService empService, ShiftService shiftService) {
        this.employeeService = empService;
        this.shiftService = shiftService;
        this.scanner = new Scanner(System.in);
    }

    /**
     * Starts the main system loop.
     * Handles the top-level routing between Manager, Employee, and Exit options.
     */
    public void start() {
        System.out.println("--- Welcome to Super-Lee HR System ---");
        System.out.print("Please enter Branch ID to operate: ");
        try {
            this.currentBranchId = Integer.parseInt(scanner.nextLine());
        } catch (Exception e) {
            System.out.println("Invalid Branch ID. Defaulting to 1.");
            this.currentBranchId = 1;
        }
        while (true) {
            System.out.println("\n--- Branch: " + currentBranchId + " ---");
            System.out.println("1. Login as Personnel Manager");
            System.out.println("2. Login as Employee");
            System.out.println("3. Exit");

            try {
                int choice = Integer.parseInt(scanner.nextLine());
                switch (choice) {
                    case 1 -> managerMenu();
                    case 2 -> employeeMenu();
                    case 3 -> System.exit(0);
                    default -> System.out.println("Invalid choice.");
                }
            } catch (Exception e) {
                System.out.println("Error: Please enter a valid number.");
            }
        }
    }
    // --- Personnel Manager Section ---
    /**
     * Handles Manager authentication and displays the administrative dashboard.
     */
    private void managerMenu() {
        System.out.print("Enter HR Password: ");
        String pass = scanner.nextLine();
        if (!pass.equals("6789")) {
            System.out.println("Access Denied!");
            return;
        }

        while (true) {
            System.out.println("\n--- Personnel Manager Menu ---");
            System.out.println("1. Add Employee to System");
            System.out.println("2. View All Employees");
            System.out.println("3. Create New Shift");
            System.out.println("4. Assign Employee to Shift (Shift Scheduling)");
            System.out.println("5. View Shift History");
            System.out.println("6. Edit Section (Promotion, Template, Substitution)");
            System.out.println("7. Set Submission Deadline");
            System.out.println("8. Exceptional Assignment");
            System.out.println("9. Fire Employee (Move to Archive)");
            System.out.println("10. View Fired Employees");
            System.out.println("11. Logout");

            try {
                int choice = Integer.parseInt(scanner.nextLine());
                if (choice == 11) break;
                handleHRChoice(choice);
            } catch (Exception e) {
                System.out.println("Invalid input.");
                e.printStackTrace();
            }
        }
    }

    private void handleHRChoice(int choice) {
        switch (choice) {
            case 1 -> addEmployeeUI();
            case 2 -> displayEmployeesUI();
            case 3 -> createShiftUI();
            case 4 -> shiftAssignmentUI();
            case 5 -> displayHistoryUI();
            case 6 -> editSubMenu();
            case 7 -> setDeadlineUI();
            case 8 -> exceptionalAssignmentUI();
            case 9 -> fireEmployeeUI();
            case 10 -> displayFiredEmployeesUI();
        }
    }

    // --- Employee Section ---
    private void employeeMenu() {
        System.out.print("Enter Employee ID: ");
        int id = Integer.parseInt(scanner.nextLine());
        Employee emp = employeeService.getEmployeeById(id);

        if (emp == null) {
            System.out.println("Employee not found.");
            return;
        }

        while (true) {
            System.out.println("\n--- Employee Menu: " + emp.getName() + " ---");
            System.out.println("1. Submit Constraint");
            System.out.println("2. View My Constraints");
            System.out.println("3. View Weekly Schedule");
            System.out.println("4. Edit Existing Constraint");
            System.out.println("5. Logout");

            int choice = Integer.parseInt(scanner.nextLine());
            if (choice == 5) break;

            switch (choice) {
                case 1 -> addConstraintUI(emp);
                case 2 -> viewConstraintsUI(emp);
                case 3 -> displayWeeklyScheduleUI();
                case 4 -> editConstraints(emp);
            }
        }
    }


    // --- 1. Add Employee ---
    private void addEmployeeUI() {
        try {
            System.out.println("\n--- Add New Employee ---");
            System.out.print("Name: ");
            String name = scanner.nextLine();
            System.out.print("ID: ");
            int id = Integer.parseInt(scanner.nextLine());
            System.out.print("Roles (comma separated, e.g. CASHIER, DRIVER): ");
            String[] rolesInput = scanner.nextLine().split(",");
            Role[] roles = new Role[rolesInput.length];
            try {
                for (int i = 0; i < rolesInput.length; i++) {
                    roles[i] = Role.valueOf(rolesInput[i].trim().toUpperCase());
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Error: One of the roles you entered is not recognized. (Check your spelling!)");
                return;
            }
            System.out.print("Bank Number: ");
            int bankNum = Integer.parseInt(scanner.nextLine());
            System.out.print("Branch Number: ");
            int branchNum = Integer.parseInt(scanner.nextLine());
            System.out.print("Account Number: ");
            int accNum = Integer.parseInt(scanner.nextLine());

            System.out.print("Day Off (e.g., MONDAY, SUNDAY): ");
            String dayOffInput = scanner.nextLine().toUpperCase().trim();
            java.time.DayOfWeek dayOff = java.time.DayOfWeek.valueOf(dayOffInput);

            System.out.print("Job Scope: ");
            String jobScope = scanner.nextLine();
            System.out.print("Global Wage: ");
            double global = Double.parseDouble(scanner.nextLine());
            System.out.print("Hourly Wage: ");
            double hourly = Double.parseDouble(scanner.nextLine());
            boolean isDriver = false;
            String license="";
            for (Role r : roles) {
                if (r == Role.DRIVER) {
                    isDriver = true;
                    System.out.print("Enter driver license type: ");
                    license = scanner.nextLine();
                    break;
                }
            }
            Employee newEmp;
            if(isDriver){
                newEmp = new Driver(name, id, roles, bankNum, branchNum, accNum, dayOff, LocalDate.now(), jobScope, global, hourly,this.currentBranchId,license);
            }
            else{
                newEmp = new Employee(name, id, roles, bankNum, branchNum, accNum, dayOff, LocalDate.now(), jobScope, global, hourly,this.currentBranchId);
            }

            employeeService.addEmployee(newEmp);
            System.out.println("Employee added successfully.");

        } catch (IllegalArgumentException e) {
            System.out.println("Error: Invalid day name. Please use full English names like MONDAY.");
        } catch (Exception e) {
            System.out.println("Error adding employee: Check your input format.");
        }
    }

    // --- 2. Display Employee ---
    private void displayEmployeesUI() {
        List<Employee> all = employeeService.getAllEmployees();
        if (all.isEmpty()) {
            System.out.println("No active employees found in the system.");
        } else {
            all.forEach(e -> System.out.println(e.getId() + ": " + e.getName()));
        }
    }

    // --- 3. Create Shift UI ---
    private void createShiftUI() {
        try {
            System.out.println("\n--- Create New Shift ---");
            System.out.print("Date (dd/MM/yyyy): ");
            LocalDate d = LocalDate.parse(scanner.nextLine(), dateFormatter);
            System.out.print("Type (m/e): ");
            char t = scanner.nextLine().toLowerCase().charAt(0);
            if((t!='m')&&(t!='e')){
                System.out.println("Error: shift type must be m/e");
                return;
            }
            System.out.print("Manager ID: ");
            int mId = Integer.parseInt(scanner.nextLine());
            Employee mgr = employeeService.getEmployeeById(mId);

            if (mgr == null) {
                System.out.println("Error: Employee not found.");
                return;
            }
            if(!mgr.getIs_shift_manager()){
                System.out.println("Error: Employee is not qualified as shit manager");
                return;
            }
            int num = shiftService.numOfDriversPerShift(d, t);
            if (shiftService.mustHaveStoreKeeper(d, t)) {
                if (num == 1) {
                    System.out.print("There is " + num + " transportation during this shift, assign at least one store keeper\n");
                } else {
                    System.out.print("There are " + num + " transportations during this shift, assign at least one store keeper\n");
                }
            }
            System.out.print("Use default staffing requirements (2 Cashiers, 2 Storekeepers)? (y/n): ");
            String useDefault = scanner.nextLine().toLowerCase();

            if (useDefault.equals("y")) {
                shiftService.createDefaultShift(d, t, mgr, this.currentBranchId, num);
                System.out.println("There are "+ num +" transportations,"+ num + " drivers added to template");
                System.out.println("Shift created successfully for branch " + currentBranchId);
            } else if(useDefault.equals("n")) {
                Map<Role, Integer> customModel = new HashMap<>();
                System.out.println("Enter roles and amounts (type 'done' to finish):");
                while (true) {
                    System.out.print("Role Name: ");
                    String roleName = scanner.nextLine();
                    if (roleName.equalsIgnoreCase("done")) break;
                    if (roleName.equals("DRIVER")) {
                        System.out.println("Notice: Drivers are assigned automatically based on transportations.");
                        continue;
                    }
                    try {
                        Role roleEnum = Role.valueOf(roleName.trim().toUpperCase());
                        System.out.print("Amount needed: ");
                        int amount = Integer.parseInt(scanner.nextLine());

                        customModel.put(roleEnum, amount);
                    } catch (IllegalArgumentException e) {
                        if (e instanceof NumberFormatException) {
                            System.out.println("Error: Please enter a valid whole number for the amount.");
                        } else {
                            System.out.println("Error: Role '" + roleName + "' is not recognized. (Use: CASHIER, DRIVER, etc.)");
                        }
                    }
                    customModel.put(Role.DRIVER,num);
                    shiftService.createCustomShift(d, t, mgr, customModel, this.currentBranchId);
                }
                System.out.println("There are "+ num +" transportations,"+ num + " drivers added to template");
                System.out.println("Shift created successfully for branch " + currentBranchId);
            }else {
                System.out.println("Invalid input, must enter y/n");
            }
        }catch (Exception e) {
            System.out.println("Error in shift creation process: " + e.getMessage());
        }
    }

    // --- 4. Shift assignment ---
    private void shiftAssignmentUI() {
        try {
            boolean hasStoreKeeper=false;
            System.out.println("\n--- Full Shift Scheduling ---");
            System.out.print("Enter Date (dd/MM/yyyy): ");
            LocalDate date = LocalDate.parse(scanner.nextLine(), dateFormatter);
            System.out.print("Enter Shift Type (m/e): ");
            char type = scanner.nextLine().toLowerCase().charAt(0);

            Shift shift = shiftService.findShiftByDateAndType(date, type);
            if (shift == null) {
                System.out.println("Error: No shift was created for this date/type yet.");
                return;
            }

            List<Employee> available = shiftService.getAvailableEmployeesForShift(date, type);
            if (available.isEmpty()) {
                System.out.println("No employees available for this date.");
                return;
            }

            System.out.println("\n--- Reference: Available Employees ---");
            for (Employee e : available) {
                System.out.println("ID: " + e.getId() + " | Name: " + e.getName() + " | Roles: " + Arrays.toString(e.getRoles()));
            }

            Map<Role, Integer> model = shift.getShift_model();
            System.out.println("\n--- Starting Assignment for " + model.size() + " Roles ---");

            for (Role roleName : model.keySet()) {
                int requiredAmount = model.get(roleName);
                System.out.println("\n>> Role: [" + roleName + "] | Needs: " + requiredAmount + " employees.");
                Set<Integer> assignedDriverIds = new HashSet<>();
                for (int i = 1; i <= requiredAmount; i++) {
                    int empId = -1;
                    int extraHoursToAssign = 0;

                    if (roleName == Role.DRIVER) {

                        String reqLicense = TransportationMock.getRequiredLicenseForTransport(TransportationMock.getTransportId());
                        System.out.println("   [Transport #" + i + " requires License: " + reqLicense + "]");

                        List<Driver> validDrivers = shiftService.getAvailableDriversForTransport(date, type, reqLicense);

                        validDrivers.removeIf(d -> assignedDriverIds.contains(d.getId()));
                        if (validDrivers.isEmpty()) {
                            System.out.println("   Error: No available drivers with license " + reqLicense + "!");
                            continue;
                        }

                        System.out.print("   Available: ");
                        validDrivers.forEach(d -> System.out.print(d.getName() + " (ID:" + d.getId() + ") "));

                        Driver selectedDriver = null;
                        while (selectedDriver == null) {
                            System.out.print("\n  Enter ID of a driver from the list above: ");
                            try {
                                empId = Integer.parseInt(scanner.nextLine());
                                int finalId =empId;
                                selectedDriver = validDrivers.stream()
                                        .filter(d -> d.getId() == finalId)
                                        .findFirst()
                                        .orElse(null);

                                if (selectedDriver == null) {
                                    System.out.println("Error: The ID " + empId + " is not in the list of available drivers. Please try again.");
                                }
                            } catch (NumberFormatException e) {
                                System.out.println("Error: Please enter a valid numeric ID.");
                            }
                        }

                        shiftService.assignEmployeeToShift(empId, date, type, Role.DRIVER, 0);
                        shift.getShiftAssignment().assignDriverToTransport(i, selectedDriver);
                        assignedDriverIds.add(selectedDriver.getId());
                        System.out.println("   Success: Driver " + selectedDriver.getName() + " assigned to Transport #" + i);
                        continue;
                    }else {

                        System.out.print("   Enter ID for employee #" + i + " to be a " + roleName + ": ");
                        try {
                            empId = Integer.parseInt(scanner.nextLine());
                        }catch (Exception e) { System.out.println("Invalid ID."); i--; continue; }
                        }

                        if (roleName == Role.SHIFTMANAGER) {
                            continue;
                        }

                        if (type == 'm') {
                            System.out.print("   Assign extra hours for this employee? (Enter amount or 0): ");
                            try {
                                extraHoursToAssign = Integer.parseInt(scanner.nextLine());
                            } catch (NumberFormatException e) {
                                extraHoursToAssign = 0;
                            }
                        }

                        if(roleName == Role.STOREKEEPER){
                            hasStoreKeeper=true;
                        }

                        String result = shiftService.assignEmployeeToShift(empId, date, type, roleName, extraHoursToAssign);
                        System.out.println("   Result: " + result);

                        if (result.contains("Error") || result.contains("failed") || result.contains("not qualified")) {
                            System.out.println("   Please try again for this position.");
                            i--;
                        }else if (extraHoursToAssign > 0) {
                            System.out.println("   -> Extra hours [" + extraHoursToAssign + "] recorded for this assignment.");
                        }
                    }
                }
            if(shiftService.mustHaveStoreKeeper(date,type)&&!hasStoreKeeper){
                System.out.println("Validation Error: Shift must have a storekeeper due to transportation constraints.");
                return;
            }

            System.out.println("\n--- Scheduling Completed for this Shift ---");

        } catch (Exception e) {
            System.out.println("Error in process: " + e.getMessage());
        }
    }

    // --- 5. Display History ---
    private void displayHistoryUI() {
        shiftService.getShiftHistory().forEach(System.out::println);
    }

    // --- 6. Edit Sub-Menu Implementation ---

    private void editSubMenu() {
        boolean backToManager = false;
        while (!backToManager) {
            System.out.println("\n--- Edit Section ---");
            System.out.println("1. Certify Shift Manager (Promotion)");
            System.out.println("2. Edit Shift Template (Role requirements)");
            System.out.println("3. Substitute/Remove Employee from Shift");
            System.out.println("4. Back to Manager Menu");

            try {
                int choice = Integer.parseInt(scanner.nextLine());
                switch (choice) {
                    case 1 -> certifyShiftManagerUI();
                    case 2 -> editShiftTemplateUI();
                    case 3 -> substituteEmployeeUI();
                    case 4 -> backToManager = true;
                    default -> System.out.println("Invalid choice.");
                }
            } catch (Exception e) {
                System.out.println("Error: Please enter a number.");
            }
        }
    }

    // --- 7. Set DeadLine ---
    private void setDeadlineUI() {
        try {
            System.out.print("Enter Deadline Date (dd/MM/yyyy): ");
            String datePart = scanner.nextLine();
            System.out.print("Enter Deadline Time (HH:mm): ");
            String timePart = scanner.nextLine();

            // CHANGE: Parse input to LocalDateTime
            DateTimeFormatter fullFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            LocalDateTime deadline = LocalDateTime.parse(datePart + " " + timePart, fullFormatter);

            employeeService.setConstraintDeadline(deadline);
            System.out.println("Deadline set successfully to: " + deadline.format(fullFormatter));
        } catch (Exception e) {
            System.out.println("Error: Invalid format. Use dd/MM/yyyy and HH:mm.");
        }
    }
    // --- 8. Set Exceptional Assignment ---
    private void exceptionalAssignmentUI() {
        try {
            System.out.println("\n--- Exceptional Shift Assignment ---");
            System.out.print("Enter Date (dd/MM/yyyy): ");
            LocalDate date = LocalDate.parse(scanner.nextLine(), dateFormatter);
            System.out.print("Enter Shift Type (m/e): ");
            char type = scanner.nextLine().toLowerCase().charAt(0);
            System.out.print("Enter Employee ID: ");
            int id = Integer.parseInt(scanner.nextLine());
            System.out.print("Enter Role(CASHIER, DRIVER, etc.): ");
            String roleInput = scanner.nextLine().trim().toUpperCase();
            Role roleEnum;
            try {
                roleEnum = Role.valueOf(roleInput);
            } catch (IllegalArgumentException e) {
                System.out.println("Error: Role '" + roleInput + "' is not recognized.");
                return;
            }

            String result = shiftService.assignExceptionalShift(id, date, type, roleEnum);
            System.out.println("Result: " + result);
        } catch (Exception e) {
            System.out.println("Error: Invalid input.");
        }
    }

    // --- 9. Fire Employee ---
    private void fireEmployeeUI () {
        System.out.print("Enter Employee ID to FIRE: ");
        try {
            int id = Integer.parseInt(scanner.nextLine());
            System.out.print("Are you sure you want to fire this employee? (yes/no): ");
            if (scanner.nextLine().equalsIgnoreCase("yes")) {
                String result = employeeService.fireEmployee(id);
                System.out.println(result);
            } else {
                System.out.println("Action cancelled.");
            }
        } catch (Exception e) {
            System.out.println("Invalid ID format.");
        }
    }
    //--- 10.displayFiredEmployees ---
    private void displayFiredEmployeesUI () {
        System.out.println("\n--- Fired Employees Records ---");
        List<Employee> fired = employeeService.getFiredEmployees();
        if (fired.isEmpty()) {
            System.out.println("No fired employees in the system.");
        } else {
            fired.forEach(e -> System.out.println("ID: " + e.getId() + " | Name: " + e.getName()));
        }
}


    // --- 6.1 Certify Shift Manager ---
    private void certifyShiftManagerUI() {
        try {
            System.out.print("Enter Employee ID to promote: ");
            int id = Integer.parseInt(scanner.nextLine());
            Employee emp = employeeService.getEmployeeById(id);

            if (emp == null) {
                System.out.println("Employee not found.");
                return;
            }

            // Manual update since Service doesn't have the method
            List<Role> rolesList = new ArrayList<>(Arrays.asList(emp.getRoles()));
            if (!rolesList.contains(Role.SHIFTMANAGER)) {
                rolesList.add(Role.SHIFTMANAGER);
                emp.setRoles(rolesList.toArray(new Role[0])); // Assumes setRoles exists in Employee
                System.out.println(emp.getName() + " is now a Shift Manager.");
            } else {
                System.out.println("Employee is already a Shift Manager.");
            }
        } catch (Exception e) {
            System.out.println("Error.");
        }
    }

    // --- 6.2 Edit Shift Template ---
    private void editShiftTemplateUI() {
        try {
            System.out.print("Date (dd/MM/yyyy): ");
            LocalDate date = LocalDate.parse(scanner.nextLine(), dateFormatter);
            System.out.print("Type (m/e): ");
            char type = scanner.nextLine().toLowerCase().charAt(0);

            Shift shift = shiftService.findShiftByDateAndType(date, type);
            if (shift == null) {
                System.out.println("Shift not found.");
                return;
            }

            System.out.print("Enter role to change (CASHIER, DRIVER...): ");
            String roleInput = scanner.nextLine().trim().toUpperCase();

            if (roleInput.equals("DRIVER")) {
                System.out.println("Error: Driver staffing is determined automatically by the transportation schedule.");
                return;
            }

            Role roleEnum;
            try {
                roleEnum = Role.valueOf(roleInput);
                System.out.print("Enter new required amount: ");
                int amount = Integer.parseInt(scanner.nextLine());
                shift.getStaffingRequirement().setRequirement(roleEnum, amount);
                System.out.println("Template updated.");
            } catch (IllegalArgumentException e) {
                System.out.println("Error: Role '" + roleInput + "' is not valid.");
            }
        } catch (Exception e) {
            System.out.println("Error updating template.");
        }
    }

    // --- 6.3 Substitute/Edit Assignment ---

    private void substituteEmployeeUI() {
        try {
            System.out.print("Date (dd/MM/yyyy): ");
            LocalDate date = LocalDate.parse(scanner.nextLine(), dateFormatter);
            System.out.print("Type (m/e): ");
            char type = scanner.nextLine().toLowerCase().charAt(0);

            Shift shift = shiftService.findShiftByDateAndType(date, type);
            if (shift == null) {
                System.out.println("Shift not found.");
                return;
            }

            System.out.println("Current assignments: " + shift.getShift_roles());
            System.out.print("Enter ID to remove: ");
            int id = Integer.parseInt(scanner.nextLine());
            Employee empToRemove = employeeService.getEmployeeById(id);

            if (empToRemove != null && shift.getShift_roles().containsKey(empToRemove)) {
                shift.getShift_roles().remove(empToRemove);
                System.out.println("Employee removed from shift.");
            } else {
                System.out.println("Employee not found in this shift.");
            }
        } catch (Exception e) {
            System.out.println("Error removing employee.");
        }
    }

    // --- Employee: Submit Constraint UI ---

    // --- 1. Add Constraint ---
    private void addConstraintUI(Employee emp) {

        if (employeeService.isDeadlinePassed()) {
            System.out.println("ACCESS DENIED: The deadline for constraints has passed.");
            return;
        }
        try {
            System.out.print("Date (dd/MM/yyyy): ");
            LocalDate d = LocalDate.parse(scanner.nextLine(), dateFormatter);
            System.out.print("Start Time (HH:mm): ");
            LocalTime s = LocalTime.parse(scanner.nextLine());
            System.out.print("End Time (HH:mm): ");
            LocalTime e = LocalTime.parse(scanner.nextLine());

            System.out.print("Do you approve double shifts for this date? (true/false): ");
            boolean isDouble = Boolean.parseBoolean(scanner.nextLine());
            System.out.print("Extra hours needed? (0 if none): ");
            int extraHours = Integer.parseInt(scanner.nextLine());

            System.out.print("Is this a flexible constraint? (true if manager can ask you to work, false if blocked): ");
            boolean isChangeable = Boolean.parseBoolean(scanner.nextLine());

            Constraint c = new Constraint(emp.getId(), d, s, e, isDouble, extraHours,isChangeable);
            emp.addConstraint(c);

        } catch (Exception e) {
            System.out.println("Invalid format or input. Please try again.");
        }
    }

    // --- 2. View Constraints ---
    private void viewConstraintsUI(Employee emp) {
        try {
            System.out.print("Enter Date to check (dd/MM/yyyy): ");
            LocalDate date = LocalDate.parse(scanner.nextLine(), dateFormatter);

            System.out.println("\n--- Constraints for " + emp.getName() + " on " + date + " ---");

            // Morning shift availability check
            boolean morningAvailable = shiftService.isEmployeeAvailableForShift(emp, date, 'm');
            System.out.print("Morning Shift (m): ");
            if (morningAvailable) {
                System.out.println("AVAILABLE");
            } else {
                System.out.println("BLOCKED - Constraint exists");
            }

            // Evening shift availability check
            boolean eveningAvailable = shiftService.isEmployeeAvailableForShift(emp, date, 'e');
            System.out.print("Evening Shift (e): ");
            if (eveningAvailable) {
                System.out.println("AVAILABLE");
            } else {
                int potentialExtra = shiftService.getPotentialExtraHours(emp, date);
                if (potentialExtra > 0) {
                    System.out.println("BLOCKED (But offers " + potentialExtra + " extra hours from morning shift)");
                } else {
                    System.out.println("BLOCKED - Constraint exists");
                }
            }

            int potentialExtra = shiftService.getPotentialExtraHours(emp, date);
            if (potentialExtra > 0) {
                System.out.println("\n>>> Employee indicated they can work " + potentialExtra + " EXTRA HOURS on this day.");
            }

            // Detailed time breakdown of constraints
            if (!morningAvailable || !eveningAvailable) {
                System.out.println("\nDetailed Constraints List for this day:");
                emp.getCurrentConstraints().stream()
                        .filter(c -> c.getDate().equals(date))
                        .forEach(c -> System.out.println("   - From: " + c.getStartTime() + " To: " + c.getEndTime()));
            }

        } catch (Exception e) {
            System.out.println("Error: Invalid date format or system error. Please use dd/MM/yyyy.");
        }

    }

    //--- 3.Display Weekly Schedule ---
    private void displayWeeklyScheduleUI() {
        System.out.println("\n--- Weekly Shift Schedule (Next 7 Days) ---");
        LocalDate today = LocalDate.now();

        for (int i = 0; i < 7; i++) {
            LocalDate currentDate = today.plusDays(i);
            System.out.println("\n========================================");
            System.out.println("DATE: " + currentDate.format(dateFormatter) + " (" + currentDate.getDayOfWeek() + ")");
            System.out.println("========================================");

            printShiftSummary(currentDate, 'm');
            printShiftSummary(currentDate, 'e');
        }
    }


    private void printShiftSummary(LocalDate date, char type) {
        String shiftTypeName = (type == 'm') ? "MORNING" : "EVENING";
        System.out.println("\n>>> " + shiftTypeName + " SHIFT:");

        Shift shift = shiftService.findShiftByDateAndType(date, type);

        if (shift == null) {
            System.out.println("    Status: No shift created.");
            return;
        }

        if (shift.getShift_manager() != null) {
            System.out.println("    Shift Manager: " + shift.getShift_manager().getName());
        } else {
            System.out.println("    Shift Manager: Not assigned");
        }

        Map<Employee, Role> assignments = shift.getShift_roles();
        if (assignments == null || assignments.isEmpty()) {
            System.out.println("    Assignments: No employees assigned yet.");
        } else {
            System.out.println("    Assignments:");
            for (Map.Entry<Employee, Role> entry : assignments.entrySet()) {
                System.out.println("      - " + entry.getValue() + ": " + entry.getKey().getName());
            }
        }
        Map<Employee, Integer> extraAssignments = shift.getExtraHoursAssignments();
        if (extraAssignments != null && !extraAssignments.isEmpty()) {
            System.out.println("    Extra Hours Staff (Overlapping from morning):");
            for (Map.Entry<Employee, Integer> entry : extraAssignments.entrySet()) {
                System.out.println("      * " + entry.getKey().getName() + ": " + entry.getValue() + " hours");
            }
        }
    }

    //--- 4.Edit Constraints ---
    private void editConstraints(Employee emp) {
        // CHANGE: Immediate deadline check
        if (employeeService.isDeadlinePassed()) {
            System.out.println("!!! ACCESS DENIED: The deadline for editing constraints has passed.");
            return;
        }

        try {
            System.out.println("\n--- Edit Existing Constraint ---");
            System.out.print("Enter the Date of the constraint you wish to change (dd/MM/yyyy): ");
            LocalDate dateToEdit = LocalDate.parse(scanner.nextLine(), dateFormatter);

            // Check if a constraint even exists for that day
            boolean exists = emp.getCurrentConstraints().stream()
                    .anyMatch(c -> c.getDate().equals(dateToEdit));

            if (!exists) {
                System.out.println("No constraint found for this date. Use 'Submit' to add a new one.");
                return;
            }

            // Get new details
            System.out.println("Enter new details for this date:");
            System.out.print("New Start Time (HH:mm): ");
            LocalTime s = LocalTime.parse(scanner.nextLine());
            System.out.print("New End Time (HH:mm): ");
            LocalTime e = LocalTime.parse(scanner.nextLine());
            System.out.print("Is this a double shift? (true/false): ");
            boolean isDouble = Boolean.parseBoolean(scanner.nextLine());
            System.out.print("Extra hours needed? (0 if none): ");
            int extraHours = Integer.parseInt(scanner.nextLine());
            System.out.print("Is this a flexible constraint? (true if manager can ask you to work, false if blocked): ");
            boolean isChangeable = Boolean.parseBoolean(scanner.nextLine());

            // Create new constraint object
            Constraint updatedConstraint = new Constraint(emp.getId(), dateToEdit, s, e, isDouble, extraHours, isChangeable);

            // Send to service for processing
            String result = employeeService.editConstraint(emp.getId(), dateToEdit, updatedConstraint);
            System.out.println(result);

        } catch (Exception e) {
            System.out.println("Error: Invalid input format. Editing cancelled.");
        }

    }

}