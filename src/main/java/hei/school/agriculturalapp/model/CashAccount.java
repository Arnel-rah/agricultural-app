package hei.school.agriculturalapp.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CashAccount extends FinancialAccount {
    public CashAccount() {
        super();
        setAccountType("CASH");
    }
}