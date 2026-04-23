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
        String sql = """
                SELECT ct.id, ct.amount, ct.payment_mode, ct.creation_date, 
                       m.id as member_id, m.first_name, m.last_name, m.birth_date, m.gender, 
                       m.address, m.profession, m.phone_number, m.email, m.occupation, 
                       fa.id as acc_id, fa.balance as acc_balance
                FROM collectivity_transaction ct 
                JOIN member m ON m.id = ct.member_id 
                JOIN financial_account fa ON fa.id = ct.account_id 
                WHERE ct.collectivity_id = ? AND ct.creation_date BETWEEN ? AND ? 
                ORDER BY ct.creation_date DESC
                """;

        Connection connection = dbconfig.connection();
        PreparedStatement stmt = connection.prepareStatement(sql);

        stmt.setInt(1, Integer.parseInt(collectivityId));
        stmt.setDate(2, Date.valueOf(from));
        stmt.setDate(3, Date.valueOf(to));

        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            transactions.add(mapToTransaction(rs));
        }

        rs.close();
        stmt.close();

        return transactions;
    }

    private CollectivityTransaction mapToTransaction(ResultSet rs) throws SQLException {
        CollectivityTransaction transaction = new CollectivityTransaction();
        transaction.setId(String.valueOf(rs.getInt("id")));
        transaction.setAmount(rs.getDouble("amount"));
        transaction.setPaymentMode(rs.getString("payment_mode"));
        transaction.setCreationDate(rs.getDate("creation_date").toLocalDate());

        FinancialAccount account = new FinancialAccount();
        account.setId(rs.getString("acc_id"));
        account.setAmount(rs.getDouble("acc_balance"));
        transaction.setAccountCredited(account);

        Member member = new Member();
        member.setId(rs.getInt("member_id"));
        member.setFirstName(rs.getString("first_name"));
        member.setLastName(rs.getString("last_name"));
        Date birthDate = rs.getDate("birth_date");
        if (birthDate != null) {
            member.setBirthDate(birthDate.toLocalDate());
        }
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