package hei.school.agriculturalapp.repository;

import hei.school.agriculturalapp.config.DatabaseConfig;
import hei.school.agriculturalapp.dto.CreateMembershipFee;
import hei.school.agriculturalapp.model.MembershipFee;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MembershipFeeRepository {

    private final DatabaseConfig dbconfig;

    public List<MembershipFee> findByCollectivityId(String collectivityId) throws SQLException {
        List<MembershipFee> fees = new ArrayList<>();
        String sql = "SELECT id, collectivity_id, eligible_from, frequency, amount, label, status FROM membership_fee WHERE collectivity_id = ? ORDER BY eligible_from DESC";
        try (PreparedStatement stmt = dbconfig.connection().prepareStatement(sql)) {
            stmt.setString(1, collectivityId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    fees.add(mapToMembershipFee(rs));
                }
            }
        }
        return fees;
    }

    public List<MembershipFee> saveAll(String collectivityId, List<CreateMembershipFee> requests) throws SQLException {
        List<MembershipFee> saved = new ArrayList<>();
        String sql = "INSERT INTO membership_fee (id, collectivity_id, eligible_from, frequency, amount, label, status) VALUES (?, ?, ?, ?, ?, ?, 'ACTIVE')";
        try (PreparedStatement stmt = dbconfig.connection().prepareStatement(sql)) {
            for (CreateMembershipFee req : requests) {
                String id = "cot-" + System.currentTimeMillis() + saved.size();
                stmt.setString(1, id);
                stmt.setString(2, collectivityId);
                stmt.setDate(3, Date.valueOf(req.getEligibleFrom()));
                stmt.setString(4, req.getFrequency());
                stmt.setDouble(5, req.getAmount());
                stmt.setString(6, req.getLabel());
                stmt.addBatch();
                MembershipFee fee = new MembershipFee();
                fee.setId(id);
                fee.setCollectivityId(collectivityId);
                fee.setEligibleFrom(req.getEligibleFrom());
                fee.setFrequency(req.getFrequency());
                fee.setAmount(req.getAmount());
                fee.setLabel(req.getLabel());
                fee.setStatus("ACTIVE");
                saved.add(fee);
            }
            stmt.executeBatch();
        }
        return saved;
    }

    private MembershipFee mapToMembershipFee(ResultSet rs) throws SQLException {
        MembershipFee fee = new MembershipFee();
        fee.setId(rs.getString("id"));
        fee.setCollectivityId(rs.getString("collectivity_id"));
        fee.setEligibleFrom(rs.getDate("eligible_from").toLocalDate());
        fee.setFrequency(rs.getString("frequency"));
        fee.setAmount(rs.getDouble("amount"));
        fee.setLabel(rs.getString("label"));
        fee.setStatus(rs.getString("status"));
        return fee;
    }
}