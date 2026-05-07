package edu.hei.school.agricultural.repository;

import edu.hei.school.agricultural.entity.Activity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static java.util.UUID.randomUUID;

@Repository
@RequiredArgsConstructor
public class CollectivityActivityRepository {
    private final Connection connection;

    public void linkActivityToCollectivity(String collectivityId, String activityId) {
        String sql = "INSERT INTO collectivity_activity (id, collectivity_id, activity_id) VALUES (?, ?, ?) ON CONFLICT (id) DO NOTHING";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, randomUUID().toString());
            ps.setString(2, collectivityId);
            ps.setString(3, activityId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Activity> findActivitiesByCollectivityId(String collectivityId) {
        List<Activity> activities = new ArrayList<>();
        String sql = """
            SELECT a.id, a.label, a.activity_type, a.executive_date, a.recurrence_rule_week_ordinal, a.recurrence_rule_day_of_week
            FROM activity a
            JOIN collectivity_activity ca ON a.id = ca.activity_id
            WHERE ca.collectivity_id = ?
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, collectivityId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                activities.add(Activity.builder()
                        .id(rs.getString("id"))
                        .label(rs.getString("label"))
                        .activityType(rs.getString("activity_type"))
                        .executiveDate(rs.getDate("executive_date") != null ? rs.getDate("executive_date").toLocalDate() : null)
                        .recurrenceRuleWeekOrdinal(rs.getInt("recurrence_rule_week_ordinal"))
                        .recurrenceRuleDayOfWeek(rs.getString("recurrence_rule_day_of_week"))
                        .build());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return activities;
    }
}