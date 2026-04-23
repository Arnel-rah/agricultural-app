package hei.school.agriculturalapp.repository;

import hei.school.agriculturalapp.dto.CreateMemberPayment;
import hei.school.agriculturalapp.model.MemberPayment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDate;

@Repository
@RequiredArgsConstructor
public class PaymentRepository {

    private final Connection connection;

    public void updateBalance(String accountId, double amount) throws SQLException {
        String sql = "UPDATE financial_account SET amount = amount + ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, amount);
            stmt.setInt(2, Integer.parseInt(accountId));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Financial account not found with ID: " + accountId);
            }
        }
    }
    public MemberPayment saveMemberPayment(String memberId, CreateMemberPayment req) throws SQLException {
        String insertPaymentSql = """
        INSERT INTO member_payment (member_id, membership_fee_id, financial_account_id, amount, payment_mode, payment_date)
        VALUES (?, ?, ?, ?, ?, CURRENT_DATE) RETURNING id
        """;

        String insertTransactionSql = """
        INSERT INTO collectivity_transaction (collectivity_id, member_id, membership_fee_id, financial_account_id, amount, payment_mode)
        SELECT m.collectivity_id, ?, ?, ?, ?, ?
        FROM membership m WHERE m.member_id = ? LIMIT 1
        """;

        String selectAccountSql = "SELECT id, amount FROM financial_account WHERE id = ?";

        connection.setAutoCommit(false);

        try {
            int parsedMemberId = Integer.parseInt(memberId);
            int feeId = Integer.parseInt(req.getMembershipFeeIdentifier());
            int accId = Integer.parseInt(req.getAccountCreditedIdentifier());

            try (PreparedStatement psPayment = connection.prepareStatement(insertPaymentSql)) {
                psPayment.setInt(1, parsedMemberId);
                psPayment.setInt(2, feeId);
                psPayment.setInt(3, accId);
                psPayment.setDouble(4, req.getAmount());
                psPayment.setString(5, req.getPaymentMode().name());
                ResultSet rsPayment = psPayment.executeQuery();
                rsPayment.next();
                int generatedPaymentId = rsPayment.getInt(1);

                try (PreparedStatement psTransaction = connection.prepareStatement(insertTransactionSql)) {
                    psTransaction.setInt(1, parsedMemberId);
                    psTransaction.setInt(2, feeId);
                    psTransaction.setInt(3, accId);
                    psTransaction.setDouble(4, req.getAmount());
                    psTransaction.setString(5, req.getPaymentMode().name());
                    psTransaction.setInt(6, parsedMemberId);
                    psTransaction.executeUpdate();
                }
                updateBalance(String.valueOf(accId), req.getAmount());

                MemberPayment.AccountSummary accountSummary;
                try (PreparedStatement psAccount = connection.prepareStatement(selectAccountSql)) {
                    psAccount.setInt(1, accId);
                    ResultSet rsAccount = psAccount.executeQuery();
                    if (!rsAccount.next()) throw new SQLException("Account not found: " + accId);

                    accountSummary = new MemberPayment.AccountSummary(
                            String.valueOf(rsAccount.getInt("id")),
                            rsAccount.getDouble("amount")
                    );
                }

                connection.commit();

                MemberPayment payment = new MemberPayment();
                payment.setId(String.valueOf(generatedPaymentId));
                payment.setAmount(req.getAmount());
                payment.setPaymentMode(req.getPaymentMode().name());
                payment.setAccountCredited(accountSummary);
                payment.setCreationDate(LocalDate.now());

                return payment;
            }

        } catch (Exception e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }
}