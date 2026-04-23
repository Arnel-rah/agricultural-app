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
public class CollectivityTransaction {
    private String id;
    private LocalDate creationDate;
    private Double amount;
    private String paymentMode;
    private FinancialAccount accountCredited;
    private Member memberDebited;
}