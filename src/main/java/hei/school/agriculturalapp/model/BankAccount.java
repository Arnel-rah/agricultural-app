package hei.school.agriculturalapp.model;

import hei.school.agriculturalapp.model.BankName;
import hei.school.agriculturalapp.model.FinancialAccount;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BankAccount extends FinancialAccount {
    private String holderName;
    private BankName bankName;

    private String bankCode;
    private String bankBranchCode;
    private String bankAccountNumber;
    private String bankAccountKey;
}