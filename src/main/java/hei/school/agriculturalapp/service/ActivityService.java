package hei.school.agriculturalapp.service;

import hei.school.agriculturalapp.dto.CreateActivity;
import hei.school.agriculturalapp.exception.BadRequestException;
import hei.school.agriculturalapp.exception.NotFoundException;
import hei.school.agriculturalapp.model.Activity;
import hei.school.agriculturalapp.repository.ActivityRepository;
import hei.school.agriculturalapp.repository.CollectivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final CollectivityRepository collectivityRepository;

    public List<Activity> createActivities(String collectivityId, List<CreateActivity> requests) throws SQLException {
        if (!collectivityRepository.existsById(collectivityId)) {
            throw new NotFoundException("Collectivity not found");
        }

        List<Activity> created = new ArrayList<>();
        for (CreateActivity req : requests) {
            validateActivityRequest(req);
            Activity activity = activityRepository.save(collectivityId, req);
            created.add(activity);
        }
        return created;
    }

    public List<Activity> getActivities(String collectivityId) throws SQLException {
        if (!collectivityRepository.existsById(collectivityId)) {
            throw new NotFoundException("Collectivity not found");
        }
        return activityRepository.findByCollectivityId(collectivityId);
    }

    public Activity getActivityById(String activityId) throws SQLException {
        Activity activity = activityRepository.findById(activityId);
        if (activity == null) {
            throw new NotFoundException("Activity not found");
        }
        return activity;
    }

    private void validateActivityRequest(CreateActivity req) {
        boolean hasRecurrence = req.getRecurrenceRule() != null;
        boolean hasExecutiveDate = req.getExecutiveDate() != null;

        if (hasRecurrence && hasExecutiveDate) {
            throw new BadRequestException("Both recurrence rule and executive date cannot be provided at the same time");
        }
        if (!hasRecurrence && !hasExecutiveDate) {
            throw new BadRequestException("Either recurrence rule or executive date must be provided");
        }

        String activityType = req.getActivityType();
        if (!activityType.equals("MEETING") && !activityType.equals("TRAINING") && !activityType.equals("OTHER")) {
            throw new BadRequestException("Invalid activity type: " + activityType);
        }
    }
}