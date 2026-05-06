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
public class MemberPayment {
    private String id;
    private Double amount;
    private String paymentMode;
    private AccountSummary accountCredited;
    private LocalDate creationDate;

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class AccountSummary {
        private String id;
        private Double balance;
    }
}