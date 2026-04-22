package hei.school.agriculturalapp.service;

import hei.school.agriculturalapp.dto.CreateMembershipFee;
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
        if (!collectivityRepository.existsById(collectivityId)) {
            throw new IllegalArgumentException("Collectivity not found");
        }
        return membershipFeeRepository.findByCollectivityId(collectivityId);
    }

    public List<MembershipFee> createMembershipFees(String collectivityId, List<CreateMembershipFee> requests) throws SQLException {
        if (!collectivityRepository.existsById(collectivityId)) {
            throw new IllegalArgumentException("Collectivity not found");
        }

        for (CreateMembershipFee request : requests) {
            if (request.getAmount() < 0) {
                throw new IllegalArgumentException("Amount must be greater than or equal to 0");
            }
            String frequency = request.getFrequency();
            if (!frequency.equals("WEEKLY") && !frequency.equals("MONTHLY") &&
                    !frequency.equals("ANNUALLY") && !frequency.equals("PUNCTUALLY")) {
                throw new IllegalArgumentException("Invalid frequency: " + frequency);
            }
        }

        return requests.stream()
                .map(request -> {
                    try {
                        return membershipFeeRepository.save(collectivityId, request);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();
    }
}