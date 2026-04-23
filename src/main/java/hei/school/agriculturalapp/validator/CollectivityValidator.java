package hei.school.agriculturalapp.validator;

import hei.school.agriculturalapp.dto.CreateCollectivity;
import hei.school.agriculturalapp.dto.ValidationResult;
import hei.school.agriculturalapp.repository.CollectivityRepository;
import hei.school.agriculturalapp.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CollectivityValidator {

    private final CollectivityRepository collectivityRepository;
    private final MemberRepository memberRepository;

    public ValidationResult validate(CreateCollectivity request) throws SQLException {
        ValidationResult result = new ValidationResult();

        if (request == null) {
            result.addError("request", "Request cannot be null");
            return result;
        }

        if (request.getLocation() == null || request.getLocation().trim().isEmpty()) {
            result.addError("location", "Location is required");
        }

        List<String> memberIds = request.getMembers();
        if (memberIds == null || memberIds.size() < 10) {
            result.addError("members", "At least 10 members are required");
        } else {
            boolean allMembersExist = collectivityRepository.checkAllMembersExist(memberIds);
            if (!allMembersExist) {
                result.addError("members", "One or more members not found");
            }

            int seniorCount = 0;
            for (String memberId : memberIds) {
                if (memberRepository.isMemberSenior(memberId)) {
                    seniorCount++;
                }
            }
            if (seniorCount < 5) {
                result.addError("members", "At least 5 members must have 6 months seniority");
            }
        }

        if (request.getStructure() == null) {
            result.addError("structure", "Structure is required");
        }

        return result;
    }

    public void validateIdentification(String name, String number) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Unique name is required");
        }
        if (number == null || number.trim().isEmpty()) {
            throw new IllegalArgumentException("Official number is required");
        }
    }
}