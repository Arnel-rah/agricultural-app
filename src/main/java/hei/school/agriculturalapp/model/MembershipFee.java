package hei.school.agriculturalapp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MembershipFee {
    private String id;
    private String collectivityId;
    private LocalDate eligibleFrom;
    private String frequency;
    private Double amount;
    private String label;
    private String status;
}