package edu.hei.school.agricultural.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Activity {
    private String id;
    private String label;
    private String activityType;
    private LocalDate executiveDate;
    private Integer recurrenceRuleWeekOrdinal;
    private String recurrenceRuleDayOfWeek;
}