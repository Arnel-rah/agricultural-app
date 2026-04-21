package hei.school.agriculturalapp.validator;

import hei.school.agriculturalapp.exception.BadRequestException;
import hei.school.agriculturalapp.model.Member;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MemberValidator {
    public void validate(List<Member> members) throws BadRequestException {
        if (members == null || members.isEmpty()) {
            throw new BadRequestException("The member list cannot be empty.");
        }

        for (Member m : members) {
            if (m.getFirstName() == null || m.getFirstName().isBlank()) {
                throw new BadRequestException("First name is mandatory for all members.");
            }
            if (m.getLastName() == null || m.getLastName().isBlank()) {
                throw new BadRequestException("Last name is mandatory for all members.");
            }
            if (m.getBirthDate() == null) {
                throw new BadRequestException("Birth date is required for: " + m.getLastName());
            }
            if (m.getGender() == null || m.getGender().isBlank()) {
                throw new BadRequestException("Gender is required.");
            }
        }
    }
}
