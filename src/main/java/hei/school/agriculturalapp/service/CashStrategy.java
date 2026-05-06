package hei.school.agriculturalapp.service;

import hei.school.agriculturalapp.repository.FinancialAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CashStrategy implements AccountStrategy {

    private final FinancialAccountRepository financialAccountRepository;

    @Override
    public boolean supports(String paymentMode) {
        return "CASH".equals(paymentMode);
    }

    @Override
    public void credit(String accountIdentifier, Integer amount) {
        financialAccountRepository.updateBalance(accountIdentifier, amount);
    }
}