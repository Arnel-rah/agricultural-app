package hei.school.agriculturalapp.service;

import hei.school.agriculturalapp.exception.NotFoundException;
import hei.school.agriculturalapp.model.Collectivity;
import hei.school.agriculturalapp.model.CollectivityLocalStatistics;
import hei.school.agriculturalapp.model.CollectivityOverallStatistics;
import hei.school.agriculturalapp.model.MemberDescription;
import hei.school.agriculturalapp.repository.CollectivityRepository;
import hei.school.agriculturalapp.repository.StatisticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CollectivityStatisticsService {

    private final StatisticsRepository statisticsRepository;
    private final CollectivityRepository collectivityRepository;

    public List<CollectivityLocalStatistics> getLocalStatistics(String collectivityId, LocalDate from, LocalDate to) throws SQLException {
        if (!collectivityRepository.existsById(collectivityId)) throw new NotFoundException("Collectivity not found");
        List<MemberDescription> members = statisticsRepository.getMemberDescriptionsByCollectivityId(collectivityId);
        List<CollectivityLocalStatistics> stats = new ArrayList<>();
        for (MemberDescription member : members) {
            CollectivityLocalStatistics stat = new CollectivityLocalStatistics();
            stat.setMemberDescription(member);
            stat.setEarnedAmount(statisticsRepository.getMemberEarnedAmount(member.getId(), from, to));
            stat.setUnpaidAmount(statisticsRepository.getMemberUnpaidAmount(member.getId()));
            stats.add(stat);
        }
        return stats;
    }

    public List<CollectivityOverallStatistics> getOverallStatistics(LocalDate from, LocalDate to) throws SQLException {
        List<Collectivity> collectivities = collectivityRepository.findAll();
        List<CollectivityOverallStatistics> stats = new ArrayList<>();
        for (Collectivity col : collectivities) {
            CollectivityOverallStatistics stat = new CollectivityOverallStatistics();
            stat.setCollectivityInformation(col);
            stat.setNewMembersNumber(statisticsRepository.getNewMembersCount(col.getId(), from, to));
            stat.setOverallMemberCurrentDuePercentage(statisticsRepository.getMemberCurrentDuePercentage(col.getId()));
            stats.add(stat);
        }
        return stats;
    }
}