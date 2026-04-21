package hei.school.agriculturalapp.repository;

import hei.school.agriculturalapp.model.Collectivity;
import hei.school.agriculturalapp.model.CollectivityStructure;
import hei.school.agriculturalapp.model.Member;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Repository
public class CollectivityRepository implements Repository<Collectivity, String> {

    private final Connection connection;

    public CollectivityRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Collectivity save(Collectivity entity) throws SQLException {
        String sql = "INSERT INTO collectivity (name, location, agricultural_specialty, registration_number, creation_date, federation_approval, federation_id) VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, entity.getLocation() + "_collectivity"); // name basé sur location
            stmt.setString(2, entity.getLocation());
            stmt.setString(3, "GENERAL"); // agricultural_specialty par défaut
            stmt.setInt(4, (int) (System.currentTimeMillis() % 1000000)); // registration_number temporaire
            stmt.setDate(5, Date.valueOf(java.time.LocalDate.now()));
            stmt.setBoolean(6, true); // federation_approval
            stmt.setNull(7, Types.INTEGER); // federation_id

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    entity.setId(String.valueOf(rs.getInt("id")));
                }
            }
        }
        return entity;
    }

    @Override
    public Optional<Collectivity> findById(String id) throws SQLException {
        String sql = "SELECT * FROM collectivity WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, Integer.parseInt(id));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapToCollectivity(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Collectivity> findAll() throws SQLException {
        List<Collectivity> collectivities = new ArrayList<>();
        String sql = "SELECT * FROM collectivity";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                collectivities.add(mapToCollectivity(rs));
            }
        }
        return collectivities;
    }

    @Override
    public void deleteById(String id) throws SQLException {
        String sql = "DELETE FROM collectivity WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, Integer.parseInt(id));
            stmt.executeUpdate();
        }
    }

    @Override
    public boolean existsById(String id) throws SQLException {
        String sql = "SELECT 1 FROM collectivity WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, Integer.parseInt(id));
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Méthodes spécifiques pour la création de collectivité (Feature A)

    public void addMembersToCollectivity(String collectivityId, List<String> memberIds) throws SQLException {
        String sql = "INSERT INTO membership (member_id, collectivity_id, joined_at) VALUES (?, ?, CURRENT_DATE) ON CONFLICT DO NOTHING";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (String memberId : memberIds) {
                stmt.setInt(1, Integer.parseInt(memberId));
                stmt.setInt(2, Integer.parseInt(collectivityId));
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    public void assignRolesToCollectivity(String collectivityId, Integer mandateId, String presidentId, String vicePresidentId, String treasurerId, String secretaryId) throws SQLException {
        String sql = "INSERT INTO assignment (member_id, role_id, mandate_id, collectivity_id) VALUES (?, (SELECT id FROM role WHERE name = ?), ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
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
        String sql = "SELECT m.* FROM member m " +
                "JOIN membership ms ON ms.member_id = m.id " +
                "WHERE ms.collectivity_id = ? AND m.admission_status = 'APPROVED'";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, Integer.parseInt(collectivityId));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    members.add(mapToMember(rs));
                }
            }
        }
        return members;
    }

    public CollectivityStructure getCollectivityStructure(String collectivityId, Integer mandateId) throws SQLException {
        String sql = "SELECT m.*, r.name as role_name FROM assignment a " +
                "JOIN member m ON m.id = a.member_id " +
                "JOIN role r ON r.id = a.role_id " +
                "WHERE a.collectivity_id = ? AND a.mandate_id = ?";

        Member president = null;
        Member vicePresident = null;
        Member treasurer = null;
        Member secretary = null;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, Integer.parseInt(collectivityId));
            stmt.setInt(2, mandateId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String roleName = rs.getString("role_name");
                    Member member = mapToMember(rs);

                    switch (roleName) {
                        case "PRESIDENT":
                            president = member;
                            break;
                        case "VICE_PRESIDENT":
                            vicePresident = member;
                            break;
                        case "TREASURER":
                            treasurer = member;
                            break;
                        case "SECRETARY":
                            secretary = member;
                            break;
                    }
                }
            }
        }

        return new CollectivityStructure(president, vicePresident, treasurer, secretary);
    }

    public int getCurrentMandateId() throws SQLException {
        String sql = "SELECT id FROM mandate WHERE start_date <= CURRENT_DATE AND end_date >= CURRENT_DATE LIMIT 1";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("id");
            } else {
                String insertSql = "INSERT INTO mandate (start_date, end_date) VALUES (?, ?) RETURNING id";
                try (PreparedStatement pstmt = connection.prepareStatement(insertSql)) {
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

    public int getMemberCountByCollectivityId(String collectivityId) throws SQLException {
        String sql = "SELECT COUNT(DISTINCT member_id) FROM membership WHERE collectivity_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, Integer.parseInt(collectivityId));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public int getSeniorMemberCountByCollectivityId(String collectivityId) throws SQLException {
        String sql = "SELECT COUNT(DISTINCT ms.member_id) FROM membership ms " +
                "JOIN member m ON m.id = ms.member_id " +
                "WHERE ms.collectivity_id = ? AND m.join_date <= CURRENT_DATE - INTERVAL '6 months'";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, Integer.parseInt(collectivityId));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public boolean checkAllMembersExist(List<String> memberIds) throws SQLException {
        if (memberIds == null || memberIds.isEmpty()) {
            return false;
        }

        String sql = "SELECT COUNT(*) FROM member WHERE id = ? AND admission_status = 'APPROVED'";
        int existingCount = 0;

        for (String memberId : memberIds) {
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, Integer.parseInt(memberId));
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        existingCount++;
                    }
                }
            }
        }

        return existingCount == memberIds.size();
    }

    private Collectivity mapToCollectivity(ResultSet rs) throws SQLException {
        Collectivity collectivity = new Collectivity();
        collectivity.setId(String.valueOf(rs.getInt("id")));
        collectivity.setLocation(rs.getString("location"));
        return collectivity;
    }

    private Member mapToMember(ResultSet rs) throws SQLException {
        Member member = new Member();
        member.setId(rs.getInt("id"));
        member.setFirstName(rs.getString("first_name"));
        member.setLastName(rs.getString("last_name"));
        member.setBirthDate(rs.getDate("birth_date") != null ? rs.getDate("birth_date").toLocalDate() : null);
        member.setGender(rs.getString("gender"));
        member.setPhoneNumber(rs.getString("phone_number"));
        member.setRegistrationFeePaid(rs.getBoolean("registration_fee_paid"));
        member.setMembershipDuesPaid(rs.getBoolean("membership_dues_paid"));
        member.setAdmissionStatus(rs.getString("admission_status"));
        member.setCollectivityId(rs.getObject("collectivity_id") != null ? rs.getInt("collectivity_id") : null);

        // Champs supplémentaires
        try {
            member.setAddress(rs.getString("address"));
        } catch (SQLException ignored) {}
        try {
            member.setProfession(rs.getString("profession"));
        } catch (SQLException ignored) {}
        try {
            member.setEmail(rs.getString("email"));
        } catch (SQLException ignored) {}

        return member;
    }
}