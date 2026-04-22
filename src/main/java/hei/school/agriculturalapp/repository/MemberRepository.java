package hei.school.agriculturalapp.repository;

import hei.school.agriculturalapp.config.DatabaseConfig;
import hei.school.agriculturalapp.model.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MemberRepository {
    private final DatabaseConfig dbconfig;

    public Member save(Member member) throws SQLException {
        String sql = "INSERT INTO member (first_name, last_name, birth_date, gender, address, profession, phone_number, email, registration_fee_paid, membership_dues_paid, admission_status, join_date, occupation) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'APPROVED', CURRENT_DATE, ?) RETURNING id";

        try (PreparedStatement stmt = dbconfig.connection().prepareStatement(sql)) {
            stmt.setString(1, member.getFirstName());
            stmt.setString(2, member.getLastName());
            stmt.setDate(3, Date.valueOf(member.getBirthDate()));
            stmt.setString(4, member.getGender());
            stmt.setString(5, member.getAddress());
            stmt.setString(6, member.getProfession());
            stmt.setInt(7, Integer.parseInt(member.getPhoneNumber()));
            stmt.setString(8, member.getEmail());
            stmt.setBoolean(9, true);
            stmt.setBoolean(10, true);
            stmt.setString(11, member.getOccupation());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    member.setId(rs.getInt("id"));
                }
            }
        }
        return member;
    }

    public Optional<Member> findById(String id) throws SQLException {
        String sql = "SELECT id, first_name, last_name, birth_date, gender, address, profession, phone_number, email, occupation FROM member WHERE id = ?";
        try (PreparedStatement stmt = dbconfig.connection().prepareStatement(sql)) {
            stmt.setInt(1, Integer.parseInt(id));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapToMember(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Member> findAll() throws SQLException {
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

    public boolean existsById(String id) throws SQLException {
        String sql = "SELECT 1 FROM member WHERE id = ?";
        try (PreparedStatement stmt = dbconfig.connection().prepareStatement(sql)) {
            stmt.setInt(1, Integer.parseInt(id));
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
            stmt.setInt(1, Integer.parseInt(memberId));
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
                stmt.setInt(i + 1, Integer.parseInt(memberIds.get(i)));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    members.add(mapToMember(rs));
                }
            }
        }
        return members;
    }

    private Member mapToMember(ResultSet rs) throws SQLException {
        Member member = new Member();
        member.setId(rs.getInt("id"));
        member.setFirstName(rs.getString("first_name"));
        member.setLastName(rs.getString("last_name"));
        Date birthDate = rs.getDate("birth_date");
        if (birthDate != null) member.setBirthDate(birthDate.toLocalDate());
        member.setGender(rs.getString("gender"));
        member.setAddress(rs.getString("address"));
        member.setProfession(rs.getString("profession"));
        member.setPhoneNumber(rs.getString("phone_number"));
        member.setEmail(rs.getString("email"));
        member.setOccupation(rs.getString("occupation"));
        member.setReferees(member.getReferees() == null ? new ArrayList<>() : member.getReferees());
        return member;
    }
}