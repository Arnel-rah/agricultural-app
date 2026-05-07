package edu.hei.school.agricultural.repository;

import edu.hei.school.agricultural.controller.dto.FinancialAccountDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class FinancialAccountRepository {
    private final Connection connection;

    public List<FinancialAccountDto> findByCollectivityId(String collectivityId, LocalDate atDate) {
        List<FinancialAccountDto> accounts = new ArrayList<>();
        String sql = """
            SELECT id, account_type, holder_name, phone_number, bank_name, 
                   bank_code, agency_code, account_number, rib_key, balance
            FROM financial_account
            WHERE collectivity_id = ?
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, collectivityId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String type = rs.getString("account_type");
                FinancialAccountDto account = new FinancialAccountDto();
                account.setId(rs.getString("id"));
                account.setAmount(rs.getDouble("balance"));

                if ("CASH".equals(type)) {
                    account.setType("CASH");
                } else if ("BANK".equals(type)) {
                    account.setType("BANK");
                    account.setHolderName(rs.getString("holder_name"));
                    account.setBankName(rs.getString("bank_name"));
                    account.setBankCode(rs.getInt("bank_code"));
                    account.setBankBranchCode(rs.getInt("agency_code"));
                    account.setBankAccountNumber(rs.getLong("account_number"));
                    account.setBankAccountKey(rs.getInt("rib_key"));
                } else if (type.equals("ORANGE_MONEY") || type.equals("MVOLA") || type.equals("AIRTEL_MONEY")) {
                    account.setType("MOBILE_BANKING");
                    account.setHolderName(rs.getString("holder_name"));
                    account.setMobileBankingService(type);
                    account.setMobileNumber(rs.getString("phone_number"));
                }
                accounts.add(account);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return accounts;
    }
}