package edu.hei.school.agricultural.repository;

import edu.hei.school.agricultural.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.util.UUID.randomUUID;

@Repository
@RequiredArgsConstructor
public class ActivityMemberAttendanceRepository {
    private final Connection connection;

    public void saveAttendance(String activityId, String memberId, String attendanceStatus) {
        String checkSql = "SELECT attendance_status FROM activity_member_attendance WHERE activity_id = ? AND member_id = ?";
        String insertSql = "INSERT INTO activity_member_attendance (id, activity_id, member_id, attendance_status) VALUES (?, ?, ?, ?)";
        String updateSql = "UPDATE activity_member_attendance SET attendance_status = ? WHERE activity_id = ? AND member_id = ? AND attendance_status = 'UNDEFINED'";

        try (PreparedStatement checkPs = connection.prepareStatement(checkSql)) {
            checkPs.setString(1, activityId);
            checkPs.setString(2, memberId);
            ResultSet rs = checkPs.executeQuery();

            if (rs.next()) {
                String currentStatus = rs.getString("attendance_status");
                if (!"UNDEFINED".equals(currentStatus)) {
                    throw new RuntimeException("Cannot update attendance status for member " + memberId + " because status is already " + currentStatus);
                }
                try (PreparedStatement updatePs = connection.prepareStatement(updateSql)) {
                    updatePs.setString(1, attendanceStatus);
                    updatePs.setString(2, activityId);
                    updatePs.setString(3, memberId);
                    updatePs.executeUpdate();
                }
            } else {
                try (PreparedStatement insertPs = connection.prepareStatement(insertSql)) {
                    insertPs.setString(1, randomUUID().toString());
                    insertPs.setString(2, activityId);
                    insertPs.setString(3, memberId);
                    insertPs.setString(4, attendanceStatus);
                    insertPs.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Member> findAttendanceByActivityIdAndStatus(String activityId, String attendanceStatus) {
        List<Member> members = new ArrayList<>();
        String sql = """
            SELECT m.id, m.first_name, m.last_name, m.email, m.occupation
            FROM member m
            JOIN activity_member_attendance ama ON m.id = ama.member_id
            WHERE ama.activity_id = ? AND ama.attendance_status = ?
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, activityId);
            ps.setString(2, attendanceStatus);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Member member = Member.builder()
                        .id(rs.getString("id"))
                        .firstName(rs.getString("first_name"))
                        .lastName(rs.getString("last_name"))
                        .email(rs.getString("email"))
                        .build();
                members.add(member);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return members;
    }

    public Double getMemberAssiduityPercentage(String memberId, LocalDate from, LocalDate to) {
        String sql = """
            SELECT 
                CASE 
                    WHEN COUNT(DISTINCT a.id) = 0 THEN 100.0
                    ELSE (COUNT(DISTINCT CASE WHEN ama.attendance_status = 'ATTENDED' THEN a.id END) * 100.0) 
                         / NULLIF(COUNT(DISTINCT a.id), 0)
                END
            FROM collectivity_activity ca
            JOIN activity a ON ca.activity_id = a.id
            LEFT JOIN activity_member_attendance ama ON a.id = ama.activity_id AND ama.member_id = ?
            WHERE a.executive_date BETWEEN ? AND ?
            GROUP BY ca.collectivity_id
            LIMIT 1
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, memberId);
            ps.setDate(2, java.sql.Date.valueOf(from));
            ps.setDate(3, java.sql.Date.valueOf(to));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 100.0;
    }
}