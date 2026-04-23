package hei.school.agriculturalapp.validator;

import hei.school.agriculturalapp.dto.CreateMemberPayment;
import hei.school.agriculturalapp.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Consumer;

@Component
public class PaymentValidator implements Consumer<List<CreateMemberPayment>> {

    @Override
    public void accept(List<CreateMemberPayment> requests) {
        for (CreateMemberPayment req : requests) {
            validateGeneralFields(req);
            validateSpecificBusinessRules(req);
        }
    }

    private void validateGeneralFields(CreateMemberPayment req) {
        if (req.getAmount() == null || req.getAmount() <= 0) {
            throw new BadRequestException("Amount must be greater than 0");
        }
        if (req.getMembershipFeeIdentifier() == null || req.getMembershipFeeIdentifier().isBlank()) {
            throw new BadRequestException("Membership fee identifier is required");
        }
        if (req.getAccountCreditedIdentifier() == null || req.getAccountCreditedIdentifier().isBlank()) {
            throw new BadRequestException("Credited account identifier is required");
        }
        if (req.getPaymentMode() == null) {
            throw new BadRequestException("Payment mode is required (CASH, MOBILE_BANKING, or BANK_TRANSFER)");
        }
    }

    private void validateSpecificBusinessRules(CreateMemberPayment req) {
        if (req.getAmount() < 1000) {
            throw new BadRequestException("Le montant est trop faible pour une cotisation agricole.");
        }
    }
}