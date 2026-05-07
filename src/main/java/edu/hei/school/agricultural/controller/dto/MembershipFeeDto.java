package edu.hei.school.agricultural.controller.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class MembershipFeeDto {
    private String id;
    private String label;
    private String status;
    private String frequency;
    private LocalDate eligibleFrom;
    private Double amount;
}