package DTO;

import java.time.DayOfWeek;
import java.time.LocalDate;
/**
 * Data Transfer Object (DTO) representing a flattened employee record.
 * Used to safely serialize and transfer comprehensive personnel data, structural contract terms,
 * specialized driver fields, and aggregated roles between the DAO mapping tier and the database tables.
 */
public class EmployeeDTO {
    public int id;
    public String name;
    public int bankNum;
    public int branchNum;
    public int accountNum;
    public boolean isShiftManager;
    public DayOfWeek dayOff;
    public int branchId;

    // Job Terms fields (flattened)
    public LocalDate startDate;
    public String jobScope;
    public double globalWage;
    public double hourlyWage;

    // Driver fields (null if not a driver)
    public boolean isDriver;
    public String license;

    // Roles compiled as a comma-separated String for easy DB storage
    public String rolesCSV;
    public boolean isActive;

    public EmployeeDTO() {}
}
