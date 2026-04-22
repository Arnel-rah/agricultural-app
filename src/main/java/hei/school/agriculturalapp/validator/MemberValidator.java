package hei.school.agriculturalapp.validator;

import hei.school.agriculturalapp.dto.CreateMember;
import hei.school.agriculturalapp.dto.ValidationResult;
import hei.school.agriculturalapp.repository.MemberRepository;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

@Component
public class MemberValidator {

    private final MemberRepository memberRepository;

    public MemberValidator(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public ValidationResult validate(CreateMember request) throws SQLException {
        ValidationResult result = new ValidationResult();

        if (request == null) {
            result.addError("request", "Request cannot be null");
            return result;
        }

        if (request.getFirstName() == null || request.getFirstName().trim().isEmpty()) {
            result.addError("firstName", "First name is required");
        }
        if (request.getLastName() == null || request.getLastName().trim().isEmpty()) {
            result.addError("lastName", "Last name is required");
        }
        if (request.getBirthDate() == null) {
            result.addError("birthDate", "Birth date is required");
        }
        if (request.getGender() == null || request.getGender().trim().isEmpty()) {
            result.addError("gender", "Gender is required");
        }
        if (request.getPhoneNumber() == null) {
            result.addError("phoneNumber", "Phone number is required");
        }

        if (request.getRegistrationFeePaid() == null || !request.getRegistrationFeePaid()) {
            result.addError("registrationFeePaid", "Registration fee must be paid");
        }
        if (request.getMembershipDuesPaid() == null || !request.getMembershipDuesPaid()) {
            result.addError("membershipDuesPaid", "Membership dues must be paid");
        }

        // Correction : utiliser request.getEmail() au lieu de member.getEmail()
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            if (memberRepository.existsByEmail(request.getEmail())) {
                result.addError("email", "Member with email " + request.getEmail() + " already exists");
            }
        }

        if ("JUNIOR".equals(request.getOccupation())) {
            if (request.getReferees() == null || request.getReferees().size() < 2) {
                result.addError("referees", "At least 2 referees required for junior member");
            } else {
                for (String refereeId : request.getReferees()) {
                    boolean exists = memberRepository.existsById(refereeId);
                    if (!exists) {
                        result.addError("referees", "Referee with id " + refereeId + " does not exist");
                    }
                }
            }
        }

        return result;
    }
}