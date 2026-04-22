package hei.school.agriculturalapp.repository;

import hei.school.agriculturalapp.config.DatabaseConfig;
import hei.school.agriculturalapp.dto.CreateMembershipFee;
import hei.school.agriculturalapp.model.MembershipFee;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MembershipFeeRepository {

    private final DatabaseConfig dbconfig;

    public MembershipFee save(String collectivityId, CreateMembershipFee dto) throws SQLException {
        String sql = "INSERT INTO membership_fee (collectivity_id, eligible_from, frequency, amount, label, status) VALUES (?, ?, ?, ?, ?, 'ACTIVE') RETURNING id";

        try (PreparedStatement stmt = dbconfig.connection().prepareStatement(sql)) {
            stmt.setInt(1, Integer.parseInt(collectivityId));
            stmt.setDate(2, Date.valueOf(dto.getEligibleFrom()));
            stmt.setString(3, dto.getFrequency());
            stmt.setDouble(4, dto.getAmount());
            stmt.setString(5, dto.getLabel());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    MembershipFee fee = new MembershipFee();
                    fee.setId(String.valueOf(rs.getInt("id")));
                    fee.setEligibleFrom(dto.getEligibleFrom());
                    fee.setFrequency(dto.getFrequency());
                    fee.setAmount(dto.getAmount());
                    fee.setLabel(dto.getLabel());
                    fee.setStatus("ACTIVE");
                    return fee;
                }
            }
        }
        throw new SQLException("Failed to create membership fee");
    }

    public List<MembershipFee> findByCollectivityId(String collectivityId) throws SQLException {
        List<MembershipFee> fees = new ArrayList<>();
        String sql = "SELECT id, eligible_from, frequency, amount, label, status FROM membership_fee WHERE collectivity_id = ? ORDER BY eligible_from DESC";

        try (PreparedStatement stmt = dbconfig.connection().prepareStatement(sql)) {
            stmt.setInt(1, Integer.parseInt(collectivityId));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    MembershipFee fee = new MembershipFee();
                    fee.setId(String.valueOf(rs.getInt("id")));
                    fee.setEligibleFrom(rs.getDate("eligible_from").toLocalDate());
                    fee.setFrequency(rs.getString("frequency"));
                    fee.setAmount(rs.getDouble("amount"));
                    fee.setLabel(rs.getString("label"));
                    fee.setStatus(rs.getString("status"));
                    fees.add(fee);
                }
            }
        }
        return fees;
    }

    public Optional<MembershipFee> findById(String id) throws SQLException {
        String sql = "SELECT id, eligible_from, frequency, amount, label, status FROM membership_fee WHERE id = ?";

        try (PreparedStatement stmt = dbconfig.connection().prepareStatement(sql)) {
            stmt.setInt(1, Integer.parseInt(id));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    MembershipFee fee = new MembershipFee();
                    fee.setId(String.valueOf(rs.getInt("id")));
                    fee.setEligibleFrom(rs.getDate("eligible_from").toLocalDate());
                    fee.setFrequency(rs.getString("frequency"));
                    fee.setAmount(rs.getDouble("amount"));
                    fee.setLabel(rs.getString("label"));
                    fee.setStatus(rs.getString("status"));
                    return Optional.of(fee);
                }
            }
        }
        return Optional.empty();
    }
}