package hei.school.agriculturalapp.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class CreateMembershipFee {
    private LocalDate eligibleFrom;
    private String frequency;
    private Double amount;
    private String label;
}