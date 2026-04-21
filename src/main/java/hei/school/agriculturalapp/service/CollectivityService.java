package hei.school.agriculturalapp.service;

import hei.school.agriculturalapp.dto.CreateCollectivity;
import hei.school.agriculturalapp.dto.ValidationResult;
import hei.school.agriculturalapp.model.Collectivity;
import hei.school.agriculturalapp.model.CollectivityStructure;
import hei.school.agriculturalapp.model.Member;
import hei.school.agriculturalapp.repository.CollectivityRepository;
import hei.school.agriculturalapp.repository.MemberRepository;
import hei.school.agriculturalapp.validator.CollectivityValidator;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
public class CollectivityService {

    private final CollectivityRepository collectivityRepository;
    private final MemberRepository memberRepository;
    private final CollectivityValidator validator;

    public CollectivityService(CollectivityRepository collectivityRepository, MemberRepository memberRepository) {
        this.collectivityRepository = collectivityRepository;
        this.memberRepository = memberRepository;
        this.validator = new CollectivityValidator(collectivityRepository, memberRepository);
    }

    public List<Collectivity> createCollectivities(List<CreateCollectivity> requests) throws SQLException {
        List<Collectivity> createdCollectivities = new ArrayList<>();

        for (CreateCollectivity request : requests) {
            Collectivity collectivity = createSingleCollectivity(request);
            createdCollectivities.add(collectivity);
        }

        return createdCollectivities;
    }

    private Collectivity createSingleCollectivity(CreateCollectivity request) throws SQLException {

        ValidationResult validation = validator.validateCreateCollectivity(request);

        if (!validation.isValid()) {
            throw new IllegalArgumentException("Validation failed: " + validation.getErrorMessage());
        }

        Collectivity collectivity = new Collectivity();
        collectivity.setLocation(request.getLocation());
        Collectivity savedCollectivity = collectivityRepository.save(collectivity);

        collectivityRepository.addMembersToCollectivity(savedCollectivity.getId(), request.getMembers());

        int currentMandateId = collectivityRepository.getCurrentMandateId();

        collectivityRepository.assignRolesToCollectivity(
                savedCollectivity.getId(),
                currentMandateId,
                request.getStructure().getPresident(),
                request.getStructure().getVicePresident(),
                request.getStructure().getTreasurer(),
                request.getStructure().getSecretary()
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