package hei.school.agriculturalapp.service;

import hei.school.agriculturalapp.dto.CreateMember;
import hei.school.agriculturalapp.dto.ValidationResult;
import hei.school.agriculturalapp.model.Member;
import hei.school.agriculturalapp.repository.MemberRepository;
import hei.school.agriculturalapp.validator.MemberValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberValidator validator;

    public List<Member> createMembers(List<CreateMember> requests) throws SQLException {
        List<Member> createdMembers = new ArrayList<>();
        for (CreateMember request : requests) {
            createdMembers.add(createSingleMember(request));
        }
        return createdMembers;
    }

    private Member createSingleMember(CreateMember request) throws SQLException {
        ValidationResult validation = validator.validate(request);
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Validation failed: " + validation.getErrorMessage());
        }

        Member member = new Member();
        member.setFirstName(request.getFirstName());
        member.setLastName(request.getLastName());
        member.setBirthDate(request.getBirthDate());
        member.setGender(request.getGender());
        member.setAddress(request.getAddress());
        member.setProfession(request.getProfession());
        member.setPhoneNumber(String.valueOf(request.getPhoneNumber()));
        member.setEmail(request.getEmail());
        member.setOccupation(request.getOccupation());

        Member savedMember = memberRepository.save(member);

        if (request.getReferees() != null && !request.getReferees().isEmpty()) {
            List<Member> referees = memberRepository.getMembersByIds(request.getReferees());
            savedMember.setReferees(referees);
        }

        return savedMember;
    }
}