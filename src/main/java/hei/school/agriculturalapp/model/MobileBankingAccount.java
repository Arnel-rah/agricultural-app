package hei.school.agriculturalapp.model;

import lombok.Data;

@Data
public class MobileBankingAccount extends FinancialAccount {
    private String holderName;
    private String mobileService;
}
