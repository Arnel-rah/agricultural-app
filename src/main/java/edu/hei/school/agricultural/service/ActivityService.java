package edu.hei.school.agricultural.service;

import edu.hei.school.agricultural.controller.dto.CollectivityActivity;
import edu.hei.school.agricultural.controller.dto.CreateCollectivityActivity;
import edu.hei.school.agricultural.controller.dto.MonthlyRecurrenceRule;
import edu.hei.school.agricultural.entity.Activity;
import edu.hei.school.agricultural.entity.Collectivity;
import edu.hei.school.agricultural.exception.BadRequestException;
import edu.hei.school.agricultural.exception.NotFoundException;
import edu.hei.school.agricultural.repository.ActivityRepository;
import edu.hei.school.agricultural.repository.CollectivityActivityRepository;
import edu.hei.school.agricultural.repository.CollectivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.util.UUID.randomUUID;

@Service
@RequiredArgsConstructor
public class ActivityService {
    private final ActivityRepository activityRepository;
    private final CollectivityRepository collectivityRepository;
    private final CollectivityActivityRepository collectivityActivityRepository;

    public List<CollectivityActivity> addActivities(String collectivityId, List<CreateCollectivityActivity> createActivities) {
        Collectivity collectivity = collectivityRepository.findById(collectivityId)
                .orElseThrow(() -> new NotFoundException("Collectivity.id=" + collectivityId + " not found"));

        List<CollectivityActivity> result = new ArrayList<>();

        for (CreateCollectivityActivity createActivity : createActivities) {
            if (createActivity.getRecurrenceRule() != null && createActivity.getExecutiveDate() != null) {
                throw new BadRequestException("Cannot provide both recurrenceRule and executiveDate");
            }

            Activity activity = Activity.builder()
                    .id(randomUUID().toString())
                    .label(createActivity.getLabel())
                    .activityType(createActivity.getActivityType())
                    .executiveDate(createActivity.getExecutiveDate())
                    .build();

            if (createActivity.getRecurrenceRule() != null) {
                MonthlyRecurrenceRule rule = createActivity.getRecurrenceRule();
                activity.setRecurrenceRuleWeekOrdinal(rule.getWeekOrdinal());
                activity.setRecurrenceRuleDayOfWeek(rule.getDayOfWeek());
            }

            activityRepository.save(activity);
            collectivityActivityRepository.linkActivityToCollectivity(collectivityId, activity.getId());

            CollectivityActivity resultActivity = CollectivityActivity.builder()
                    .id(activity.getId())
                    .label(activity.getLabel())
                    .activityType(activity.getActivityType())
                    .executiveDate(activity.getExecutiveDate())
                    .build();

            result.add(resultActivity);
        }

        return result;
    }

    public List<CollectivityActivity> getActivities(String collectivityId) {
        Collectivity collectivity = collectivityRepository.findById(collectivityId)
                .orElseThrow(() -> new NotFoundException("Collectivity.id=" + collectivityId + " not found"));

        List<Activity> activities = collectivityActivityRepository.findActivitiesByCollectivityId(collectivityId);
        List<CollectivityActivity> result = new ArrayList<>();

        for (Activity activity : activities) {
            CollectivityActivity collectivityActivity = CollectivityActivity.builder()
                    .id(activity.getId())
                    .label(activity.getLabel())
                    .activityType(activity.getActivityType())
                    .executiveDate(activity.getExecutiveDate())
                    .build();
            result.add(collectivityActivity);
        }

        return result;
    }
}