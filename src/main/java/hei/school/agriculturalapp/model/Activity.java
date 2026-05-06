package hei.school.agriculturalapp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Activity {
    private String id;
    private String collectivityId;
    private String label;
    private String activityType;
    private List<String> memberOccupationConcerned;
    private Integer recurrenceWeekOrdinal;
    private String recurrenceDayOfWeek;
    private LocalDate executiveDate;
}