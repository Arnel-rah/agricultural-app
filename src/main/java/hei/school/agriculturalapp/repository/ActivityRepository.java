package hei.school.agriculturalapp.repository;

import hei.school.agriculturalapp.config.DatabaseConfig;
import hei.school.agriculturalapp.dto.CreateActivity;
import hei.school.agriculturalapp.model.Activity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ActivityRepository {

    private final DatabaseConfig dbconfig;

    public Activity save(String collectivityId, CreateActivity dto) throws SQLException {
        String sql = "INSERT INTO activity (id, collectivity_id, label, activity_type, member_occupation_concerned, recurrence_week_ordinal, recurrence_day_of_week, executive_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String id = "act-" + System.currentTimeMillis();

        String occupationConcerned = null;
        if (dto.getMemberOccupationConcerned() != null && !dto.getMemberOccupationConcerned().isEmpty()) {
            occupationConcerned = String.join(",", dto.getMemberOccupationConcerned());
        }

        Integer weekOrdinal = null;
        String dayOfWeek = null;
        LocalDate executiveDate = null;

        if (dto.getRecurrenceRule() != null) {
            weekOrdinal = dto.getRecurrenceRule().getWeekOrdinal();
            dayOfWeek = dto.getRecurrenceRule().getDayOfWeek();
        } else if (dto.getExecutiveDate() != null) {
            executiveDate = dto.getExecutiveDate();
        }

        try (PreparedStatement stmt = dbconfig.connection().prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.setString(2, collectivityId);
            stmt.setString(3, dto.getLabel());
            stmt.setString(4, dto.getActivityType());
            stmt.setString(5, occupationConcerned);
            if (weekOrdinal != null) stmt.setInt(6, weekOrdinal);
            else stmt.setNull(6, Types.INTEGER);
            stmt.setString(7, dayOfWeek);
            if (executiveDate != null) stmt.setDate(8, Date.valueOf(executiveDate));
            else stmt.setNull(8, Types.DATE);
            stmt.executeUpdate();
        }

        Activity activity = new Activity();
        activity.setId(id);
        activity.setCollectivityId(collectivityId);
        activity.setLabel(dto.getLabel());
        activity.setActivityType(dto.getActivityType());
        activity.setMemberOccupationConcerned(dto.getMemberOccupationConcerned());
        activity.setRecurrenceWeekOrdinal(weekOrdinal);
        activity.setRecurrenceDayOfWeek(dayOfWeek);
        activity.setExecutiveDate(executiveDate);
        return activity;
    }

    public List<Activity> findByCollectivityId(String collectivityId) throws SQLException {
        List<Activity> activities = new ArrayList<>();
        String sql = "SELECT id, collectivity_id, label, activity_type, member_occupation_concerned, recurrence_week_ordinal, recurrence_day_of_week, executive_date FROM activity WHERE collectivity_id = ? ORDER BY id";

        try (PreparedStatement stmt = dbconfig.connection().prepareStatement(sql)) {
            stmt.setString(1, collectivityId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    activities.add(mapToActivity(rs));
                }
            }
        }
        return activities;
    }

    public Activity findById(String activityId) throws SQLException {
        String sql = "SELECT id, collectivity_id, label, activity_type, member_occupation_concerned, recurrence_week_ordinal, recurrence_day_of_week, executive_date FROM activity WHERE id = ?";

        try (PreparedStatement stmt = dbconfig.connection().prepareStatement(sql)) {
            stmt.setString(1, activityId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapToActivity(rs);
                }
            }
        }
        return null;
    }

    public boolean existsById(String activityId) throws SQLException {
        String sql = "SELECT 1 FROM activity WHERE id = ?";
        try (PreparedStatement stmt = dbconfig.connection().prepareStatement(sql)) {
            stmt.setString(1, activityId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private Activity mapToActivity(ResultSet rs) throws SQLException {
        Activity activity = new Activity();
        activity.setId(rs.getString("id"));
        activity.setCollectivityId(rs.getString("collectivity_id"));
        activity.setLabel(rs.getString("label"));
        activity.setActivityType(rs.getString("activity_type"));

        String occupations = rs.getString("member_occupation_concerned");
        if (occupations != null && !occupations.isEmpty()) {
            activity.setMemberOccupationConcerned(List.of(occupations.split(",")));
        }

        int weekOrdinal = rs.getInt("recurrence_week_ordinal");
        if (!rs.wasNull()) activity.setRecurrenceWeekOrdinal(weekOrdinal);
        activity.setRecurrenceDayOfWeek(rs.getString("recurrence_day_of_week"));

        Date execDate = rs.getDate("executive_date");
        if (execDate != null) activity.setExecutiveDate(execDate.toLocalDate());

        return activity;
    }
}