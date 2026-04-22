package hei.school.agriculturalapp.service;

import hei.school.agriculturalapp.dto.CreateCollectivity;
import hei.school.agriculturalapp.dto.ValidationResult;
import hei.school.agriculturalapp.model.Collectivity;
import hei.school.agriculturalapp.model.CollectivityStructure;
import hei.school.agriculturalapp.model.Member;
import hei.school.agriculturalapp.repository.CollectivityRepository;
import hei.school.agriculturalapp.repository.MemberRepository;
import hei.school.agriculturalapp.validator.CollectivityValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CollectivityService {
    private final CollectivityRepository collectivityRepository;
    private final MemberRepository memberRepository;
    private final CollectivityValidator validator;

    public List<Collectivity> createCollectivities(List<CreateCollectivity> requests) throws SQLException {
        List<Collectivity> createdCollectivities = new ArrayList<>();
        for (CreateCollectivity request : requests) {
            createdCollectivities.add(createSingleCollectivity(request));
        }
        return createdCollectivities;
    }

    private Collectivity createSingleCollectivity(CreateCollectivity request) throws SQLException {
        ValidationResult validation = validator.validate(request);
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

        CollectivityStructure structure = collectivityRepository.getCollectivityStructure(savedCollectivity.getId(), currentMandateId);

        Collectivity responseCollectivity = new Collectivity();
        responseCollectivity.setId(savedCollectivity.getId());
        responseCollectivity.setLocation(savedCollectivity.getLocation());
        responseCollectivity.setStructure(structure);
        responseCollectivity.setMembers(members);

        return responseCollectivity;
    }
}