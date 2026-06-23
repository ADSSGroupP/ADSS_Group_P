package devEmployees.DomainLayer;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Domain entity representing a Driver, which is a specialized type of Employee.
 * Inherits all standard personnel characteristics from the Employee base class
 * and extends them with supply chain logistics capabilities, specifically vehicle license levels.
 */
public class Driver extends Employee{
    private String license ;
    // Constructor to initialize a fully defined Driver with all base employee fields and specific driving license
    public Driver(String name, int id, Role[] roles, int bank_num, int branch_num, int account_num, DayOfWeek day_off, LocalDate start_date, String job_scope, double global_wage, double hourly_wage, int branch_id , String license) {
        super(name, id, roles,bank_num,branch_num,account_num,day_off,start_date,job_scope,global_wage,hourly_wage,branch_id);
        this.license = license;
    }
    // Getter method to retrieve the driver's vehicle license type
    public String getLicense(){
        return this.license;
    }
}
