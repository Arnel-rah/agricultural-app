package edu.hei.school.agricultural.repository;

import edu.hei.school.agricultural.controller.dto.CollectivityInformation;
import edu.hei.school.agricultural.controller.dto.CollectivityOverallStatistics;
import edu.hei.school.agricultural.entity.Collectivity;
import edu.hei.school.agricultural.entity.CollectivityStructure;
import edu.hei.school.agricultural.mapper.CollectivityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CollectivityRepository {
    private final Connection connection;
    private final CollectivityMapper collectivityMapper;

    public List<Collectivity> saveAll(List<Collectivity> collectivities) {
        List<Collectivity> memberList = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                """
                        insert into "collectivity" (id, name, number, location, president_id, vice_president_id, treasurer_id, secretary_id) 
                        values (?, ?, ?, ?, ?, ?, ?, ?) 
                        on conflict (id) do update set name = excluded.name,
                                                       number = excluded.number,
                                                       location = excluded.location,
                                                       president_id = excluded.president_id,
                                                       vice_president_id = excluded.vice_president_id,
                                                       treasurer_id = excluded.treasurer_id,
                                                       secretary_id = excluded.secretary_id
                        """)) {
            for (Collectivity collectivity : collectivities) {
                CollectivityStructure collectivityStructure = collectivity.getCollectivityStructure();
                preparedStatement.setString(1, collectivity.getId());
                preparedStatement.setString(2, collectivity.getName() != null ? collectivity.getName() : "");
                preparedStatement.setInt(3, collectivity.getNumber() != null ? collectivity.getNumber() : 0);
                preparedStatement.setString(4, collectivity.getLocation());
                preparedStatement.setString(5, collectivityStructure.getPresident().getId());
                preparedStatement.setString(6, collectivityStructure.getVicePresident().getId());
                preparedStatement.setString(7, collectivityStructure.getTreasurer().getId());
                preparedStatement.setString(8, collectivityStructure.getSecretary().getId());
                preparedStatement.addBatch();
            }
            var executedRow = preparedStatement.executeBatch();
            for (int i = 0; i < executedRow.length; i++) {
                memberList.add(findById(collectivities.get(i).getId()).orElseThrow());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return memberList;
    }

    public Optional<Collectivity> findById(String id) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("""
                select c.id, c.name, c.number, c.location, c.president_id, c.vice_president_id, c.treasurer_id, c.secretary_id
                from "collectivity" c
                where c.id = ?
                """)) {
            preparedStatement.setString(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(collectivityMapper.mapFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    public List<Collectivity> findAllByMemberId(String memberId) {
        List<Collectivity> collectivities = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement("""
                select c.id, c.name, c.number, c.location, c.president_id, c.vice_president_id, c.treasurer_id, c.secretary_id
                from "collectivity" c
                join "collectivity_member" cm on c.id = cm.collectivity_id
                where cm.member_id = ?
                """)) {
            preparedStatement.setString(1, memberId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                collectivities.add(collectivityMapper.mapFromResultSet(resultSet));
            }
            return collectivities;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Collectivity> findAll() {
        List<Collectivity> collectivities = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement("""
                select c.id, c.name, c.number, c.location, c.president_id, c.vice_president_id, c.treasurer_id, c.secretary_id
                from "collectivity" c
                """)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                collectivities.add(collectivityMapper.mapFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return collectivities;
    }

    public List<CollectivityOverallStatistics> findAllCollectivitiesStatistics(LocalDate from, LocalDate to) {
        List<CollectivityOverallStatistics> statisticsList = new ArrayList<>();

        String sql = """
        WITH 
        total_members AS (
            SELECT 
                cm.collectivity_id,
                COUNT(DISTINCT cm.member_id) as total_members
            FROM collectivity_member cm
            GROUP BY cm.collectivity_id
        ),
        new_members AS (
            SELECT 
                cm.collectivity_id,
                COUNT(DISTINCT cm.member_id) as new_members_count
            FROM collectivity_member cm
            JOIN "member" m ON cm.member_id = m.id
            WHERE m.created_at BETWEEN ? AND ?
            GROUP BY cm.collectivity_id
        ),
        members_up_to_date AS (
            SELECT 
                cm.collectivity_id,
                COUNT(DISTINCT cm.member_id) as up_to_date_count
            FROM collectivity_member cm
            WHERE NOT EXISTS (
                SELECT 1
                FROM collectivity_membership_fee cmf
                JOIN membership_fee mf ON cmf.membership_fee_id = mf.id
                WHERE cmf.collectivity_id = cm.collectivity_id
                    AND mf.status = 'ACTIVE'
                    AND mf.eligible_from <= ?
                    AND NOT EXISTS (
                        SELECT 1
                        FROM member_payment mp
                        WHERE mp.member_id = cm.member_id
                            AND mp.membership_fee_id = mf.id
                            AND mp.creation_date <= ?
                    )
            )
            GROUP BY cm.collectivity_id
        ),
        assiduity_stats AS (
            SELECT 
                ca.collectivity_id,
                COALESCE(
                    (COUNT(DISTINCT CASE WHEN ama.attendance_status = 'ATTENDED' THEN a.id END) * 100.0) 
                    / NULLIF(COUNT(DISTINCT a.id), 0),
                    100.0
                ) as assiduity_percentage
            FROM collectivity_activity ca
            JOIN activity a ON ca.activity_id = a.id
            LEFT JOIN activity_member_attendance ama ON a.id = ama.activity_id
            WHERE a.executive_date BETWEEN ? AND ?
            GROUP BY ca.collectivity_id
        )
        SELECT 
            c.id as collectivity_id,
            c.name,
            c.number,
            COALESCE(tm.total_members, 0) as total_members,
            COALESCE(nm.new_members_count, 0) as new_members,
            COALESCE(mud.up_to_date_count, 0) as members_up_to_date,
            ROUND(COALESCE(mud.up_to_date_count * 100.0 / NULLIF(tm.total_members, 0), 0), 2) as current_due_percentage,
            ROUND(COALESCE(ad.assiduity_percentage, 100.0), 2) as assiduity_percentage
        FROM "collectivity" c
        LEFT JOIN total_members tm ON c.id = tm.collectivity_id
        LEFT JOIN new_members nm ON c.id = nm.collectivity_id
        LEFT JOIN members_up_to_date mud ON c.id = mud.collectivity_id
        LEFT JOIN assiduity_stats ad ON c.id = ad.collectivity_id
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(from));
            ps.setDate(2, java.sql.Date.valueOf(to));
            ps.setDate(3, java.sql.Date.valueOf(to));
            ps.setDate(4, java.sql.Date.valueOf(to));
            ps.setDate(5, java.sql.Date.valueOf(from));
            ps.setDate(6, java.sql.Date.valueOf(to));

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                CollectivityInformation collectivityInfo = CollectivityInformation.builder()
                        .name(rs.getString("name"))
                        .number(rs.getInt("number"))
                        .build();

                CollectivityOverallStatistics statistics = CollectivityOverallStatistics.builder()
                        .collectivityInformation(collectivityInfo)
                        .newMembersNumber(rs.getInt("new_members"))
                        .overallMemberCurrentDuePercentage(rs.getDouble("current_due_percentage"))
                        .overallMemberAssiduityPercentage(rs.getDouble("assiduity_percentage"))
                        .build();

                statisticsList.add(statistics);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return statisticsList;
    }
}