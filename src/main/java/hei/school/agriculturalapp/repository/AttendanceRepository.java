package hei.school.agriculturalapp.repository;

import hei.school.agriculturalapp.config.DatabaseConfig;
import hei.school.agriculturalapp.dto.CreateAttendance;
import hei.school.agriculturalapp.model.Activity;
import hei.school.agriculturalapp.model.Attendance;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class AttendanceRepository {

    private final DatabaseConfig dbconfig;

    public void saveAttendance(String activityId, List<CreateAttendance> attendances) throws SQLException {
        String sql = "INSERT INTO attendance (id, activity_id, member_id, attendance_status) VALUES (?, ?, ?, ?) ON CONFLICT (activity_id, member_id) DO NOTHING";

        try (PreparedStatement stmt = dbconfig.connection().prepareStatement(sql)) {
            for (CreateAttendance att : attendances) {
                String id = "att-" + System.currentTimeMillis() + "-" + att.getMemberIdentifier();
                stmt.setString(1, id);
                stmt.setString(2, activityId);
                stmt.setString(3, att.getMemberIdentifier());
                stmt.setString(4, att.getAttendanceStatus());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    public boolean hasExistingAttendance(String activityId, String memberId) throws SQLException {
        String sql = "SELECT 1 FROM attendance WHERE activity_id = ? AND member_id = ? AND attendance_status != 'UNDEFINED'";
        try (PreparedStatement stmt = dbconfig.connection().prepareStatement(sql)) {
            stmt.setString(1, activityId);
            stmt.setString(2, memberId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public List<Attendance> findByActivityId(String activityId) throws SQLException {
        List<Attendance> attendances = new ArrayList<>();
        String sql = "SELECT a.id, a.activity_id, a.member_id, a.attendance_status, m.first_name, m.last_name, m.email, m.occupation FROM attendance a JOIN member m ON m.id = a.member_id WHERE a.activity_id = ?";

        try (PreparedStatement stmt = dbconfig.connection().prepareStatement(sql)) {
            stmt.setString(1, activityId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Attendance att = new Attendance();
                    att.setId(rs.getString("id"));
                    att.setActivityId(rs.getString("activity_id"));
                    att.setMemberId(rs.getString("member_id"));
                    att.setAttendanceStatus(rs.getString("attendance_status"));
                    att.setMemberFirstName(rs.getString("first_name"));
                    att.setMemberLastName(rs.getString("last_name"));
                    att.setMemberEmail(rs.getString("email"));
                    att.setMemberOccupation(rs.getString("occupation"));
                    attendances.add(att);
                }
            }
        }
        return attendances;
    }

    public void initializeUndefinedAttendance(String activityId, Activity activity, List<String> memberIds) throws SQLException {
        String sql = "INSERT INTO attendance (id, activity_id, member_id, attendance_status) VALUES (?, ?, ?, 'UNDEFINED') ON CONFLICT DO NOTHING";

        try (PreparedStatement stmt = dbconfig.connection().prepareStatement(sql)) {
            for (String memberId : memberIds) {
                String id = "att-" + System.currentTimeMillis() + "-" + memberId;
                stmt.setString(1, id);
                stmt.setString(2, activityId);
                stmt.setString(3, memberId);
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }
}