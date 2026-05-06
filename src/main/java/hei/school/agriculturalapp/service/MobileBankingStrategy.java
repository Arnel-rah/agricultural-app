package hei.school.agriculturalapp.service;

import hei.school.agriculturalapp.repository.FinancialAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MobileBankingStrategy implements AccountStrategy {

    private final FinancialAccountRepository financialAccountRepository;

    @Override
    public boolean supports(String paymentMode) {
        return "MOBILE_BANKING".equals(paymentMode);
    }

    @Override
    public void credit(String accountIdentifier, Integer amount) {
        financialAccountRepository.updateBalance(accountIdentifier, amount);
    }
}