package hei.school.agriculturalapp.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BankAccount extends FinancialAccount {
    private String holderName;
    private String bankName;
    private String bankAccountNumber;

    public BankAccount() {
        super();
        setAccountType("BANK");
    }
}