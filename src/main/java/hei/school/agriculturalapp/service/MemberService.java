package hei.school.agriculturalapp.service;

import hei.school.agriculturalapp.exception.BadRequestException;
import hei.school.agriculturalapp.model.Member;
import hei.school.agriculturalapp.repository.MemberRepository;
import hei.school.agriculturalapp.validator.MemberValidator;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberValidator memberValidator;
    private final Connection connection;

    public MemberService(MemberRepository memberRepository,
                         MemberValidator memberValidator,
                         Connection connection) {
        this.memberRepository = memberRepository;
        this.memberValidator = memberValidator;
        this.connection = connection;
    }

    public List<Member> saveAll(List<Member> members) throws BadRequestException, SQLException {
        memberValidator.validate(members);
        connection.setAutoCommit(false);

        for (Member member : members) {
            this.checkBusinessRules(member);
            int id = memberRepository.save(member, connection);
            member.setId(id);
            if (member.getRefereeIds() != null) {
                for (Integer refereeId : member.getRefereeIds()) {
                    memberRepository.saveSponsorship(id, refereeId, connection);
                }
            }
        }
        connection.commit();
        return members;
    }

    private void checkBusinessRules(Member member) throws BadRequestException {
        if (!member.isRegistrationFeePaid() || !member.isMembershipDuesPaid()) {
            throw new BadRequestException("Fees must be paid for: " + member.getLastName());
        }
        if (member.getRefereeIds() == null || member.getRefereeIds().size() < 2) {
            throw new BadRequestException("At least 2 referees required for: " + member.getLastName());
        }
    }
}