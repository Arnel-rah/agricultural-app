package hei.school.agriculturalapp.repository;

import hei.school.agriculturalapp.model.Member;
import org.springframework.stereotype.Repository;

import java.sql.*;

@Repository
public class MemberRepository {

    public int save(Member member, Connection conn) throws SQLException {
        String sql = "INSERT INTO member (first_name, last_name, birth_date, gender, phone_number, " +
                "registration_fee_paid, membership_dues_paid, admission_status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, member.getFirstName());
            stmt.setString(2, member.getLastName());
            stmt.setDate(3, Date.valueOf(member.getBirthDate()));
            stmt.setString(4, member.getGender());
            stmt.setString(5, member.getPhoneNumber());
            stmt.setBoolean(6, member.isRegistrationFeePaid());
            stmt.setBoolean(7, member.isMembershipDuesPaid());
            stmt.setString(8, "APPROVED");

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            throw new SQLException("creating member failed");
        }
    }

    public void saveSponsorship(int memberId, int sponsorId, Connection conn) throws SQLException {
        String sql = "INSERT INTO sponsorship (member_id, sponsor_member_id, relationship) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, memberId);
            stmt.setInt(2, sponsorId);
            stmt.setString(3, "OTHER");
            stmt.executeUpdate();
        }
    }
}
