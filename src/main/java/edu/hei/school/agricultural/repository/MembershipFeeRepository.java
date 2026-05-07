package edu.hei.school.agricultural.repository;

import edu.hei.school.agricultural.controller.dto.ActivityStatus;
import edu.hei.school.agricultural.controller.dto.CreateMembershipFee;
import edu.hei.school.agricultural.controller.dto.MembershipFeeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static java.util.UUID.randomUUID;

@Repository
@RequiredArgsConstructor
public class MembershipFeeRepository {
    private final Connection connection;

    public List<MembershipFeeDto> findByCollectivityId(String collectivityId) {
        List<MembershipFeeDto> fees = new ArrayList<>();
        String sql = """
            SELECT mf.id, mf.label, mf.status, mf.frequency, mf.eligible_from, mf.amount
            FROM membership_fee mf
            JOIN collectivity_membership_fee cmf ON mf.id = cmf.membership_fee_id
            WHERE cmf.collectivity_id = ?
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, collectivityId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                MembershipFeeDto fee = new MembershipFeeDto();
                fee.setId(rs.getString("id"));
                fee.setLabel(rs.getString("label"));
                fee.setStatus(String.valueOf(ActivityStatus.valueOf(rs.getString("status"))));
                fee.setFrequency(rs.getString("frequency"));
                fee.setEligibleFrom(rs.getDate("eligible_from").toLocalDate());
                fee.setAmount(rs.getDouble("amount"));
                fees.add(fee);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return fees;
    }

    public MembershipFeeDto save(String collectivityId, CreateMembershipFee createFee) {
        String id = randomUUID().toString();
        String sql = "INSERT INTO membership_fee (id, label, status, frequency, eligible_from, amount) VALUES (?, ?, 'ACTIVE', ?, ?, ?)";
        String linkSql = "INSERT INTO collectivity_membership_fee (id, collectivity_id, membership_fee_id) VALUES (?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, createFee.getLabel());
            ps.setString(3, String.valueOf(createFee.getFrequency()));
            ps.setDate(4, java.sql.Date.valueOf(createFee.getEligibleFrom()));
            ps.setDouble(5, createFee.getAmount());
            ps.executeUpdate();

            try (PreparedStatement ps2 = connection.prepareStatement(linkSql)) {
                ps2.setString(1, randomUUID().toString());
                ps2.setString(2, collectivityId);
                ps2.setString(3, id);
                ps2.executeUpdate();
            }

            MembershipFeeDto fee = new MembershipFeeDto();
            fee.setId(id);
            fee.setLabel(createFee.getLabel());
            fee.setStatus(String.valueOf(ActivityStatus.ACTIVE));
            fee.setFrequency(String.valueOf(createFee.getFrequency()));
            fee.setEligibleFrom(createFee.getEligibleFrom());
            fee.setAmount(createFee.getAmount());
            return fee;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}