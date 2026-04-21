package hei.school.agriculturalapp.service;

import hei.school.agriculturalapp.model.Collectivity;
import hei.school.agriculturalapp.model.CollectivityStructure;
import hei.school.agriculturalapp.model.Member;
import hei.school.agriculturalapp.repository.CollectivityRepository;
import hei.school.agriculturalapp.repository.MemberRepository;
import hei.school.agriculturalapp.validator.CollectivityValidator;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CollectivityService {

    private final CollectivityRepository collectivityRepository;
    private final MemberRepository memberRepository;
    private final CollectivityValidator validator;

    public CollectivityService(CollectivityRepository collectivityRepository, MemberRepository memberRepository) {
        this.collectivityRepository = collectivityRepository;
        this.memberRepository = memberRepository;
        this.validator = new CollectivityValidator(collectivityRepository, memberRepository);
    }

    public List<Collectivity> createCollectivities(
            List<String> locations,
            List<List<String>> allMemberIds,
            List<Boolean> federationApprovals,
            List<String> presidentIds,
            List<String> vicePresidentIds,
            List<String> treasurerIds,
            List<String> secretaryIds
    ) throws SQLException {

        List<Collectivity> createdCollectivities = new ArrayList<>();

        for (int i = 0; i < locations.size(); i++) {
            Collectivity collectivity = createSingleCollectivity(
                    locations.get(i),
                    allMemberIds.get(i),
                    federationApprovals.get(i),
                    presidentIds.get(i),
                    vicePresidentIds.get(i),
                    treasurerIds.get(i),
                    secretaryIds.get(i)
            );
            createdCollectivities.add(collectivity);
        }

        return createdCollectivities;
    }

    private Collectivity createSingleCollectivity(
            String location,
            List<String> memberIds,
            Boolean federationApproval,
            String presidentId,
            String vicePresidentId,
            String treasurerId,
            String secretaryId
    ) throws SQLException {

        CollectivityValidator.ValidationResult validation = validator.validateCreateCollectivity(
                location, memberIds, federationApproval, presidentId, vicePresidentId, treasurerId, secretaryId
        );

        if (!validation.isValid()) {
            throw new IllegalArgumentException("Validation failed: " + validation.getErrorMessage());
        }

        Collectivity collectivity = new Collectivity();
        collectivity.setLocation(location);
        Collectivity savedCollectivity = collectivityRepository.save(collectivity);

        collectivityRepository.addMembersToCollectivity(savedCollectivity.getId(), memberIds);

        int currentMandateId = collectivityRepository.getCurrentMandateId();

        collectivityRepository.assignRolesToCollectivity(
                savedCollectivity.getId(),
                currentMandateId,
                presidentId,
                vicePresidentId,
                treasurerId,
                secretaryId
        );

        List<Member> members = collectivityRepository.getMembersByCollectivityId(savedCollectivity.getId());

        for (Member member : members) {
            List<Integer> refereeIds = memberRepository.getRefereeIdsByMemberId(member.getId());
            List<Member> referees = new ArrayList<>();
            for (Integer refereeId : refereeIds) {
                Member referee = memberRepository.findById(refereeId).orElse(null);
                if (referee != null) {
                    referees.add(referee);
                }
            }
            member.setReferees(referees);
        }

        CollectivityStructure structure = collectivityRepository.getCollectivityStructure(
                savedCollectivity.getId(),
                currentMandateId
        );

        Collectivity responseCollectivity = new Collectivity();
        responseCollectivity.setId(savedCollectivity.getId());
        responseCollectivity.setLocation(savedCollectivity.getLocation());
        responseCollectivity.setStructure(structure);
        responseCollectivity.setMembers(members);

        return responseCollectivity;
    }
}