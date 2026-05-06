package hei.school.agriculturalapp.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateMemberPayment {
    private Integer amount;
    private String membershipFeeIdentifier;
    private String accountCreditedIdentifier;
    private String paymentMode;
}