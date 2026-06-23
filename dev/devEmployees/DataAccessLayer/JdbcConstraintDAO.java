package DataAccessLayer;

import DTO.ConstraintDTO;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
/**
 * JDBC-based implementation of the ConstraintDAO interface.
 * Handles SQL execution, parameters binding, and data mapping between
 * SQLite database rows and ConstraintDTO object structures.
 */
public class JdbcConstraintDAO implements ConstraintDAO {
    // Saves or updates a constraint record using standard SQL parameter indexing
    @Override
    public void insertConstraint(ConstraintDTO dto) {
        String sql = "INSERT OR REPLACE INTO constraints (employee_id, date, start_time, end_time, double_shift, extra_hours, is_changeable) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, dto.employeeId);
            pstmt.setString(2, dto.date.toString());
            pstmt.setString(3, dto.startTime.toString());
            pstmt.setString(4, dto.endTime.toString());
            pstmt.setInt(5, dto.doubleShift ? 1 : 0);
            pstmt.setInt(6, dto.extraHours);
            pstmt.setInt(7, dto.isChangeable ? 1 : 0);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting constraint", e);
        }
    }
    // Removals a constraint matching both the unique employee reference and date parameters
    @Override
    public void deleteConstraint(int employeeId, LocalDate date) {
        String sql = "DELETE FROM constraints WHERE employee_id = ? AND date = ?";
        try (Connection conn = Database.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, employeeId);
            pstmt.setString(2, date.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting constraint", e);
        }
    }
    // Queries the database to extract all constraints submitted by a targeted worker ID
    @Override
    public List<ConstraintDTO> getConstraintsByEmployee(int employeeId) {
        List<ConstraintDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM constraints WHERE employee_id = ?";
        try (Connection conn = Database.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, employeeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToDTO(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching constraints by employee", e);
        }
        return list;
    }
    // Gathers all submissions matching a targeted operational calendar date    @Override
    public List<ConstraintDTO> getConstraintsByDate(LocalDate date) {
        List<ConstraintDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM constraints WHERE date = ?";
        try (Connection conn = Database.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, date.toString());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToDTO(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching constraints by date", e);
        }
        return list;
    }
    // Helper method: Extracts data from SQL ResultSet rows and maps them into a DTO instance
    private ConstraintDTO mapResultSetToDTO(ResultSet rs) throws SQLException {
        ConstraintDTO dto = new ConstraintDTO();
        dto.employeeId = rs.getInt("employee_id");
        dto.date = LocalDate.parse(rs.getString("date"));
        dto.startTime = LocalTime.parse(rs.getString("start_time"));
        dto.endTime = LocalTime.parse(rs.getString("end_time"));
        dto.doubleShift = rs.getInt("double_shift") == 1;
        dto.extraHours = rs.getInt("extra_hours");
        dto.isChangeable = rs.getInt("is_changeable") == 1;
        return dto;
    }
}