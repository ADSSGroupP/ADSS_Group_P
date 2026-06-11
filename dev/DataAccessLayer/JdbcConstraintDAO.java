package DataAccessLayer;

import DTO.ConstraintDTO;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class JdbcConstraintDAO implements ConstraintDAO {

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

    @Override
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