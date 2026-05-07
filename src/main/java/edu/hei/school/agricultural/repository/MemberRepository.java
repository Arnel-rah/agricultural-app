package edu.hei.school.agricultural.repository;

import edu.hei.school.agricultural.entity.Collectivity;
import edu.hei.school.agricultural.entity.Member;
import edu.hei.school.agricultural.mapper.MemberMapper;
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
public class MemberRepository {
    private final Connection connection;
    private final MemberMapper memberMapper;
    private final CollectivityMemberRepository collectivityMemberRepository;
    private final MemberRefereeRepository memberRefereeRepository;

    public List<Member> saveAll(List<Member> members) {
        List<Member> memberList = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                """
                        insert into "member" (id, 
                                              first_name,
                                              last_name,
                                              birth_date,
                                              gender,
                                              address,
                                              profession,
                                              phone_number,
                                              email,
                                              occupation,
                                              registration_fee_paid,
                                              membership_dues_paid) 
                        values (?, ?, ?, ?, ?::gender, ?, ?, ?, ?, ?::member_occupation, ?, ?) 
                        on conflict (id) do update set first_name = excluded.first_name,
                                                       last_name = excluded.last_name,
                                                       birth_date = excluded.birth_date,
                                                       gender = excluded.gender,
                                                       phone_number = excluded.phone_number,
                                                       email = excluded.email,
                                                       address = excluded.address,
                                                       profession = excluded.profession,
                                                       occupation = excluded.occupation
                        returning id;
                        """)) {
            for (Member member : members) {
                preparedStatement.setString(1, member.getId());
                preparedStatement.setString(2, member.getFirstName());
                preparedStatement.setString(3, member.getLastName());
                preparedStatement.setDate(4, java.sql.Date.valueOf(member.getBirthDate()));
                preparedStatement.setObject(5, member.getGender().name());
                preparedStatement.setString(6, member.getAddress());
                preparedStatement.setString(7, member.getProfession());
                preparedStatement.setString(8, member.getPhoneNumber());
                preparedStatement.setString(9, member.getEmail());
                preparedStatement.setObject(10, member.getOccupation().name());
                preparedStatement.setBoolean(11, member.getRegistrationFeePaid());
                preparedStatement.setBoolean(12, member.getMembershipDuesPaid());
                preparedStatement.addBatch();
            }
            var executedRow = preparedStatement.executeBatch();
            for (int i = 0; i < executedRow.length; i++) {
                Member member = members.get(i);
                attachCollectivityMember(member);
                attachRefereeMember(member);
                memberList.add(findById(member.getId()).orElseThrow());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return memberList;
    }

    private void attachRefereeMember(Member member) {
        List<Member> referees = member.getReferees();
        if (referees != null) {
            for (Member referee : referees) {
                memberRefereeRepository.attachMemberReferee(referee, member);
            }
        }
    }

    private void attachCollectivityMember(Member member) {
        List<Collectivity> collectivities = member.getCollectivities();
        if (collectivities != null) {
            for (Collectivity collectivity : collectivities) {
                collectivityMemberRepository.attachMemberToCollectivity(collectivity, member);
            }
        }
    }

    public Optional<Member> findById(String id) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("""
                select member.id, first_name, last_name, birth_date, gender, phone_number, email, address, profession, occupation, registration_fee_paid, membership_dues_paid
                from "member"
                where id = ?
                """)) {
            preparedStatement.setString(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                var member = memberMapper.mapFromResultSet(resultSet);
                member.setReferees(findRefereesByIdMember(member.getId()));
                return Optional.of(member);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    public List<Member> findAllByCollectivity(Collectivity collectivity) {
        List<Member> memberList = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement("""
                select member.id, first_name, last_name, birth_date, gender, phone_number, email, address, profession, occupation, registration_fee_paid, membership_dues_paid
                from "member"
                    join collectivity_member on member.id = collectivity_member.member_id
                    join collectivity on collectivity.id = collectivity_member.collectivity_id
                where collectivity_member.collectivity_id = ?
                """)) {
            preparedStatement.setString(1, collectivity.getId());
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                var memberMapped = memberMapper.mapFromResultSet(resultSet);
                memberMapped.setReferees(findRefereesByIdMember(memberMapped.getId()));
                memberMapped.addCollectivity(collectivity);
                memberList.add(memberMapped);
            }
            return memberList;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Member> findRefereesByIdMember(String idMember) {
        List<Member> memberList = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement("""
                select member.id, first_name, last_name, birth_date, gender, phone_number, email, address, profession, occupation, registration_fee_paid, membership_dues_paid
                from "member"
                    join member_referee on member.id = member_referee.member_referee_id
                where member_referee.member_refereed_id = ?
                """)) {
            preparedStatement.setString(1, idMember);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                memberList.add(memberMapper.mapFromResultSet(resultSet));
            }
            return memberList;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Double getTotalPaymentsByMemberBetweenDates(String memberId, LocalDate from, LocalDate to) {
        String sql = """
            SELECT COALESCE(SUM(amount), 0)
            FROM member_payment
            WHERE member_id = ? 
            AND creation_date BETWEEN ? AND ?
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, memberId);
            ps.setDate(2, java.sql.Date.valueOf(from));
            ps.setDate(3, java.sql.Date.valueOf(to));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0.0;
    }

    public Double getUnpaidAmountByMemberBetweenDates(String memberId, LocalDate from, LocalDate to) {
        String sql = """
            SELECT COALESCE((
                SELECT SUM(mfi.amount)
                FROM membership_fee_installment mfi
                JOIN membership_fee mf ON mfi.membership_fee_id = mf.id
                WHERE mfi.member_id = ?
                    AND mf.status = 'ACTIVE'
                    AND mfi.due_date BETWEEN ? AND ?
            ), 0) - COALESCE((
                SELECT SUM(mp.amount)
                FROM member_payment mp
                WHERE mp.member_id = ?
                    AND mp.creation_date BETWEEN ? AND ?
            ), 0)
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, memberId);
            ps.setDate(2, java.sql.Date.valueOf(from));
            ps.setDate(3, java.sql.Date.valueOf(to));
            ps.setString(4, memberId);
            ps.setDate(5, java.sql.Date.valueOf(from));
            ps.setDate(6, java.sql.Date.valueOf(to));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Math.max(rs.getDouble(1), 0);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0.0;
    }

    public Integer countNewMembersByCollectivityBetweenDates(String collectivityId, LocalDate from, LocalDate to) {
        String sql = """
            SELECT COUNT(DISTINCT m.id)
            FROM "member" m
            JOIN collectivity_member cm ON m.id = cm.member_id
            WHERE cm.collectivity_id = ?
            AND m.created_at BETWEEN ? AND ?
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, collectivityId);
            ps.setDate(2, java.sql.Date.valueOf(from));
            ps.setDate(3, java.sql.Date.valueOf(to));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    public Integer countMembersUpToDateByCollectivity(String collectivityId, LocalDate to) {
        String sql = """
            SELECT COUNT(DISTINCT cm.member_id)
            FROM collectivity_member cm
            WHERE NOT EXISTS (
                SELECT 1
                FROM membership_fee_installment mfi
                JOIN membership_fee mf ON mfi.membership_fee_id = mf.id
                WHERE mfi.member_id = cm.member_id
                    AND mf.status = 'ACTIVE'
                    AND mfi.due_date <= ?
                    AND NOT EXISTS (
                        SELECT 1
                        FROM member_payment mp
                        WHERE mp.member_id = cm.member_id
                            AND mp.creation_date <= mfi.due_date
                    )
            )
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(to));
            ps.setString(2, collectivityId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    public Integer countTotalMembersByCollectivity(String collectivityId) {
        String sql = """
            SELECT COUNT(DISTINCT member_id)
            FROM collectivity_member
            WHERE collectivity_id = ?
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, collectivityId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }
}