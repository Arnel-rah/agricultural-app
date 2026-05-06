package hei.school.agriculturalapp.repository;

import hei.school.agriculturalapp.config.DatabaseConfig;
import hei.school.agriculturalapp.model.Collectivity;
import hei.school.agriculturalapp.model.MemberDescription;
import hei.school.agriculturalapp.model.MembershipFee;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class StatisticsRepository {

    private final DatabaseConfig dbconfig;

    public double getMemberEarnedAmount(String memberId, LocalDate from, LocalDate to) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0) as total FROM member_payment WHERE member_id = ? AND payment_date BETWEEN ? AND ? AND status = 'COMPLETED'";
        try (PreparedStatement stmt = dbconfig.connection().prepareStatement(sql)) {
            stmt.setString(1, memberId);
            stmt.setDate(2, Date.valueOf(from));
            stmt.setDate(3, Date.valueOf(to));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getDouble("total");
            }
        }
        return 0;
    }

    public double getMemberUnpaidAmount(String memberId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(mf.amount), 0) as total FROM membership_fee mf WHERE mf.collectivity_id = (SELECT collectivity_id FROM membership WHERE member_id = ? LIMIT 1) AND mf.status = 'ACTIVE' AND mf.id NOT IN (SELECT mp.membership_fee_id FROM member_payment mp WHERE mp.member_id = ? AND mp.status = 'COMPLETED')";
        try (PreparedStatement stmt = dbconfig.connection().prepareStatement(sql)) {
            stmt.setString(1, memberId);
            stmt.setString(2, memberId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getDouble("total");
            }
        }
        return 0;
    }

    public List<MemberDescription> getMemberDescriptionsByCollectivityId(String collectivityId) throws SQLException {
        List<MemberDescription> members = new ArrayList<>();
        String sql = "SELECT m.id, m.first_name, m.last_name, m.email, m.occupation FROM member m JOIN membership ms ON ms.member_id = m.id WHERE ms.collectivity_id = ? ORDER BY m.id";
        try (PreparedStatement stmt = dbconfig.connection().prepareStatement(sql)) {
            stmt.setString(1, collectivityId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    MemberDescription md = new MemberDescription();
                    md.setId(rs.getString("id"));
                    md.setFirstName(rs.getString("first_name"));
                    md.setLastName(rs.getString("last_name"));
                    md.setEmail(rs.getString("email"));
                    md.setOccupation(rs.getString("occupation"));
                    members.add(md);
                }
            }
        }
        return members;
    }

    public int getNewMembersCount(String collectivityId, LocalDate from, LocalDate to) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM membership WHERE collectivity_id = ? AND joined_at BETWEEN ? AND ?";
        try (PreparedStatement stmt = dbconfig.connection().prepareStatement(sql)) {
            stmt.setString(1, collectivityId);
            stmt.setDate(2, Date.valueOf(from));
            stmt.setDate(3, Date.valueOf(to));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("count");
            }
        }
        return 0;
    }

    public double getMemberCurrentDuePercentage(String collectivityId) throws SQLException {
        List<MemberDescription> members = getMemberDescriptionsByCollectivityId(collectivityId);
        if (members.isEmpty()) return 0;

        List<MembershipFee> activeFees = getActiveMembershipFees(collectivityId);
        if (activeFees.isEmpty()) return 100.0;

        int membersUpToDate = 0;
        for (MemberDescription member : members) {
            boolean isUpToDate = true;
            for (MembershipFee fee : activeFees) {
                if (!hasMemberPaidFee(member.getId(), fee.getId())) {
                    isUpToDate = false;
                    break;
                }
            }
            if (isUpToDate) membersUpToDate++;
        }
        return (double) membersUpToDate / members.size() * 100;
    }

    private boolean hasMemberPaidFee(String memberId, String feeId) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM member_payment WHERE member_id = ? AND membership_fee_id = ? AND status = 'COMPLETED'";
        try (PreparedStatement stmt = dbconfig.connection().prepareStatement(sql)) {
            stmt.setString(1, memberId);
            stmt.setString(2, feeId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("count") > 0;
            }
        }
        return false;
    }

    private List<MembershipFee> getActiveMembershipFees(String collectivityId) throws SQLException {
        List<MembershipFee> fees = new ArrayList<>();
        String sql = "SELECT id, eligible_from, frequency, amount, label, status FROM membership_fee WHERE collectivity_id = ? AND status = 'ACTIVE'";
        try (PreparedStatement stmt = dbconfig.connection().prepareStatement(sql)) {
            stmt.setString(1, collectivityId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    MembershipFee fee = new MembershipFee();
                    fee.setId(rs.getString("id"));
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
}