package hei.school.agriculturalapp.repository;

import hei.school.agriculturalapp.config.DatabaseConfig;
import hei.school.agriculturalapp.model.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MemberRepository {

    private final DatabaseConfig dbconfig;

    public Member save(Member member) throws SQLException {
        String sql = "INSERT INTO member (id, first_name, last_name, birth_date, gender, address, profession, phone_number, email, occupation, join_date, registration_fee_paid, membership_dues_paid, admission_status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
        String id = "M" + System.currentTimeMillis();
        try (PreparedStatement stmt = dbconfig.connection().prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.setString(2, member.getFirstName());
            stmt.setString(3, member.getLastName());
            stmt.setDate(4, Date.valueOf(member.getBirthDate()));
            stmt.setString(5, member.getGender());
            stmt.setString(6, member.getAddress());
            stmt.setString(7, member.getProfession());
            stmt.setString(8, member.getPhoneNumber());
            stmt.setString(9, member.getEmail());
            stmt.setString(10, member.getOccupation());
            stmt.setDate(11, Date.valueOf(member.getJoinDate() != null ? member.getJoinDate() : LocalDate.now()));
            stmt.setBoolean(12, member.getRegistrationFeePaid() != null && member.getRegistrationFeePaid());
            stmt.setBoolean(13, member.getMembershipDuesPaid() != null && member.getMembershipDuesPaid());
            stmt.setString(14, member.getAdmissionStatus() != null ? member.getAdmissionStatus() : "APPROVED");
            stmt.executeUpdate();
            member.setId(id);
        }
        return member;
    }

    public Optional<Member> findById(String id) throws SQLException {
        String sql = "SELECT id, first_name, last_name, birth_date, gender, address, profession, phone_number, email, occupation, join_date, registration_fee_paid, membership_dues_paid, admission_status FROM member WHERE id = ?";
        try (PreparedStatement stmt = dbconfig.connection().prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapToMember(rs));
                }
            }
        }
        return Optional.empty();
    }

    public boolean existsById(String id) throws SQLException {
        String sql = "SELECT 1 FROM member WHERE id = ?";
        try (PreparedStatement stmt = dbconfig.connection().prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean existsByEmail(String email) throws SQLException {
        String sql = "SELECT 1 FROM member WHERE email = ?";
        try (PreparedStatement stmt = dbconfig.connection().prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean isMemberSenior(String memberId) throws SQLException {
        String sql = "SELECT 1 FROM member WHERE id = ? AND join_date <= CURRENT_DATE - INTERVAL '6 months'";
        try (PreparedStatement stmt = dbconfig.connection().prepareStatement(sql)) {
            stmt.setString(1, memberId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public List<Member> getMembersByIds(List<String> memberIds) throws SQLException {
        List<Member> members = new ArrayList<>();
        if (memberIds == null || memberIds.isEmpty()) return members;
        String placeholders = String.join(",", memberIds.stream().map(id -> "?").toArray(String[]::new));
        String sql = "SELECT id, first_name, last_name, birth_date, gender, address, profession, phone_number, email, occupation FROM member WHERE id IN (" + placeholders + ")";
        try (PreparedStatement stmt = dbconfig.connection().prepareStatement(sql)) {
            for (int i = 0; i < memberIds.size(); i++) {
                stmt.setString(i + 1, memberIds.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Member m = mapToMember(rs);
                    m.setReferees(new ArrayList<>());
                    members.add(m);
                }
            }
        }
        return members;
    }

    public List<Member> getAllMembers() throws SQLException {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT id, first_name, last_name, birth_date, gender, address, profession, phone_number, email, occupation FROM member";
        try (Statement stmt = dbconfig.connection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                members.add(mapToMember(rs));
            }
        }
        return members;
    }

    private Member mapToMember(ResultSet rs) throws SQLException {
        Member m = new Member();
        m.setId(rs.getString("id"));
        m.setFirstName(rs.getString("first_name"));
        m.setLastName(rs.getString("last_name"));
        Date bd = rs.getDate("birth_date");
        if (bd != null) m.setBirthDate(bd.toLocalDate());
        m.setGender(rs.getString("gender"));
        m.setAddress(rs.getString("address"));
        m.setProfession(rs.getString("profession"));
        m.setPhoneNumber(rs.getString("phone_number"));
        m.setEmail(rs.getString("email"));
        m.setOccupation(rs.getString("occupation"));
        return m;
    }
}