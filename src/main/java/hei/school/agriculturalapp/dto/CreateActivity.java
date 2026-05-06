package hei.school.agriculturalapp.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class CreateActivity {
    private String label;
    private String activityType;
    private List<String> memberOccupationConcerned;
    private RecurrenceRule recurrenceRule;
    private LocalDate executiveDate;

    @Getter
    @Setter
    public static class RecurrenceRule {
        private Integer weekOrdinal;
        private String dayOfWeek;
    }
}