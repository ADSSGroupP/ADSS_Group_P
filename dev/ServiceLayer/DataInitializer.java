package ServiceLayer;

import DomainLayer.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class DataInitializer {

    /**
     * Fills the system with initial data for testing and demonstration purpose10s.
     */
    public static void seedData(EmployeeService empService, ShiftService shiftService) {
        // Base date for the demonstration week
        LocalDate startWeek = LocalDate.of(2026, 4, 26);
        int branchId = 1;

        // 1. הגדרת העובדים (נשמור אותם במשתנים ברורים)
        Employee e1 = new Employee("Yossi Cohen", 101, new Role[]{Role.CASHIER, Role.SHIFTMANAGER}, 12, 901, 55501, DayOfWeek.SATURDAY, LocalDate.now(), "Full", 10000, 50, branchId);
        Employee e2 = new Employee("Dana Levy", 102, new Role[]{Role.CASHIER}, 12, 901, 55502, DayOfWeek.FRIDAY, LocalDate.now(), "Part", 0, 45, branchId);
        Employee e3 = new Employee("Ran Israeli", 103, new Role[]{Role.STOREKEEPER}, 12, 901, 55503, DayOfWeek.SUNDAY, LocalDate.now(), "Full", 8000, 42, branchId);
        Employee e5 = new Employee("Ariel Mizrahi", 105, new Role[]{Role.SHIFTMANAGER}, 12, 901, 55505, DayOfWeek.TUESDAY, LocalDate.now(), "Full", 12000, 60, branchId);

        // נהגים
        Driver d1 = new Driver("Avi Driver", 201, new Role[]{Role.DRIVER}, 12, 901, 77701, DayOfWeek.SATURDAY, LocalDate.now(), "Full", 9500, 50, branchId, "C1");
        Driver d2 = new Driver("Benny Trans", 202, new Role[]{Role.DRIVER}, 12, 901, 77702, DayOfWeek.FRIDAY, LocalDate.now(), "Part", 0, 55, branchId, "C");

        // הוספה לסרוויס
        empService.addEmployee(e1); empService.addEmployee(e2); empService.addEmployee(e3);
        empService.addEmployee(e5); empService.addEmployee(d1); empService.addEmployee(d2);

        // 2. הוספת אילוצים (נשתמש ב-ID של העובדים שהרגע יצרנו)
        empService.insertConstraint(101, new Constraint(101, startWeek, LocalTime.of(6,0), LocalTime.of(14,0), true, 0, true));
        empService.insertConstraint(103, new Constraint(103, startWeek, LocalTime.of(16,0), LocalTime.of(20,0), true, 2, true));

        // 3. יצירת משמרות

        // אופציה א: Custom Shift ליום ראשון
        Map<Role, Integer> standardModel = new HashMap<>();
        standardModel.put(Role.CASHIER, 2);
        standardModel.put(Role.STOREKEEPER, 1);
        standardModel.put(Role.SHIFTMANAGER, 1);
        standardModel.put(Role.DRIVER, 1); // הוספת נהג אחד באופן ידני למודל

        // עכשיו e1 מזוהה כי הוא הוגדר למעלה
        shiftService.createCustomShift(startWeek, 'm', e1, standardModel, branchId);

        // אופציה ב: Default Shift ליום שני
        // כאן אנחנו מעבירים 2 כמות נהגים כפי שביקשת
        shiftService.createDefaultShift(startWeek.plusDays(1), 'e', e5, branchId, 2);

        System.out.println(">>> Data Seeding Completed Successfully for Branch " + branchId);
    }
}