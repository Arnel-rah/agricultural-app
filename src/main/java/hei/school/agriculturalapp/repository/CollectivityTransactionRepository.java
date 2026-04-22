package hei.school.agriculturalapp.repository;

import hei.school.agriculturalapp.config.DatabaseConfig;
import hei.school.agriculturalapp.model.CollectivityTransaction;
import hei.school.agriculturalapp.model.FinancialAccount;
import hei.school.agriculturalapp.model.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CollectivityTransactionRepository {

    private final DatabaseConfig dbconfig;

    public List<CollectivityTransaction> findByCollectivityIdAndDateRange(
            String collectivityId, LocalDate from, LocalDate to) throws SQLException {

        List<CollectivityTransaction> transactions = new ArrayList<>();
        String sql = "SELECT ct.id, ct.amount, ct.payment_mode, ct.payment_date, " +
                "m.id as member_id, m.first_name, m.last_name, m.birth_date, m.gender, " +
                "m.address, m.profession, m.phone_number, m.email, m.occupation, " +
                "fa.id as account_id, fa.holder_name, fa.mobile_banking_service, " +
                "fa.mobile_number, fa.amount as account_amount " +
                "FROM collectivity_transaction ct " +
                "JOIN member m ON m.id = ct.member_id " +
                "JOIN financial_account fa ON fa.id = ct.financial_account_id " +
                "WHERE ct.collectivity_id = ? AND ct.payment_date BETWEEN ? AND ? " +
                "ORDER BY ct.payment_date DESC";

        try (PreparedStatement stmt = dbconfig.connection().prepareStatement(sql)) {
            stmt.setInt(1, Integer.parseInt(collectivityId));
            stmt.setDate(2, Date.valueOf(from));
            stmt.setDate(3, Date.valueOf(to));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapToTransaction(rs));
                }
            }
        }
        return transactions;
    }

    private CollectivityTransaction mapToTransaction(ResultSet rs) throws SQLException {
        CollectivityTransaction transaction = new CollectivityTransaction();
        transaction.setId(String.valueOf(rs.getInt("id")));
        transaction.setAmount(rs.getDouble("amount"));
        transaction.setPaymentMode(rs.getString("payment_mode"));
        transaction.setCreationDate(rs.getDate("payment_date").toLocalDate());

        // Account credited
        FinancialAccount account = new FinancialAccount();
        account.setId(String.valueOf(rs.getInt("account_id")));
        account.setHolderName(rs.getString("holder_name"));
        account.setMobileBankingService(rs.getString("mobile_banking_service"));
        account.setMobileNumber(rs.getString("mobile_number"));
        account.setAmount(rs.getDouble("account_amount"));
        transaction.setAccountCredited(account);

        // Member debited
        Member member = new Member();
        member.setId(rs.getInt("member_id"));
        member.setFirstName(rs.getString("first_name"));
        member.setLastName(rs.getString("last_name"));
        Date birthDate = rs.getDate("birth_date");
        if (birthDate != null) member.setBirthDate(birthDate.toLocalDate());
        member.setGender(rs.getString("gender"));
        member.setAddress(rs.getString("address"));
        member.setProfession(rs.getString("profession"));
        member.setPhoneNumber(rs.getString("phone_number"));
        member.setEmail(rs.getString("email"));
        member.setOccupation(rs.getString("occupation"));
        transaction.setMemberDebited(member);

        return transaction;
    }
}