package devEmployees.DataAccessLayer;

import devEmployees.DTO.EmployeeDTO;

import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
/**
 * JDBC-based implementation of the EmployeeDAO interface.
 * Manages SQL execution for saving, updating, retrieving, and removing employee data
 * from the SQLite database while handling column data type transformations.
 */
public class JdbcEmployeeDAO implements EmployeeDAO {
    // Saves a new employee record into the database
    @Override
    public void insertEmployee(EmployeeDTO dto) {
        String sql = "INSERT INTO employees (id, name, bank_num, branch_num, account_num, is_shift_manager, " +
                "day_off, branch_id, start_date, job_scope, global_wage, hourly_wage, is_driver, license, roles) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Mapping fields and converting complex types/booleans into SQL compatible values
            pstmt.setInt(1, dto.id);
            pstmt.setString(2, dto.name);
            pstmt.setInt(3, dto.bankNum);
            pstmt.setInt(4, dto.branchNum);
            pstmt.setInt(5, dto.accountNum);
            pstmt.setInt(6, dto.isShiftManager ? 1 : 0);
            pstmt.setString(7, dto.dayOff != null ? dto.dayOff.name() : null);
            pstmt.setInt(8, dto.branchId);
            pstmt.setString(9, dto.startDate != null ? dto.startDate.toString() : null);
            pstmt.setString(10, dto.jobScope);
            pstmt.setDouble(11, dto.globalWage);
            pstmt.setDouble(12, dto.hourlyWage);
            pstmt.setInt(13, dto.isDriver ? 1 : 0);
            pstmt.setString(14, dto.license);
            pstmt.setString(15, dto.rolesCSV);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting employee", e);
        }
    }
    // Updates existing fields and employment status for an employee record
    @Override
    public void updateEmployee(EmployeeDTO dto) {
        String sql = "UPDATE employees SET name=?, bank_num=?, branch_num=?, account_num=?, is_shift_manager=?, " +
                "day_off=?, branch_id=?, start_date=?, job_scope=?, global_wage=?, hourly_wage=?, is_driver=?, " +
                "license=?, roles=?, is_active=? WHERE id=?";

        try (Connection conn = Database.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, dto.name);
            pstmt.setInt(2, dto.bankNum);
            pstmt.setInt(3, dto.branchNum);
            pstmt.setInt(4, dto.accountNum);
            pstmt.setInt(5, dto.isShiftManager ? 1 : 0);
            pstmt.setString(6, dto.dayOff != null ? dto.dayOff.name() : null);
            pstmt.setInt(7, dto.branchId);
            pstmt.setString(8, dto.startDate != null ? dto.startDate.toString() : null);
            pstmt.setString(9, dto.jobScope);
            pstmt.setDouble(10, dto.globalWage);
            pstmt.setDouble(11, dto.hourlyWage);
            pstmt.setInt(12, dto.isDriver ? 1 : 0);
            pstmt.setString(13, dto.license);
            pstmt.setString(14, dto.rolesCSV);
            pstmt.setInt(15, dto.isActive ? 1 : 0); // Param 15: Maps boolean status (1 for active, 0 for fired)
            pstmt.setInt(16, dto.id);               // Param 16: Matches the target employee ID
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating employee profile and status", e);
        }
    }

    // Retrieves a specific employee's DTO matching their unique ID
    @Override
    public EmployeeDTO getEmployeeById(int id) {
        String sql = "SELECT * FROM employees WHERE id = ?";
        try (Connection conn = Database.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDTO(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching employee", e);
        }
        return null;
    }
    // Fetches all registered employee rows from the table
    @Override
    public List<EmployeeDTO> getAllEmployees() {
        List<EmployeeDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM employees";
        try (Connection conn = Database.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToDTO(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching all employees", e);
        }
        return list;
    }
    // Permanently removes an employee from the table using their unique ID
    @Override
    public void deleteEmployee(int id) {
        String sql = "DELETE FROM employees WHERE id = ?";
        try (Connection conn = Database.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting employee", e);
        }
    }
    // Helper method: Maps relational database row values back into a clean EmployeeDTO object
    private EmployeeDTO mapResultSetToDTO(ResultSet rs) throws SQLException {
        EmployeeDTO dto = new EmployeeDTO();
        dto.id = rs.getInt("id");
        dto.name = rs.getString("name");
        dto.bankNum = rs.getInt("bank_num");
        dto.branchNum = rs.getInt("branch_num");
        dto.accountNum = rs.getInt("account_num");
        dto.isShiftManager = rs.getInt("is_shift_manager") == 1;
        // Reconstruct DayOfWeek Enum from database string representation
        String dayOffStr = rs.getString("day_off");
        dto.dayOff = dayOffStr != null ? DayOfWeek.valueOf(dayOffStr) : null;
        dto.branchId = rs.getInt("branch_id");
        String dateStr = rs.getString("start_date");
        dto.startDate = dateStr != null ? LocalDate.parse(dateStr) : null;
        dto.jobScope = rs.getString("job_scope");
        dto.globalWage = rs.getDouble("global_wage");
        dto.hourlyWage = rs.getDouble("hourly_wage");
        dto.isDriver = rs.getInt("is_driver") == 1;
        dto.license = rs.getString("license");
        dto.rolesCSV = rs.getString("roles");
        // Reads the integer from DB column and reconstructs the boolean active status flag
        dto.isActive = rs.getInt("is_active") == 1;
        return dto;
    }
}