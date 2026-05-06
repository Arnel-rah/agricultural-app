package hei.school.agriculturalapp.repository;

import hei.school.agriculturalapp.config.DatabaseConfig;
import hei.school.agriculturalapp.model.Collectivity;
import hei.school.agriculturalapp.model.CollectivityStructure;
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
public class CollectivityRepository {

    private final DatabaseConfig dbconfig;

    public boolean existsById(String id) throws SQLException {
        String sql = "SELECT 1 FROM collectivity WHERE id = ?";
        try (PreparedStatement stmt = dbconfig.connection().prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public Optional<Collectivity> findById(String id) throws SQLException {
        String sql = "SELECT id, name, location, agricultural_specialty, registration_number, creation_date, federation_approval, federation_id FROM collectivity WHERE id = ?";
        try (PreparedStatement stmt = dbconfig.connection().prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapToCollectivity(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Collectivity> findAll() throws SQLException {
        List<Collectivity> collectivities = new ArrayList<>();
        String sql = "SELECT id, name, location, agricultural_specialty, registration_number, creation_date, federation_approval, federation_id FROM collectivity ORDER BY id";
        try (Statement stmt = dbconfig.connection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                collectivities.add(mapToCollectivity(rs));
            }
        }
        return collectivities;
    }

    public List<Member> getMembersByCollectivityId(String collectivityId) throws SQLException {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT m.id, m.first_name, m.last_name, m.birth_date, m.gender, m.address, m.profession, m.phone_number, m.email, m.occupation FROM member m JOIN membership ms ON ms.member_id = m.id WHERE ms.collectivity_id = ?";
        try (PreparedStatement stmt = dbconfig.connection().prepareStatement(sql)) {
            stmt.setString(1, collectivityId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    members.add(mapToMember(rs));
                }
            }
        }
        return members;
    }

    public CollectivityStructure getCollectivityStructure(String collectivityId, int mandateId) throws SQLException {
        String sql = "SELECT m.id, m.first_name, m.last_name, m.birth_date, m.gender, m.address, m.profession, m.phone_number, m.email, m.occupation, r.name as role_name FROM assignment a JOIN member m ON m.id = a.member_id JOIN role r ON r.id = a.role_id WHERE a.collectivity_id = ? AND a.mandate_id = ?";
        Member president = null, vicePresident = null, treasurer = null, secretary = null;
        try (PreparedStatement stmt = dbconfig.connection().prepareStatement(sql)) {
            stmt.setString(1, collectivityId);
            stmt.setInt(2, mandateId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String roleName = rs.getString("role_name");
                    Member member = mapToMember(rs);
                    member.setReferees(new ArrayList<>());
                    switch (roleName) {
                        case "PRESIDENT": president = member; break;
                        case "VICE_PRESIDENT": vicePresident = member; break;
                        case "TREASURER": treasurer = member; break;
                        case "SECRETARY": secretary = member; break;
                    }
                }
            }
        }
        return new CollectivityStructure(president, vicePresident, treasurer, secretary);
    }

    public int getCurrentMandateId() throws SQLException {
        String sql = "SELECT id FROM mandate WHERE start_date <= CURRENT_DATE AND end_date >= CURRENT_DATE LIMIT 1";
        try (Statement stmt = dbconfig.connection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt("id");
            String insertSql = "INSERT INTO mandate (id, start_date, end_date) VALUES ('man-1', ?, ?) RETURNING id";
            try (PreparedStatement pstmt = dbconfig.connection().prepareStatement(insertSql)) {
                pstmt.setDate(1, Date.valueOf(LocalDate.now().withDayOfYear(1)));
                pstmt.setDate(2, Date.valueOf(LocalDate.now().withDayOfYear(1).plusYears(1).minusDays(1)));
                try (ResultSet rs2 = pstmt.executeQuery()) {
                    if (rs2.next()) return rs2.getInt("id");
                }
            }
        }
        throw new SQLException("Unable to get or create current mandate");
    }

    public void addMembersToCollectivity(String collectivityId, List<String> memberIds) throws SQLException {
        String sql = "INSERT INTO membership (id, member_id, collectivity_id, joined_at) VALUES (?, ?, ?, CURRENT_DATE) ON CONFLICT DO NOTHING";
        try (PreparedStatement stmt = dbconfig.connection().prepareStatement(sql)) {
            for (String memberId : memberIds) {
                stmt.setString(1, "ms-" + collectivityId + "-" + memberId);
                stmt.setString(2, memberId);
                stmt.setString(3, collectivityId);
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    public void assignRolesToCollectivity(String collectivityId, int mandateId, String presidentId, String vicePresidentId, String treasurerId, String secretaryId) throws SQLException {
        String sql = "INSERT INTO assignment (id, member_id, role_id, mandate_id, collectivity_id) VALUES (?, ?, (SELECT id FROM role WHERE name = ?), ?, ?)";
        try (PreparedStatement stmt = dbconfig.connection().prepareStatement(sql)) {
            if (presidentId != null) addRoleBatch(stmt, presidentId, "PRESIDENT", mandateId, collectivityId);
            if (vicePresidentId != null) addRoleBatch(stmt, vicePresidentId, "VICE_PRESIDENT", mandateId, collectivityId);
            if (treasurerId != null) addRoleBatch(stmt, treasurerId, "TREASURER", mandateId, collectivityId);
            if (secretaryId != null) addRoleBatch(stmt, secretaryId, "SECRETARY", mandateId, collectivityId);
            stmt.executeBatch();
        }
    }

    private void addRoleBatch(PreparedStatement stmt, String memberId, String role, int mandateId, String colId) throws SQLException {
        stmt.setString(1, "as-" + colId + "-" + role.toLowerCase());
        stmt.setString(2, memberId);
        stmt.setString(3, role);
        stmt.setInt(4, mandateId);
        stmt.setString(5, colId);
        stmt.addBatch();
    }

    private Collectivity mapToCollectivity(ResultSet rs) throws SQLException {
        Collectivity c = new Collectivity();
        c.setId(rs.getString("id"));
        c.setName(rs.getString("name"));
        c.setLocation(rs.getString("location"));
        c.setAgriculturalSpecialty(rs.getString("agricultural_specialty"));
        c.setRegistrationNumber(rs.getString("registration_number"));
        c.setCreationDate(rs.getDate("creation_date").toLocalDate());
        c.setFederationApproval(rs.getBoolean("federation_approval"));
        c.setFederationId(rs.getString("federation_id"));
        return c;
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