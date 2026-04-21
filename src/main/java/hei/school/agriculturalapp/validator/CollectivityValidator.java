package hei.school.agriculturalapp.validator;

import hei.school.agriculturalapp.model.CreateCollectivity;
import hei.school.agriculturalapp.repository.CollectivityRepository;
import hei.school.agriculturalapp.repository.MemberRepository;
import lombok.Getter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CollectivityValidator {

    private final CollectivityRepository collectivityRepository;
    private final MemberRepository memberRepository;

    public CollectivityValidator(CollectivityRepository collectivityRepository, MemberRepository memberRepository) {
        this.collectivityRepository = collectivityRepository;
        this.memberRepository = memberRepository;
    }

    public ValidationResult validateCreateCollectivity(CreateCollectivity createCollectivity) throws SQLException {
        ValidationResult result = new ValidationResult();

        // 1. Vérifier l'autorisation de la fédération
        if (createCollectivity.getFederationApproval() == null || !createCollectivity.getFederationApproval()) {
            result.addError("federationApproval", "Collectivity without federation approval");
            return result;
        }

        // 2. Vérifier que les membres existent
        if (createCollectivity.getMembers() == null || createCollectivity.getMembers().isEmpty()) {
            result.addError("members", "At least 10 members are required");
            return result;
        }

        if (createCollectivity.getMembers().size() < 10) {
            result.addError("members", "Collectivity must have at least 10 members");
        }

        boolean allMembersExist = collectivityRepository.checkAllMembersExist(createCollectivity.getMembers());
        if (!allMembersExist) {
            result.addError("members", "One or more members not found");
        }

        // 3. Vérifier que les 5 membres ont une ancienneté d'au moins 6 mois
        int seniorCount = 0;
        for (String memberId : createCollectivity.getMembers()) {
            if (memberRepository.isMemberSenior(Integer.parseInt(memberId))) {
                seniorCount++;
            }
        }

        if (seniorCount < 5) {
            result.addError("members", "At least 5 members must have seniority of 6 months or more");
        }

        // 4. Vérifier que tous les postes spécifiques sont occupés
        if (createCollectivity.getStructure() == null) {
            result.addError("structure", "Collectivity structure is missing");
            return result;
        }

        if (createCollectivity.getStructure().getPresident() == null || createCollectivity.getStructure().getPresident().isEmpty()) {
            result.addError("structure.president", "President is required");
        }

        if (createCollectivity.getStructure().getVicePresident() == null || createCollectivity.getStructure().getVicePresident().isEmpty()) {
            result.addError("structure.vicePresident", "Vice president is required");
        }

        if (createCollectivity.getStructure().getTreasurer() == null || createCollectivity.getStructure().getTreasurer().isEmpty()) {
            result.addError("structure.treasurer", "Treasurer is required");
        }

        if (createCollectivity.getStructure().getSecretary() == null || createCollectivity.getStructure().getSecretary().isEmpty()) {
            result.addError("structure.secretary", "Secretary is required");
        }

        if (createCollectivity.getMembers() != null) {
            List<String> memberIds = createCollectivity.getMembers();

            if (!memberIds.contains(createCollectivity.getStructure().getPresident())) {
                result.addError("structure.president", "President must be among the 10 members");
            }
            if (!memberIds.contains(createCollectivity.getStructure().getVicePresident())) {
                result.addError("structure.vicePresident", "Vice president must be among the 10 members");
            }
            if (!memberIds.contains(createCollectivity.getStructure().getTreasurer())) {
                result.addError("structure.treasurer", "Treasurer must be among the 10 members");
            }
            if (!memberIds.contains(createCollectivity.getStructure().getSecretary())) {
                result.addError("structure.secretary", "Secretary must be among the 10 members");
            }
        }

        return result;
    }

    @Getter
    public static class ValidationResult {
        private final List<String> errors = new ArrayList<>();
        private final Map<String, String> fieldErrors = new HashMap<>();

        public void addError(String field, String message) {
            fieldErrors.put(field, message);
            errors.add(field + ": " + message);
        }

        public boolean isValid() {
            return errors.isEmpty();
        }

        public String getErrorMessage() {
            return String.join("; ", errors);
        }
    }
}