package hei.school.agriculturalapp.repository;

import hei.school.agriculturalapp.config.DatabaseConfig;
import hei.school.agriculturalapp.dto.CreateMemberPayment;
import hei.school.agriculturalapp.model.MemberPayment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDate;

@Repository
@RequiredArgsConstructor
public class PaymentRepository {

    private final DatabaseConfig dbconfig;

    private int getAccountId(String identifier) throws SQLException {
        String sql = "SELECT id FROM financial_account WHERE id = ? OR id = ?";
        try (PreparedStatement stmt = dbconfig.connection().prepareStatement(sql)) {
            stmt.setString(1, identifier);
            stmt.setString(2, identifier);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
                throw new SQLException("Account not found: " + identifier);
            }
        }
    }

    public MemberPayment saveMemberPayment(String memberId, CreateMemberPayment req) throws SQLException {
        String insertPaymentSql = "INSERT INTO member_payment (id, member_id, membership_fee_id, financial_account_id, amount, payment_mode, payment_date, status) VALUES (?, ?, ?, ?, ?, ?, CURRENT_DATE, 'COMPLETED')";
        String insertTransactionSql = "INSERT INTO collectivity_transaction (id, collectivity_id, member_id, membership_fee_id, financial_account_id, amount, payment_mode, payment_date, status) SELECT ?, m.collectivity_id, ?, ?, ?, ?, ?, CURRENT_DATE, 'COMPLETED' FROM membership m WHERE m.member_id = ? LIMIT 1";
        String updateBalanceSql = "UPDATE financial_account SET balance = balance + ? WHERE id = ?";
        String selectAccountSql = "SELECT id, balance FROM financial_account WHERE id = ?";

        dbconfig.connection().setAutoCommit(false);
        String paymentId = "mp-" + System.currentTimeMillis();
        String txId = "tx-" + System.currentTimeMillis();

        try {
            int feeId = Integer.parseInt(req.getMembershipFeeIdentifier());
            int accId = getAccountId(req.getAccountCreditedIdentifier());

            try (PreparedStatement psPayment = dbconfig.connection().prepareStatement(insertPaymentSql)) {
                psPayment.setString(1, paymentId);
                psPayment.setString(2, memberId);
                psPayment.setInt(3, feeId);
                psPayment.setInt(4, accId);
                psPayment.setDouble(5, req.getAmount());
                psPayment.setString(6, req.getPaymentMode());
                psPayment.executeUpdate();
            }

            try (PreparedStatement psTransaction = dbconfig.connection().prepareStatement(insertTransactionSql)) {
                psTransaction.setString(1, txId);
                psTransaction.setString(2, memberId);
                psTransaction.setInt(3, feeId);
                psTransaction.setInt(4, accId);
                psTransaction.setDouble(5, req.getAmount());
                psTransaction.setString(6, req.getPaymentMode());
                psTransaction.setString(7, memberId);
                psTransaction.executeUpdate();
            }

            try (PreparedStatement psBalance = dbconfig.connection().prepareStatement(updateBalanceSql)) {
                psBalance.setDouble(1, req.getAmount());
                psBalance.setInt(2, accId);
                psBalance.executeUpdate();
            }

            MemberPayment.AccountSummary accountSummary;
            try (PreparedStatement psAccount = dbconfig.connection().prepareStatement(selectAccountSql)) {
                psAccount.setInt(1, accId);
                try (ResultSet rs = psAccount.executeQuery()) {
                    if (rs.next()) {
                        accountSummary = new MemberPayment.AccountSummary(rs.getString("id"), rs.getDouble("balance"));
                    } else {
                        throw new SQLException("Account not found");
                    }
                }
            }

            dbconfig.connection().commit();
            MemberPayment payment = new MemberPayment();
            payment.setId(paymentId);
            payment.setAmount((double) req.getAmount());
            payment.setPaymentMode(req.getPaymentMode());
            payment.setAccountCredited(accountSummary);
            payment.setCreationDate(LocalDate.now());
            return payment;

        } catch (Exception e) {
            dbconfig.connection().rollback();
            throw e;
        } finally {
            dbconfig.connection().setAutoCommit(true);
        }
    }
}