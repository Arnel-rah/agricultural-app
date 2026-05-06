package hei.school.agriculturalapp.service;

import hei.school.agriculturalapp.dto.CreateMember;
import hei.school.agriculturalapp.dto.ValidationResult;
import hei.school.agriculturalapp.exception.BadRequestException;
import hei.school.agriculturalapp.model.Member;
import hei.school.agriculturalapp.repository.MemberRepository;
import hei.school.agriculturalapp.validator.MemberValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberValidator validator;

    public List<Member> createMembers(List<CreateMember> requests) throws SQLException {
        List<Member> created = new ArrayList<>();
        for (CreateMember req : requests) {
            created.add(createSingleMember(req));
        }
        return created;
    }

    private Member createSingleMember(CreateMember req) throws SQLException {
        ValidationResult validation = validator.validate(req);
        if (!validation.isValid()) throw new BadRequestException(validation.getErrorMessage());
        Member member = new Member();
        member.setFirstName(req.getFirstName());
        member.setLastName(req.getLastName());
        member.setBirthDate(req.getBirthDate());
        member.setGender(req.getGender());
        member.setAddress(req.getAddress());
        member.setProfession(req.getProfession());
        member.setPhoneNumber(String.valueOf(req.getPhoneNumber()));
        member.setEmail(req.getEmail());
        member.setOccupation(req.getOccupation());
        member.setJoinDate(LocalDate.now());
        member.setRegistrationFeePaid(req.getRegistrationFeePaid());
        member.setMembershipDuesPaid(req.getMembershipDuesPaid());
        member.setAdmissionStatus("APPROVED");
        Member saved = memberRepository.save(member);
        if (req.getReferees() != null && !req.getReferees().isEmpty()) {
            List<Member> referees = memberRepository.getMembersByIds(req.getReferees());
            saved.setReferees(referees);
        } else {
            saved.setReferees(new ArrayList<>());
        }
        return saved;
    }
}