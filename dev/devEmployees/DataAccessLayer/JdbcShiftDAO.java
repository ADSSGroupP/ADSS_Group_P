package devEmployees.DataAccessLayer;

import devEmployees.DTO.ShiftDTO;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * JDBC-based implementation of the ShiftDAO interface.
 * Manages SQL execution for persistence, retrieval, and structural mapping
 * of shifts, dynamic staff requirements, and active staff role assignments.
 */
public class JdbcShiftDAO implements ShiftDAO {
    // Inserts or replaces a basic shift record inside the database
    @Override
    public void insertShift(ShiftDTO dto) {
        String sql = "INSERT OR REPLACE INTO shifts (date, type, manager_id, branch_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = Database.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, dto.date.toString());
            pstmt.setString(2, String.valueOf(dto.type));
            pstmt.setInt(3, dto.managerId);
            pstmt.setInt(4, dto.branchId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting shift", e);
        }
    }

    @Override
    // Fetches a single unique shift row utilizing its composite keys (date and type)
    public ShiftDTO getShift(LocalDate date, char type) {
        String sql = "SELECT * FROM shifts WHERE date = ? AND type = ?";
        try (Connection conn = Database.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Trim and format strings to prevent whitespace mismatches during database lookup
            pstmt.setString(1, date.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE).trim());
            pstmt.setString(2, String.valueOf(type).trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    ShiftDTO dto = new ShiftDTO();
                    dto.date = LocalDate.parse(rs.getString("date"));
                    dto.type = rs.getString("type").charAt(0);
                    dto.managerId = rs.getInt("manager_id");
                    dto.branchId = rs.getInt("branch_id");
                    return dto;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching shift", e);
        }
        return null;
    }

    // Retrieves all basic shift master records from the database
    @Override
    public List<ShiftDTO> getAllShifts() {
        List<ShiftDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM shifts";
        try (Connection conn = Database.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ShiftDTO dto = new ShiftDTO();
                dto.date = LocalDate.parse(rs.getString("date"));
                dto.type = rs.getString("type").charAt(0);
                dto.managerId = rs.getInt("manager_id");
                dto.branchId = rs.getInt("branch_id");
                list.add(dto);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching all shifts", e);
        }
        return list;
    }

    // Saves the model requirements (needed role capacities) for a shift using batching
    @Override
    public void saveShiftRequirements(LocalDate date, char type, Map<String, Integer> requirements) {
        String sql = "INSERT OR REPLACE INTO shift_requirements (shift_date, shift_type, role, amount) VALUES (?, ?, ?, ?)";
        try (Connection conn = Database.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Loop through map entries and add updates to a single batch run to reduce database IO
            for (Map.Entry<String, Integer> entry : requirements.entrySet()) {
                pstmt.setString(1, date.toString());
                pstmt.setString(2, String.valueOf(type));
                pstmt.setString(3, entry.getKey());
                pstmt.setInt(4, entry.getValue());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving shift requirements", e);
        }
    }

    // Fetches the defined staffing model limits for a specific scheduled time block
    @Override
    public Map<String, Integer> getShiftRequirements(LocalDate date, char type) {
        Map<String, Integer> map = new HashMap<>();
        // MATCHED WITH DB SCHEMA: shift_date, shift_type, amount
        String sql = "SELECT role, amount FROM shift_requirements WHERE shift_date = ? AND shift_type = ?";
        try (Connection conn = Database.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, date.toString());
            pstmt.setString(2, String.valueOf(type));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("role"), rs.getInt("amount"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching shift requirements", e);
        }
        return map;
    }

    // Persists the actual employee assignments and extra hours parameters using batching
    @Override
    public void saveShiftAssignments(LocalDate date, char type, Map<Integer, String> assignments, Map<Integer, Integer> extraHours) {
        // MATCHED WITH DB SCHEMA: shift_date, shift_type, employee_id, role, extra_hours
        String sql = "INSERT OR REPLACE INTO shift_assignments (shift_date, shift_type, employee_id, role, extra_hours) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Iterates over roster mappings to stage batch insertions including mapped overtime calculations
            for (Map.Entry<Integer, String> entry : assignments.entrySet()) {
                int empId = entry.getKey();
                pstmt.setString(1, date.toString());
                pstmt.setString(2, String.valueOf(type));
                pstmt.setInt(3, empId);
                pstmt.setString(4, entry.getValue());
                pstmt.setInt(5, extraHours.getOrDefault(empId, 0));
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving shift assignments", e);
        }
    }

    // Retrieves employee assignment pairs mapping specific worker IDs to their scheduled roles
    @Override
    public Map<Integer, String> getShiftAssignments(LocalDate date, char type) {
        Map<Integer, String> map = new HashMap<>();
        String sql = "SELECT employee_id, role FROM shift_assignments WHERE shift_date = ? AND shift_type = ?";
        try (Connection conn = Database.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, date.toString());
            pstmt.setString(2, String.valueOf(type));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getInt("employee_id"), rs.getString("role"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching shift assignments", e);
        }
        return map;
    }

    // Extracts overtime calculations for active staff members where allocated hours exceed 0
    @Override
    public Map<Integer, Integer> getShiftExtraHours(LocalDate date, char type) {
        Map<Integer, Integer> map = new HashMap<>();
        String sql = "SELECT employee_id, extra_hours FROM shift_assignments WHERE shift_date = ? AND shift_type = ? AND extra_hours > 0";
        try (Connection conn = Database.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, date.toString());
            pstmt.setString(2, String.valueOf(type));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getInt("employee_id"), rs.getInt("extra_hours"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching shift extra hours", e);
        }
        return map;
    }
}