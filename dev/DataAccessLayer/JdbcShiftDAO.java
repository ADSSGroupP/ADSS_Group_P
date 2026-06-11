package DataAccessLayer;


import DTO.ShiftDTO;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JdbcShiftDAO implements ShiftDAO {

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
    public ShiftDTO getShift(LocalDate date, char type) {
        String sql = "SELECT * FROM shifts WHERE date = ? AND type = ?";
        try (Connection conn = Database.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, date.toString());
            pstmt.setString(2, String.valueOf(type));
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

    @Override
    public void saveShiftRequirements(LocalDate date, char type, Map<String, Integer> requirements) {
        String deleteSql = "DELETE FROM shift_requirements WHERE shift_date = ? AND shift_type = ?";
        String insertSql = "INSERT INTO shift_requirements (shift_date, shift_type, role, amount) VALUES (?, ?, ?, ?)";

        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement delStmt = conn.prepareStatement(deleteSql);
                 PreparedStatement insStmt = conn.prepareStatement(insertSql)) {

                delStmt.setString(1, date.toString());
                delStmt.setString(2, String.valueOf(type));
                delStmt.executeUpdate();

                for (Map.Entry<String, Integer> entry : requirements.entrySet()) {
                    insStmt.setString(1, date.toString());
                    insStmt.setString(2, String.valueOf(type));
                    insStmt.setString(3, entry.getKey());
                    insStmt.setInt(4, entry.getValue());
                    insStmt.addBatch();
                }
                insStmt.executeBatch();
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving shift requirements", e);
        }
    }

    @Override
    public Map<String, Integer> getShiftRequirements(LocalDate date, char type) {
        Map<String, Integer> map = new HashMap<>();
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

    @Override
    public void saveShiftAssignments(LocalDate date, char type, Map<Integer, String> assignments, Map<Integer, Integer> extraHours) {
        String deleteSql = "DELETE FROM shift_assignments WHERE shift_date = ? AND shift_type = ?";
        String insertSql = "INSERT INTO shift_assignments (shift_date, shift_type, employee_id, role, extra_hours) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement delStmt = conn.prepareStatement(deleteSql);
                 PreparedStatement insStmt = conn.prepareStatement(insertSql)) {

                delStmt.setString(1, date.toString());
                delStmt.setString(2, String.valueOf(type));
                delStmt.executeUpdate();

                for (Map.Entry<Integer, String> entry : assignments.entrySet()) {
                    int empId = entry.getKey();
                    insStmt.setString(1, date.toString());
                    insStmt.setString(2, String.valueOf(type));
                    insStmt.setInt(3, empId);
                    insStmt.setString(4, entry.getValue());
                    insStmt.setInt(5, extraHours.getOrDefault(empId, 0));
                    insStmt.addBatch();
                }
                insStmt.executeBatch();
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving shift assignments", e);
        }
    }

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