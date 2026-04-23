package hei.school.agriculturalapp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FinancialAccount {
    private String id;
    private String holderName;
    private String mobileBankingService;
    private String mobileNumber;
    private Double amount;
}