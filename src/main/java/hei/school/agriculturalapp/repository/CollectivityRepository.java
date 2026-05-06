package hei.school.agriculturalapp.repository;

import hei.school.agriculturalapp.config.DatabaseConfig;
import hei.school.agriculturalapp.model.*;
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

    public Optional<Collectivity> findDetailedById(String id) throws SQLException {
        Optional<Collectivity> collectivityOpt = findById(id);

        if (collectivityOpt.isPresent()) {
            Collectivity collectivity = collectivityOpt.get();
            collectivity.setMembers(getMembersByCollectivityId(id));

            try {
                int mandateId = getCurrentMandateId();
                collectivity.setStructure(getCollectivityStructure(id, mandateId));
            } catch (SQLException e) {
            }

            return Optional.of(collectivity);
        }
        return Optional.empty();
    }

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

    public boolean checkAllMembersExist(List<String> memberIds) throws SQLException {
        if (memberIds == null || memberIds.isEmpty()) return false;

        String sql = "SELECT COUNT(*) FROM member WHERE id = ANY(?)";

        try (PreparedStatement stmt = dbconfig.connection().prepareStatement(sql)) {
            Integer[] ids = memberIds.stream()
                    .map(Integer::parseInt)
                    .toArray(Integer[]::new);

            Array array = dbconfig.connection().createArrayOf("INTEGER", ids);
            stmt.setArray(1, array);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == memberIds.size();
                }
            }
        }
        return false;
    }

    public Collectivity updateIdentification(String id, String uniqueName, String officialNumber) throws SQLException {
        String sql = "UPDATE collectivity SET unique_name = ?, official_number = ? WHERE id = ?";

        try (PreparedStatement stmt = dbconfig.connection().prepareStatement(sql)) {
            stmt.setString(1, uniqueName);
            stmt.setString(2, officialNumber);
            stmt.setInt(3, Integer.parseInt(id));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Update failed, no collectivity found with id: " + id);
            }
        }
        return findDetailedById(id).orElseThrow(() -> new SQLException("Error retrieving updated collectivity"));
    }

    public boolean existsByUniqueName(String uniqueName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM collectivity WHERE unique_name = ?";
        try (PreparedStatement stmt = dbconfig.connection().prepareStatement(sql)) {
            stmt.setString(1, uniqueName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public boolean existsById(String id) throws SQLException {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }

        String sql = "SELECT 1 FROM collectivity WHERE id = ?";
        try (PreparedStatement stmt = dbconfig.connection().prepareStatement(sql)) {
            stmt.setInt(1, Integer.parseInt(id));
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (NumberFormatException e) {
            return false;
        }
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
            if (presidentId != null) addRoleBatch(stmt, presidentId, "PRESIDENT", mandateId, collectivityId);
            if (vicePresidentId != null) addRoleBatch(stmt, vicePresidentId, "VICE_PRESIDENT", mandateId, collectivityId);
            if (treasurerId != null) addRoleBatch(stmt, treasurerId, "TREASURER", mandateId, collectivityId);
            if (secretaryId != null) addRoleBatch(stmt, secretaryId, "SECRETARY", mandateId, collectivityId);
            stmt.executeBatch();
        }
    }

    private void addRoleBatch(PreparedStatement stmt, String memberId, String role, int mandateId, String colId) throws SQLException {
        stmt.setInt(1, Integer.parseInt(memberId));
        stmt.setString(2, role);
        stmt.setInt(3, mandateId);
        stmt.setInt(4, Integer.parseInt(colId));
        stmt.addBatch();
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

        Member president = null, vicePresident = null, treasurer = null, secretary = null;

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
            if (rs.next()) return rs.getInt("id");

            String insertSql = "INSERT INTO mandate (start_date, end_date) VALUES (?, ?) RETURNING id";
            try (PreparedStatement pstmt = dbconfig.connection().prepareStatement(insertSql)) {
                pstmt.setDate(1, Date.valueOf(java.time.LocalDate.now().withDayOfYear(1)));
                pstmt.setDate(2, Date.valueOf(java.time.LocalDate.now().withDayOfYear(1).plusYears(1).minusDays(1)));
                try (ResultSet rs2 = pstmt.executeQuery()) {
                    if (rs2.next()) return rs2.getInt("id");
                }
            }
        }
        throw new SQLException("Unable to get or create current mandate");
    }

    private Collectivity mapToCollectivity(ResultSet rs) throws SQLException {
        Collectivity collectivity = new Collectivity();
        collectivity.setId(String.valueOf(rs.getInt("id")));
        collectivity.setLocation(rs.getString("location"));
        collectivity.setUniqueName(rs.getString("unique_name"));
        collectivity.setOfficialNumber(rs.getString("official_number"));
        return collectivity;
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
        member.setPhoneNumber(String.valueOf(rs.getLong("phone_number")));
        member.setEmail(rs.getString("email"));
        return member;
    }

    public List<CollectivityTransaction> getTransactions(String id, LocalDate from, LocalDate to) throws SQLException {
        List<CollectivityTransaction> transactions = new ArrayList<>();
        String sql = """
        SELECT t.id AS tx_id, t.creation_date AS tx_date, t.amount AS tx_amount, t.payment_mode AS tx_mode,
               fa.id AS acc_id, fa.balance AS acc_balance,
               m.id AS mem_id, m.first_name, m.last_name, m.birth_date, m.gender, 
               m.address, m.profession, m.phone_number, m.email, m.occupation
        FROM collectivity_transaction t
        JOIN member m ON t.member_id = m.id
        JOIN financial_account fa ON t.account_id = fa.id
        WHERE t.collectivity_id = ? 
          AND t.creation_date >= ? 
          AND t.creation_date <= ?
        ORDER BY t.creation_date DESC
        """;

        try (PreparedStatement stmt = dbconfig.connection().prepareStatement(sql)) {
            stmt.setInt(1, Integer.parseInt(id));
            stmt.setDate(2, Date.valueOf(from));
            stmt.setDate(3, Date.valueOf(to));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapToTransaction(rs));
                }
            }
        }
        return transactions;
    }

    private CollectivityTransaction mapToTransaction(ResultSet rs) throws SQLException {
        CollectivityTransaction tx = new CollectivityTransaction();
        tx.setId(rs.getString("tx_id"));
        tx.setCreationDate(rs.getTimestamp("tx_date").toLocalDateTime().toLocalDate());
        tx.setAmount(rs.getDouble("tx_amount"));
        tx.setPaymentMode(rs.getString("tx_mode"));

        FinancialAccount acc = new FinancialAccount();
        acc.setId(String.valueOf(rs.getInt("acc_id")));
        acc.setAmount(Double.valueOf(String.valueOf(rs.getDouble("acc_balance"))));
        tx.setAccountCredited(acc);
        Member member = new Member();
        member.setId(rs.getInt("mem_id"));
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

        tx.setMemberDebited(member);

        return tx;
    }
}