package edu.hei.school.agricultural.repository;

import edu.hei.school.agricultural.entity.Activity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.UUID.randomUUID;

@Repository
@RequiredArgsConstructor
public class ActivityRepository {
    private final Connection connection;

    public Activity save(Activity activity) {
        if (activity.getId() == null) {
            activity.setId(randomUUID().toString());
        }
        String sql = """
            INSERT INTO activity (id, label, activity_type, executive_date, recurrence_rule_week_ordinal, recurrence_rule_day_of_week)
            VALUES (?, ?, ?, ?, ?, ?)
            ON CONFLICT (id) DO UPDATE SET
                label = EXCLUDED.label,
                activity_type = EXCLUDED.activity_type,
                executive_date = EXCLUDED.executive_date,
                recurrence_rule_week_ordinal = EXCLUDED.recurrence_rule_week_ordinal,
                recurrence_rule_day_of_week = EXCLUDED.recurrence_rule_day_of_week
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, activity.getId());
            ps.setString(2, activity.getLabel());
            ps.setString(3, activity.getActivityType());
            ps.setDate(4, activity.getExecutiveDate() != null ? java.sql.Date.valueOf(activity.getExecutiveDate()) : null);
            ps.setObject(5, activity.getRecurrenceRuleWeekOrdinal());
            ps.setString(6, activity.getRecurrenceRuleDayOfWeek());
            ps.executeUpdate();
            return activity;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<Activity> findById(String id) {
        String sql = "SELECT id, label, activity_type, executive_date, recurrence_rule_week_ordinal, recurrence_rule_day_of_week FROM activity WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(Activity.builder()
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
        return Optional.empty();
    }
}