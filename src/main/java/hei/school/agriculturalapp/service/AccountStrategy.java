package hei.school.agriculturalapp.service;

import hei.school.agriculturalapp.model.PaymentMode;
import java.sql.SQLException;

public interface AccountStrategy {
    boolean supports(PaymentMode mode);
    void credit(String accountId, double amount) throws SQLException;
}