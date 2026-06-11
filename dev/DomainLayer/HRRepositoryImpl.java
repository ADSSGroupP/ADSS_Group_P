package DomainLayer;

import DataAccessLayer.*;
import DTO.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class HRRepositoryImpl implements HRRepository {
    private final EmployeeDAO employeeDAO;
    private final ConstraintDAO constraintDAO;
    private final ShiftDAO shiftDAO;

    public HRRepositoryImpl(EmployeeDAO employeeDAO, ConstraintDAO constraintDAO, ShiftDAO shiftDAO) {
        this.employeeDAO = employeeDAO;
        this.constraintDAO = constraintDAO;
        this.shiftDAO = shiftDAO;
    }

    // ==========================================
    // EMPLOYEE MAPPING & OPERATIONS
    // ==========================================

    @Override
    public void saveEmployee(Employee emp) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.id = emp.getId();
        dto.name = emp.getName();
        dto.isShiftManager = emp.getIs_shift_manager();
        dto.dayOff = emp.getDay_off();
        dto.branchId = 1; // Default or fetched from state
        dto.isActive = emp.isActive();

        // Flatten roles to comma-separated string
        dto.rolesCSV = Arrays.stream(emp.getRoles())
                .map(Role::name)
                .collect(Collectors.joining(","));

        // If it's a Driver, extract license
        if (emp instanceof Driver) {
            dto.isDriver = true;
            dto.license = ((Driver) emp).getLicense();
        } else {
            dto.isDriver = false;
            dto.license = null;
        }

        // Check if employee already exists to decide Insert or Update
        if (employeeDAO.getEmployeeById(emp.getId()) != null) {
            employeeDAO.updateEmployee(dto);
        } else {
            employeeDAO.insertEmployee(dto);
        }

        // Save their internal constraints as well
        for (Constraint c : emp.getCurrentConstraints()) {
            saveConstraint(c);
        }
    }

    @Override
    public Employee getEmployeeById(int id) {
        EmployeeDTO empDto = employeeDAO.getEmployeeById(id);
        if (empDto == null) return null;

        // 1. Reconstruct Roles Array
        Role[] roles = Arrays.stream(empDto.rolesCSV.split(","))
                .map(Role::valueOf)
                .toArray(Role[]::new);

        // 2. Build the domain object (Employee or Driver)
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

        // 3. Populate Constraints from ConstraintDAO
        List<ConstraintDTO> constraintDtos = constraintDAO.getConstraintsByEmployee(id);
        for (ConstraintDTO cDto : constraintDtos) {
            Constraint c = new Constraint(cDto.employeeId, cDto.date, cDto.startTime,
                    cDto.endTime, cDto.doubleShift, cDto.extraHours, cDto.isChangeable);
            emp.addConstraint(c);
        }

        return emp;
    }

    @Override
    public List<Employee> getAllEmployees() {
        return employeeDAO.getAllEmployees().stream()
                .map(dto -> getEmployeeById(dto.id)) // Uses the existing method to fully hydrate constraints
                .collect(Collectors.toList());
    }

    @Override
    public void deleteEmployee(int id) {
        employeeDAO.deleteEmployee(id);
    }

    // ==========================================
    // CONSTRAINT OPERATIONS
    // ==========================================

    @Override
    public void saveConstraint(Constraint c) {
        ConstraintDTO dto = new ConstraintDTO();
        // Since we can't get employeeId directly from getter in Constraint (it's private in your original code),
        // we will need to add a getter 'getEmployeeId()' in Constraint class or reflect it.
        // ASSUMPTION: You have/will add getEmployeeId() to Constraint.
        // For now, using reflection-like mapping or assuming you'll add it.
        dto.employeeId = c.getStartTime() != null ? fetchEmployeeIdViaHack(c) : 0;
        dto.date = c.getDate();
        dto.startTime = c.getStartTime();
        dto.endTime = c.getEndTime();
        dto.doubleShift = c.isDoubleShiftApproved();
        dto.extraHours = c.getExtraHours();
        dto.isChangeable = c.isChangeable();

        constraintDAO.insertConstraint(dto);
    }

    @Override
    public void removeConstraint(int employeeId, LocalDate date) {
        constraintDAO.deleteConstraint(employeeId, date);
    }

    // ==========================================
    // SHIFT MAPPING & OPERATIONS
    // ==========================================

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

    @Override
    public Shift getShiftByDateAndType(LocalDate date, char type) {
        ShiftDTO shiftDto = shiftDAO.getShift(date, type);
        if (shiftDto == null) return null;

        // Reconstruct Manager
        Employee manager = getEmployeeById(shiftDto.managerId);

        // Build core Shift object
        Shift shift = new Shift(date, type, manager, shiftDto.branchId);

        // Load and populate Requirements
        Map<String, Integer> reqs = shiftDAO.getShiftRequirements(date, type);
        reqs.forEach((roleStr, amount) -> shift.setShift_model(Role.valueOf(roleStr), amount));

        // Load and populate Assignments
        Map<Integer, String> assigns = shiftDAO.getShiftAssignments(date, type);
        Map<Integer, Integer> extras = shiftDAO.getShiftExtraHours(date, type);

        assigns.forEach((empId, roleStr) -> {
            Employee emp = getEmployeeById(empId);
            if (emp != null) {
                shift.setShift_roles(emp, Role.valueOf(roleStr));
                if (extras.containsKey(empId)) {
                    shift.addExtraHoursAssignment(emp, extras.get(empId));
                }
            }
        });

        return shift;
    }

    @Override
    public List<Shift> getShiftHistory() {
        return shiftDAO.getAllShifts().stream()
                .map(dto -> getShiftByDateAndType(dto.date, dto.type))
                .collect(Collectors.toList());
    }

    // Small helper helper to bridge the missing employeeId getter if needed
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