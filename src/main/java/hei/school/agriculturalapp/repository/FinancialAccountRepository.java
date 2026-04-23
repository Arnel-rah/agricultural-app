package hei.school.agriculturalapp.repository;

import hei.school.agriculturalapp.model.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
@AllArgsConstructor
public class FinancialAccountRepository {
    private final Connection connection;

    public List<FinancialAccount> findAccountsByCollectivityAtDate(String collectivityId, LocalDate at) {
        String sqlAccounts = "SELECT * FROM financial_account WHERE collectivity_id = ?";
        List<FinancialAccount> accounts = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sqlAccounts)) {
            ps.setInt(1, Integer.parseInt(collectivityId));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    accounts.add(mapResultSetToAccount(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching accounts: " + e.getMessage());
        }
        for (FinancialAccount account : accounts) {
            account.setAmount(calculateBalanceInJava(account.getId(), at));
        }
        return accounts;
    }

    private double calculateBalanceInJava(String accountId, LocalDate at) {
        String sql = "SELECT amount FROM collectivity_transaction WHERE financial_account_id = ? AND payment_date <= ?";
        double sum = 0.0;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(accountId));
            ps.setDate(2, Date.valueOf(at));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    sum += rs.getDouble("amount");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error in No Push-down calc: " + e.getMessage());
        }
        return sum;
    }

    private FinancialAccount mapResultSetToAccount(ResultSet rs) throws SQLException {
        String type = rs.getString("account_type");
        FinancialAccount account;

        if ("MOBILE_BANKING".equals(type) || "MOBILE_MONEY".equals(type)) {
            MobileBankingAccount mba = new MobileBankingAccount();
            mba.setHolderName(rs.getString("holder_name"));
            mba.setMobileBankingService(rs.getString("service_name"));
            mba.setMobileNumber(rs.getString("account_details"));
            account = mba;
        } else if ("BANK".equals(type)) {
            BankAccount ba = new BankAccount();
            ba.setHolderName(rs.getString("holder_name"));
            ba.setBankName(BankName.valueOf(rs.getString("service_name")));
            ba.setBankAccountNumber(rs.getString("account_details"));
            account = ba;
        } else {
            account = new CashAccount();
        }

        account.setId(String.valueOf(rs.getInt("id")));
        return account;
    }
}