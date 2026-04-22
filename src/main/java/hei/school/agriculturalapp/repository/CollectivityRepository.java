package hei.school.agriculturalapp.repository;

import hei.school.agriculturalapp.config.DatabaseConfig;
import hei.school.agriculturalapp.model.Collectivity;
import hei.school.agriculturalapp.model.CollectivityStructure;
import hei.school.agriculturalapp.model.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CollectivityRepository {
    private final DatabaseConfig dbconfig;

    public Collectivity save(Collectivity entity) throws SQLException {
        String sql = "INSERT INTO collectivity (name, location, agricultural_specialty, registration_number, creation_date, federation_approval, federation_id) VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id";

        try (PreparedStatement stmt = dbconfig.connection().prepareStatement(sql)) {
            stmt.setString(1, entity.getLocation() + "_collectivity");
            stmt.setString(2, entity.getLocation());
            stmt.setString(3, "GENERAL");
            stmt.setInt(4, (int) (System.currentTimeMillis() % 1000000));
            stmt.setDate(5, Date.valueOf(java.time.LocalDate.now()));
            stmt.setBoolean(6, true);
            stmt.setNull(7, Types.INTEGER);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    entity.setId(String.valueOf(rs.getInt("id")));
                }
            }
        }
        return entity;
    }

    public Optional<Collectivity> findById(String id) throws SQLException {
        String sql = "SELECT * FROM collectivity WHERE id = ?";

        try (PreparedStatement stmt = dbconfig.connection().prepareStatement(sql)) {
            stmt.setInt(1, Integer.parseInt(id));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapToCollectivity(rs));
                }
            }
        }
        return Optional.empty();
    }

    public void addMembersToCollectivity(String collectivityId, List<String> memberIds) throws SQLException {
        String sql = "INSERT INTO membership (member_id, collectivity_id, joined_at) VALUES (?, ?, CURRENT_DATE) ON CONFLICT DO NOTHING";

        try (PreparedStatement stmt = dbconfig.connection().prepareStatement(sql)) {
            for (String memberId : memberIds) {
                stmt.setInt(1, Integer.parseInt(memberId));
                stmt.setInt(2, Integer.parseInt(collectivityId));
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    public void assignRolesToCollectivity(String collectivityId, int mandateId, String presidentId, String vicePresidentId, String treasurerId, String secretaryId) throws SQLException {
        String sql = "INSERT INTO assignment (member_id, role_id, mandate_id, collectivity_id) VALUES (?, (SELECT id FROM role WHERE name = ?), ?, ?)";

        try (PreparedStatement stmt = dbconfig.connection().prepareStatement(sql)) {
            if (presidentId != null) {
                stmt.setInt(1, Integer.parseInt(presidentId));
                stmt.setString(2, "PRESIDENT");
                stmt.setInt(3, mandateId);
                stmt.setInt(4, Integer.parseInt(collectivityId));
                stmt.addBatch();
            }
            if (vicePresidentId != null) {
                stmt.setInt(1, Integer.parseInt(vicePresidentId));
                stmt.setString(2, "VICE_PRESIDENT");
                stmt.setInt(3, mandateId);
                stmt.setInt(4, Integer.parseInt(collectivityId));
                stmt.addBatch();
            }
            if (treasurerId != null) {
                stmt.setInt(1, Integer.parseInt(treasurerId));
                stmt.setString(2, "TREASURER");
                stmt.setInt(3, mandateId);
                stmt.setInt(4, Integer.parseInt(collectivityId));
                stmt.addBatch();
            }
            if (secretaryId != null) {
                stmt.setInt(1, Integer.parseInt(secretaryId));
                stmt.setString(2, "SECRETARY");
                stmt.setInt(3, mandateId);
                stmt.setInt(4, Integer.parseInt(collectivityId));
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    public List<Member> getMembersByCollectivityId(String collectivityId) throws SQLException {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT m.id, m.first_name, m.last_name, m.birth_date, m.gender, m.address, m.profession, m.phone_number, m.email FROM member m JOIN membership ms ON ms.member_id = m.id WHERE ms.collectivity_id = ?";

        try (PreparedStatement stmt = dbconfig.connection().prepareStatement(sql)) {
            stmt.setInt(1, Integer.parseInt(collectivityId));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    members.add(mapToMember(rs));
                }
            }
        }
        return members;
    }

    public CollectivityStructure getCollectivityStructure(String collectivityId, int mandateId) throws SQLException {
        String sql = "SELECT m.id, m.first_name, m.last_name, m.birth_date, m.gender, m.address, m.profession, m.phone_number, m.email, r.name as role_name FROM assignment a JOIN member m ON m.id = a.member_id JOIN role r ON r.id = a.role_id WHERE a.collectivity_id = ? AND a.mandate_id = ?";

        Member president = null;
        Member vicePresident = null;
        Member treasurer = null;
        Member secretary = null;

        try (PreparedStatement stmt = dbconfig.connection().prepareStatement(sql)) {
            stmt.setInt(1, Integer.parseInt(collectivityId));
            stmt.setInt(2, mandateId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String roleName = rs.getString("role_name");
                    Member member = mapToMember(rs);
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
            if (rs.next()) {
                return rs.getInt("id");
            } else {
                String insertSql = "INSERT INTO mandate (start_date, end_date) VALUES (?, ?) RETURNING id";
                try (PreparedStatement pstmt = dbconfig.connection().prepareStatement(insertSql)) {
                    pstmt.setDate(1, Date.valueOf(java.time.LocalDate.now().withDayOfYear(1)));
                    pstmt.setDate(2, Date.valueOf(java.time.LocalDate.now().withDayOfYear(1).plusYears(1).minusDays(1)));
                    try (ResultSet rs2 = pstmt.executeQuery()) {
                        if (rs2.next()) {
                            return rs2.getInt("id");
                        }
                    }
                }
            }
        }
        throw new SQLException("Unable to get or create current mandate");
    }

    public boolean checkAllMembersExist(List<String> memberIds) throws SQLException {
        if (memberIds == null || memberIds.isEmpty()) return false;
        String sql = "SELECT COUNT(*) FROM member WHERE id = ?";
        int existingCount = 0;
        for (String memberId : memberIds) {
            try (PreparedStatement stmt = dbconfig.connection().prepareStatement(sql)) {
                stmt.setInt(1, Integer.parseInt(memberId));
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) existingCount++;
                }
            }
        }
        return existingCount == memberIds.size();
    }

    public int getMemberCountByCollectivityId(String collectivityId) throws SQLException {
        String sql = "SELECT COUNT(DISTINCT member_id) FROM membership WHERE collectivity_id = ?";
        try (PreparedStatement stmt = dbconfig.connection().prepareStatement(sql)) {
            stmt.setInt(1, Integer.parseInt(collectivityId));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public int getSeniorMemberCountByCollectivityId(String collectivityId) throws SQLException {
        String sql = "SELECT COUNT(DISTINCT ms.member_id) FROM membership ms JOIN member m ON m.id = ms.member_id WHERE ms.collectivity_id = ? AND m.join_date <= CURRENT_DATE - INTERVAL '6 months'";
        try (PreparedStatement stmt = dbconfig.connection().prepareStatement(sql)) {
            stmt.setInt(1, Integer.parseInt(collectivityId));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    private Collectivity mapToCollectivity(ResultSet rs) throws SQLException {
        Collectivity collectivity = new Collectivity();
        collectivity.setId(String.valueOf(rs.getInt("id")));
        collectivity.setLocation(rs.getString("location"));
        return collectivity;
    }

    private Member mapToMember(ResultSet rs) throws SQLException {
        Member member = new Member();
        member.setId(Integer.valueOf(String.valueOf(rs.getInt("id"))));
        member.setFirstName(rs.getString("first_name"));
        member.setLastName(rs.getString("last_name"));
        Date birthDate = rs.getDate("birth_date");
        if (birthDate != null) member.setBirthDate(birthDate.toLocalDate());
        member.setGender(rs.getString("gender"));
        member.setAddress(rs.getString("address"));
        member.setProfession(rs.getString("profession"));
        member.setPhoneNumber(String.valueOf(rs.getInt("phone_number")));
        member.setEmail(rs.getString("email"));
        return member;
    }
}