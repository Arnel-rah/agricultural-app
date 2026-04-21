package hei.school.agriculturalapp.repository;

import hei.school.agriculturalapp.model.Member;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class MemberRepository {

    private final Connection connection;

    public MemberRepository(Connection connection) {
        this.connection = connection;
    }

    public int save(Member member) throws SQLException {
        String sql = "INSERT INTO member (first_name, last_name, birth_date, gender, phone_number, " +
                "registration_fee_paid, membership_dues_paid, admission_status, address, profession, email) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, member.getFirstName());
            stmt.setString(2, member.getLastName());
            stmt.setDate(3, Date.valueOf(member.getBirthDate()));
            stmt.setString(4, member.getGender());
            stmt.setString(5, member.getPhoneNumber());
            stmt.setBoolean(6, member.isRegistrationFeePaid());
            stmt.setBoolean(7, member.isMembershipDuesPaid());
            stmt.setString(8, member.getAdmissionStatus() != null ? member.getAdmissionStatus() : "APPROVED");
            stmt.setString(9, member.getAddress());
            stmt.setString(10, member.getProfession());
            stmt.setString(11, member.getEmail());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            throw new SQLException("Creating member failed");
        }
    }

    public Optional<Member> findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM member WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapToMember(rs));
            }
        }
        return Optional.empty();
    }

    public List<Member> findAll() throws SQLException {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT * FROM member";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                members.add(mapToMember(rs));
            }
        }
        return members;
    }

    public boolean existsById(Integer id) throws SQLException {
        String sql = "SELECT 1 FROM member WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    public boolean isMemberSenior(Integer memberId) throws SQLException {
        String sql = "SELECT 1 FROM member WHERE id = ? AND join_date <= CURRENT_DATE - INTERVAL '6 months' AND admission_status = 'APPROVED'";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, memberId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    public boolean isMemberEligibleSponsor(Integer memberId) throws SQLException {
        String sql = "SELECT 1 FROM member WHERE id = ? AND join_date <= CURRENT_DATE - INTERVAL '90 days' AND admission_status = 'APPROVED'";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, memberId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    public List<Integer> getRefereeIdsByMemberId(Integer memberId) throws SQLException {
        List<Integer> refereeIds = new ArrayList<>();
        String sql = "SELECT sponsor_member_id FROM sponsorship WHERE member_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, memberId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                refereeIds.add(rs.getInt("sponsor_member_id"));
            }
        }
        return refereeIds;
    }

    public void saveSponsorship(int memberId, int sponsorId, String relationship) throws SQLException {
        String sql = "INSERT INTO sponsorship (member_id, sponsor_member_id, relationship, sponsor_collectivity_id) " +
                "VALUES (?, ?, ?, (SELECT collectivity_id FROM membership WHERE member_id = ? LIMIT 1))";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, memberId);
            stmt.setInt(2, sponsorId);
            stmt.setString(3, relationship != null ? relationship : "OTHER");
            stmt.setInt(4, sponsorId);
            stmt.executeUpdate();
        }
    }

    public void saveSponsorships(int memberId, List<Integer> sponsorIds, List<String> relationships) throws SQLException {
        String sql = "INSERT INTO sponsorship (member_id, sponsor_member_id, relationship, sponsor_collectivity_id) " +
                "VALUES (?, ?, ?, (SELECT collectivity_id FROM membership WHERE member_id = ? LIMIT 1))";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < sponsorIds.size(); i++) {
                stmt.setInt(1, memberId);
                stmt.setInt(2, sponsorIds.get(i));
                stmt.setString(3, relationships != null && i < relationships.size() ? relationships.get(i) : "OTHER");
                stmt.setInt(4, sponsorIds.get(i));
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    public void updateAdmissionStatus(int memberId, String status) throws SQLException {
        String sql = "UPDATE member SET admission_status = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, memberId);
            stmt.executeUpdate();
        }
    }

    public List<Member> getMembersByIds(List<String> memberIds) throws SQLException {
        List<Member> members = new ArrayList<>();
        if (memberIds == null || memberIds.isEmpty()) {
            return members;
        }

        String placeholders = String.join(",", memberIds.stream().map(id -> "?").toArray(String[]::new));
        String sql = "SELECT * FROM member WHERE id IN (" + placeholders + ")";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < memberIds.size(); i++) {
                stmt.setInt(i + 1, Integer.parseInt(memberIds.get(i)));
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                members.add(mapToMember(rs));
            }
        }
        return members;
    }

    public int getMemberCollectivityId(int memberId) throws SQLException {
        String sql = "SELECT collectivity_id FROM membership WHERE member_id = ? LIMIT 1";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, memberId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("collectivity_id");
            }
            return -1;
        }
    }

    private Member mapToMember(ResultSet rs) throws SQLException {
        Member member = new Member();
        member.setId(rs.getInt("id"));
        member.setFirstName(rs.getString("first_name"));
        member.setLastName(rs.getString("last_name"));

        Date birthDate = rs.getDate("birth_date");
        if (birthDate != null) {
            member.setBirthDate(birthDate.toLocalDate());
        }

        member.setGender(rs.getString("gender"));
        member.setPhoneNumber(rs.getString("phone_number"));
        member.setRegistrationFeePaid(rs.getBoolean("registration_fee_paid"));
        member.setMembershipDuesPaid(rs.getBoolean("membership_dues_paid"));
        member.setAdmissionStatus(rs.getString("admission_status"));
        member.setAddress(rs.getString("address"));
        member.setProfession(rs.getString("profession"));
        member.setEmail(rs.getString("email"));

        return member;
    }
}