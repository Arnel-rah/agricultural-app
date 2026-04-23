package hei.school.agriculturalapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberPayment {
    private String id;
    private Double amount;
    private String paymentMode;
    private AccountSummary accountCredited;
    private LocalDate creationDate;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AccountSummary {
        private String id;
        private Double amount;
    }
}