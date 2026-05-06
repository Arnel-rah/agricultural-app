package hei.school.agriculturalapp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public abstract class FinancialAccount {
    private String id;
    private String collectivityId;
    private String accountType;
    private Double amount;
}