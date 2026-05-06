package hei.school.agriculturalapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RecurrenceRule {
    private Integer weekOrdinal;
    private String dayOfWeek;
}
