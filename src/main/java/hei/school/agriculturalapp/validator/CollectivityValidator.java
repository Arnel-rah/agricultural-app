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
        if (request.getFederationApproval() == null || !request.getFederationApproval()) {
            result.addError("federationApproval", "Collectivity without federation approval");
            return result;
        }
        if (request.getLocation() == null || request.getLocation().trim().isEmpty()) {
            result.addError("location", "Location is required");
        }
        List<String> memberIds = request.getMembers();
        if (memberIds == null || memberIds.isEmpty()) {
            result.addError("members", "At least 10 members are required");
            return result;
        }
        if (memberIds.size() < 10) {
            result.addError("members", "Collectivity must have at least 10 members, got " + memberIds.size());
        }
        if (request.getStructure() == null) {
            result.addError("structure", "Structure is required");
            return result;
        }
        String presidentId = request.getStructure().getPresident();
        String vicePresidentId = request.getStructure().getVicePresident();
        String treasurerId = request.getStructure().getTreasurer();
        String secretaryId = request.getStructure().getSecretary();
        if (presidentId == null || presidentId.trim().isEmpty()) result.addError("structure.president", "President is required");
        if (vicePresidentId == null || vicePresidentId.trim().isEmpty()) result.addError("structure.vicePresident", "Vice president is required");
        if (treasurerId == null || treasurerId.trim().isEmpty()) result.addError("structure.treasurer", "Treasurer is required");
        if (secretaryId == null || secretaryId.trim().isEmpty()) result.addError("structure.secretary", "Secretary is required");
        return result;
    }
}