package hei.school.agriculturalapp.service;

import hei.school.agriculturalapp.model.PaymentMode;
import hei.school.agriculturalapp.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

@Component
@RequiredArgsConstructor
public class BankTransferStrategy implements AccountStrategy {
    private final PaymentRepository repository;

    @Override
    public boolean supports(PaymentMode mode) {
        return mode == PaymentMode.BANK_TRANSFER;
    }

    @Override
    public void credit(String accountId, double amount) throws SQLException {
        repository.updateBalance(accountId, amount);
    }
}