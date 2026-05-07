package edu.hei.school.agricultural.service;

import edu.hei.school.agricultural.controller.dto.*;
import edu.hei.school.agricultural.entity.Collectivity;
import edu.hei.school.agricultural.entity.Member;
import edu.hei.school.agricultural.exception.BadRequestException;
import edu.hei.school.agricultural.exception.NotFoundException;
import edu.hei.school.agricultural.repository.ActivityMemberAttendanceRepository;
import edu.hei.school.agricultural.repository.CollectivityRepository;
import edu.hei.school.agricultural.repository.FinancialAccountRepository;
import edu.hei.school.agricultural.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.util.UUID.randomUUID;

@Service
@RequiredArgsConstructor
public class CollectivityService {
    private final CollectivityRepository collectivityRepository;
    private final MemberRepository memberRepository;
    private final ActivityMemberAttendanceRepository attendanceRepository;
    private final FinancialAccountRepository financialAccountRepository;

    public List<Collectivity> createCollectivities(List<Collectivity> collectivities) {
        for (Collectivity collectivity : collectivities) {
            if (!collectivity.hasEnoughMembers()) {
                throw new BadRequestException("Collectivity must have at least 10 members, otherwise actual is " + collectivity.getMembers().size());
            }
            collectivity.setId(randomUUID().toString());
        }
        return collectivityRepository.saveAll(collectivities);
    }

    public Collectivity getCollectivityById(String id) {
        return collectivityRepository.findById(id).orElseThrow(() -> new NotFoundException("Collectivity.id= " + id + " not found"));
    }

    public List<FinancialAccountDto> getFinancialAccounts(String collectivityId, LocalDate atDate) {
        Collectivity collectivity = collectivityRepository.findById(collectivityId)
                .orElseThrow(() -> new NotFoundException("Collectivity.id=" + collectivityId + " not found"));

        return financialAccountRepository.findByCollectivityId(collectivityId, atDate);
    }

    public List<CollectivityLocalStatistics> getCollectivityStatistics(String collectivityId, LocalDate from, LocalDate to) {
        Collectivity collectivity = collectivityRepository.findById(collectivityId)
                .orElseThrow(() -> new NotFoundException("Collectivity.id=" + collectivityId + " not found"));

        if (from == null || to == null) {
            throw new BadRequestException("Query parameters 'from' and 'to' are mandatory");
        }

        if (from.isAfter(to)) {
            throw new BadRequestException("'from' date must be before or equal to 'to' date");
        }

        List<CollectivityLocalStatistics> statisticsList = new ArrayList<>();
        List<Member> members = collectivity.getMembers();

        for (Member member : members) {
            Double earnedAmount = memberRepository.getTotalPaymentsByMemberBetweenDates(member.getId(), from, to);
            Double unpaidAmount = memberRepository.getUnpaidAmountByMemberBetweenDates(member.getId(), from, to);
            Double assiduityPercentage = attendanceRepository.getMemberAssiduityPercentage(member.getId(), from, to);

            MemberDescription memberDescription = MemberDescription.builder()
                    .id(member.getId())
                    .firstName(member.getFirstName())
                    .lastName(member.getLastName())
                    .email(member.getEmail())
                    .occupation(member.getOccupation() != null ? member.getOccupation().name() : null)
                    .build();

            CollectivityLocalStatistics statistics = CollectivityLocalStatistics.builder()
                    .memberDescription(memberDescription)
                    .earnedAmount(earnedAmount != null ? earnedAmount : 0.0)
                    .unpaidAmount(unpaidAmount != null ? unpaidAmount : 0.0)
                    .assiduityPercentage(assiduityPercentage != null ? assiduityPercentage : 100.0)
                    .build();

            statisticsList.add(statistics);
        }

        return statisticsList;
    }

    public List<CollectivityOverallStatistics> getAllCollectivitiesStatistics(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new BadRequestException("Query parameters 'from' and 'to' are mandatory");
        }
        if (from.isAfter(to)) {
            throw new BadRequestException("'from' date must be before or equal to 'to' date");
        }

        return collectivityRepository.findAllCollectivitiesStatistics(from, to);
    }
}