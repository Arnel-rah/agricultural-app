package hei.school.agriculturalapp.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MobileBankingAccount extends FinancialAccount {
    private String holderName;
    private String mobileNumber;

    public MobileBankingAccount() {
        super();
        setAccountType("MOBILE_BANKING");
    }
}