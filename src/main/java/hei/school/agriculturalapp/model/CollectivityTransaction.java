package hei.school.agriculturalapp.model;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CollectivityTransaction {
    private String id;
    private LocalDate creationDate;
    private Double amount;
    private String paymentMode;
    private FinancialAccount accountCredited;
    private Member memberDebited;
}