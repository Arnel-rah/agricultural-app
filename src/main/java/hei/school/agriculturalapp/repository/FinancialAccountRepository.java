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
        String sqlAccounts = "SELECT id, collectivity_id, account_type, holder_name, mobile_number, balance FROM financial_account WHERE collectivity_id = ?";
        List<FinancialAccount> accounts = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sqlAccounts)) {
            ps.setString(1, collectivityId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    accounts.add(mapResultSetToAccount(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching accounts: " + e.getMessage());
        }

        for (FinancialAccount account : accounts) {
            double balanceAtDate = calculateBalanceAtDate(account.getId(), at);
            account.setAmount(balanceAtDate);
        }
        return accounts;
    }

    private double calculateBalanceAtDate(String accountId, LocalDate at) {
        String sql = "SELECT COALESCE(SUM(amount), 0) as total FROM collectivity_transaction WHERE financial_account_id = ? AND payment_date <= ? AND status = 'COMPLETED'";
        double sum = 0.0;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, accountId);
            ps.setDate(2, Date.valueOf(at));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    sum = rs.getDouble("total");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error calculating balance: " + e.getMessage());
        }
        return sum;
    }

    private FinancialAccount mapResultSetToAccount(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String collectivityId = rs.getString("collectivity_id");
        String accountType = rs.getString("account_type");
        String holderName = rs.getString("holder_name");
        String mobileNumber = rs.getString("mobile_number");
        double balance = rs.getDouble("balance");

        if ("MOBILE_BANKING".equals(accountType)) {
            MobileBankingAccount account = new MobileBankingAccount();
            account.setId(id);
            account.setCollectivityId(collectivityId);
            account.setAccountType(accountType);
            account.setHolderName(holderName);
            account.setMobileNumber(mobileNumber);
            account.setAmount(balance);
            return account;
        } else if ("BANK".equals(accountType)) {
            BankAccount account = new BankAccount();
            account.setId(id);
            account.setCollectivityId(collectivityId);
            account.setAccountType(accountType);
            account.setHolderName(holderName);
            account.setAmount(balance);
            return account;
        } else {
            CashAccount account = new CashAccount();
            account.setId(id);
            account.setCollectivityId(collectivityId);
            account.setAccountType(accountType);
            account.setAmount(balance);
            return account;
        }
    }

    public void updateBalance(String accountId, Integer amount) {
        String sql = "UPDATE financial_account SET balance = balance + ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, amount);
            stmt.setString(2, accountId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating balance: " + e.getMessage());
        }
    }
}