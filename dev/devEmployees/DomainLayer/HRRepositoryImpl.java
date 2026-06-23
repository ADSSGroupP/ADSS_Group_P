package devEmployees.DomainLayer;

import devEmployees.DTO.ConstraintDTO;
import devEmployees.DTO.EmployeeDTO;
import devEmployees.DTO.ShiftDTO;
import devEmployees.DataAccessLayer.*;
import DTO.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Concrete implementation of the HRRepository interface handling the business-to-data mapping layer.
 * Coordinates between Domain entities (Employee, Driver, Shift, Constraint) and Data Access Objects (DAOs),
 * encapsulating domain object reconstitution, soft-deletes, and relational persistence tracking.
 */
public class HRRepositoryImpl implements HRRepository {
    private final EmployeeDAO employeeDAO;
    private final ConstraintDAO constraintDAO;
    private final ShiftDAO shiftDAO;

    // Constructor to inject concrete DAO data access dependencies
    public HRRepositoryImpl(EmployeeDAO employeeDAO, ConstraintDAO constraintDAO, ShiftDAO shiftDAO) {
        this.employeeDAO = employeeDAO;
        this.constraintDAO = constraintDAO;
        this.shiftDAO = shiftDAO;
    }

    // ==========================================
    // EMPLOYEE MAPPING & OPERATIONS
    // ==========================================

    // Serializes a domain Employee/Driver object into a relational DTO and persists it
    @Override
    public void saveEmployee(Employee emp) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.id = emp.getId();
        dto.name = emp.getName();
        dto.isShiftManager = emp.getIs_shift_manager();
        dto.dayOff = emp.getDay_off();
        dto.branchId = 1; // Default or fetched from state
        dto.isActive = emp.isActive();

        // Fetch inner JobTerms object from Employee and map financial data
        var terms = emp.getTerms();
        if (terms != null) {
            dto.startDate = terms.getStart_date();
            dto.jobScope = terms.getJob_scope();
            dto.globalWage = terms.getGlobal_wage();
            dto.hourlyWage = terms.getHourly_wage();
        }

        // Map basic bank account details directly from Employee
        dto.bankNum = emp.getBank_num();
        dto.branchNum = emp.getBranch_num();
        dto.accountNum = emp.getAccount_num();

        // Flatten roles array to a comma-separated string
        dto.rolesCSV = Arrays.stream(emp.getRoles())
                .map(Role::name)
                .collect(Collectors.joining(","));

        // Handle specific Driver fields if applicable
        if (emp instanceof Driver) {
            dto.isDriver = true;
            dto.license = ((Driver) emp).getLicense();
        } else {
            dto.isDriver = false;
            dto.license = null;
        }

        // Performs a database UPDATE (which updates is_active) if exists, otherwise INSERT
        if (employeeDAO.getEmployeeById(emp.getId()) != null) {
            employeeDAO.updateEmployee(dto);
        } else {
            employeeDAO.insertEmployee(dto);
        }

        // Save employee's internal constraints
        for (Constraint c : emp.getCurrentConstraints()) {
            saveConstraint(c);
        }
    }

    // Fetches relational DTO data and reconstructs the corresponding concrete domain entity
    @Override
    public Employee getEmployeeById(int id) {
        EmployeeDTO empDto = employeeDAO.getEmployeeById(id);
        if (empDto == null) return null;

        // Reconstruct Roles Array from CSV string
        Role[] roles = Arrays.stream(empDto.rolesCSV.split(","))
                .map(Role::valueOf)
                .toArray(Role[]::new);

        // Reconstruct the concrete domain object (Employee or Driver)
        Employee emp;
        if (empDto.isDriver) {
            emp = new Driver(empDto.name, empDto.id, roles, empDto.bankNum, empDto.branchNum,
                    empDto.accountNum, empDto.dayOff, empDto.startDate, empDto.jobScope,
                    empDto.globalWage, empDto.hourlyWage, empDto.branchId, empDto.license);
        } else {
            emp = new Employee(empDto.name, empDto.id, roles, empDto.bankNum, empDto.branchNum,
                    empDto.accountNum, empDto.dayOff, empDto.startDate, empDto.jobScope,
                    empDto.globalWage, empDto.hourlyWage, empDto.branchId);
        }
        emp.setActive(empDto.isActive);

        // Populate constraints from ConstraintDAO
        List<ConstraintDTO> constraintDtos = constraintDAO.getConstraintsByEmployee(id);
        for (ConstraintDTO cDto : constraintDtos) {
            Constraint c = new Constraint(cDto.employeeId, cDto.date, cDto.startTime,
                    cDto.endTime, cDto.doubleShift, cDto.extraHours, cDto.isChangeable);
            emp.addConstraint(c);
        }

        return emp;
    }

    // Filters and retrieves a list containing only currently active personnel records
    @Override
    public List<Employee> getAllEmployees() {
        return employeeDAO.getAllEmployees().stream()
                .map(dto -> getEmployeeById(dto.id))
                .filter(Objects::nonNull)
                .filter(Employee::isActive)
                .collect(Collectors.toList());
    }

    // Filters and retrieves a list containing only inactive or fired personnel records
    @Override
    public List<Employee> getFiredEmployees() {
        return employeeDAO.getAllEmployees().stream()
                .map(dto -> getEmployeeById(dto.id))
                .filter(Objects::nonNull)
                .filter(emp -> !emp.isActive())
                .collect(Collectors.toList());
    }
    // Performs a soft-delete by marking the status flag false and saving changes
    @Override
    public void deleteEmployee(int id) {
        Employee emp = getEmployeeById(id);
        if (emp != null) {
            emp.setActive(false);
            saveEmployee(emp);
            System.out.println("Employee " + emp.getName() + " was successfully soft-deleted (status set to inactive).");
        } else {
            System.out.println("Employee with ID " + id + " not found.");
        }
    }

    // ==========================================
    // CONSTRAINT OPERATIONS
    // ==========================================

    // Converts a domain constraint into a DTO record and logs it to persistent storage    @Override
    public void saveConstraint(Constraint c) {
        ConstraintDTO dto = new ConstraintDTO();
        // Dynamically extract the hidden employee ID field via reflection fallback if necessary
        dto.employeeId = c.getStartTime() != null ? fetchEmployeeIdViaHack(c) : 0;
        dto.date = c.getDate();
        dto.startTime = c.getStartTime();
        dto.endTime = c.getEndTime();
        dto.doubleShift = c.isDoubleShiftApproved();
        dto.extraHours = c.getExtraHours();
        dto.isChangeable = c.isChangeable();

        constraintDAO.insertConstraint(dto);
    }

    // Permanently removes a registered constraint entry matching an employee and a target date
    @Override
    public void removeConstraint(int employeeId, LocalDate date) {
        constraintDAO.deleteConstraint(employeeId, date);
    }

    // ==========================================
    // SHIFT MAPPING & OPERATIONS
    // ==========================================

    // Deconstructs shift models, staffing goals, and job assignments into database relation mappings
    @Override
    public void saveShift(Shift shift) {
        // 1. Save core shift details
        ShiftDTO dto = new ShiftDTO();
        dto.date = shift.getDate();
        dto.type = shift.getType();
        dto.managerId = shift.getShift_manager() != null ? shift.getShift_manager().getId() : 0;
        dto.branchId = 1; // Assuming default branch or adding a getter in Shift

        shiftDAO.insertShift(dto);

        // 2. Save Staffing Requirements (Model)
        Map<String, Integer> reqMap = new HashMap<>();
        shift.getShift_model().forEach((role, amount) -> reqMap.put(role.name(), amount));
        shiftDAO.saveShiftRequirements(shift.getDate(), shift.getType(), reqMap);

        // 3. Save Assignments and Extra Hours
        Map<Integer, String> assignMap = new HashMap<>();
        Map<Integer, Integer> extraMap = new HashMap<>();

        shift.getShift_roles().forEach((emp, role) -> assignMap.put(emp.getId(), role.name()));
        shift.getExtraHoursAssignments().forEach((emp, hours) -> extraMap.put(emp.getId(), hours));

        shiftDAO.saveShiftAssignments(shift.getDate(), shift.getType(), assignMap, extraMap);
    }

    // Resolves historical database DTO values to compile a unified stateful Shift object
    @Override
    public Shift getShiftByDateAndType(LocalDate date, char type) {
        ShiftDTO shiftDto = shiftDAO.getShift(date, type);
        if (shiftDto == null) return null;

        // 1. Fetch the manager from the database using getEmployeeById
        Employee manager = getEmployeeById(shiftDto.managerId);

        // Fallback safeguard to handle missing or archived supervisors safely without a crash
         if (manager == null) {
            manager = new Employee("Manager (ID: " + shiftDto.managerId + ")", shiftDto.managerId,
                    new Role[]{Role.SHIFTMANAGER}, 0, 0, 0,
                    null, null, "N/A", 0, 0, shiftDto.branchId);
        }

        Shift shift = new Shift(shiftDto.date, shiftDto.type, manager, shiftDto.branchId);

        // 2. Load and register organizational shift structure requirements
        Map<String, Integer> reqs = shiftDAO.getShiftRequirements(date, type);
        if (reqs != null) {
            reqs.forEach((roleStr, amount) -> shift.setShift_model(Role.valueOf(roleStr), amount));
        }

        // 3. Load, reconstruct, and assign staff rosters and relative overtime values
        Map<Integer, String> assigns = shiftDAO.getShiftAssignments(date, type);
        Map<Integer, Integer> extras = shiftDAO.getShiftExtraHours(date, type);
        if (assigns != null) {
            assigns.forEach((empId, roleStr) -> {
                Employee emp = getEmployeeById(empId);
                // Assign the active worker or handle archived/fired personnel entries safely via context placeholders
                if (emp != null) {
                    shift.setShift_roles(emp, Role.valueOf(roleStr));
                    if (extras.containsKey(empId)) {
                        shift.addExtraHoursAssignment(emp, extras.get(empId));
                    }
                } else {
                    // Optional: Create a temporary placeholder for a fired worker if you want them to show in history
                    Employee firedEmp = new Employee("Fired Worker (ID: " + empId + ")", empId,
                            new Role[]{Role.valueOf(roleStr)}, 0, 0, 0, null, null, "N/A", 0, 0, shiftDto.branchId);
                    shift.setShift_roles(firedEmp, Role.valueOf(roleStr));
                }

            });
        }
        return shift;
    }

    // Extracts and processes all historically scheduled system work shifts
    @Override
    public List<Shift> getShiftHistory() {
        return shiftDAO.getAllShifts().stream()
                .map(dto -> getShiftByDateAndType(dto.date, dto.type))
                .collect(Collectors.toList());
    }

    // Reflection utility helper used to unlock private context employee ID trackers inside constraints
    private int fetchEmployeeIdViaHack(Constraint c) {
        try {
            java.lang.reflect.Field field = Constraint.class.getDeclaredField("employee_ID");
            field.setAccessible(true);
            return (int) field.get(c);
        } catch (Exception e) {
            return 0;
        }
    }
}