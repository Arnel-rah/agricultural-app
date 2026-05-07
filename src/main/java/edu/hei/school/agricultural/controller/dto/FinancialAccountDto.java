package edu.hei.school.agricultural.controller.dto;

import lombok.Data;

@Data
public class FinancialAccountDto {
    private String id;
    private String type;
    private Double amount;
    private String holderName;
    private String bankName;
    private Integer bankCode;
    private Integer bankBranchCode;
    private Long bankAccountNumber;
    private Integer bankAccountKey;
    private String mobileBankingService;
    private String mobileNumber;
}