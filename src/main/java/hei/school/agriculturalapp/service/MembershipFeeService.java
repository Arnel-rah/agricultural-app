package hei.school.agriculturalapp.service;

import hei.school.agriculturalapp.dto.CreateMembershipFee;
import hei.school.agriculturalapp.exception.BadRequestException;
import hei.school.agriculturalapp.exception.NotFoundException;
import hei.school.agriculturalapp.model.MembershipFee;
import hei.school.agriculturalapp.repository.CollectivityRepository;
import hei.school.agriculturalapp.repository.MembershipFeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MembershipFeeService {

    private final MembershipFeeRepository membershipFeeRepository;
    private final CollectivityRepository collectivityRepository;

    public List<MembershipFee> getMembershipFees(String collectivityId) throws SQLException {
        if (!collectivityRepository.existsById(collectivityId)) throw new NotFoundException("Collectivity not found");
        return membershipFeeRepository.findByCollectivityId(collectivityId);
    }

    public List<MembershipFee> createMembershipFees(String collectivityId, List<CreateMembershipFee> requests) throws SQLException {
        if (!collectivityRepository.existsById(collectivityId)) throw new NotFoundException("Collectivity not found");
        for (CreateMembershipFee req : requests) {
            if (req.getAmount() < 0) throw new BadRequestException("Amount must be greater than or equal to 0");
            String freq = req.getFrequency();
            if (!freq.equals("WEEKLY") && !freq.equals("MONTHLY") && !freq.equals("ANNUALLY") && !freq.equals("PUNCTUALLY")) {
                throw new BadRequestException("Invalid frequency: " + freq);
            }
        }
        return membershipFeeRepository.saveAll(collectivityId, requests);
    }
}