package hei.school.agriculturalapp.service;

import hei.school.agriculturalapp.dto.CreateMemberPayment;
import hei.school.agriculturalapp.exception.BadRequestException;
import hei.school.agriculturalapp.exception.NotFoundException;
import hei.school.agriculturalapp.model.MemberPayment;
import hei.school.agriculturalapp.repository.MemberRepository;
import hei.school.agriculturalapp.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final MemberRepository memberRepository;

    public List<MemberPayment> createPayments(String memberId, List<CreateMemberPayment> requests) throws SQLException {
        if (!memberRepository.existsById(memberId)) throw new NotFoundException("Member not found");
        List<MemberPayment> payments = new ArrayList<>();
        for (CreateMemberPayment req : requests) {
            if (req.getAmount() == null || req.getAmount() <= 0) throw new BadRequestException("Amount must be greater than 0");
            if (req.getMembershipFeeIdentifier() == null || req.getMembershipFeeIdentifier().trim().isEmpty()) {
                throw new BadRequestException("Membership fee identifier is required");
            }
            if (req.getAccountCreditedIdentifier() == null || req.getAccountCreditedIdentifier().trim().isEmpty()) {
                throw new BadRequestException("Account credited identifier is required");
            }
            payments.add(paymentRepository.saveMemberPayment(memberId, req));
        }
        return payments;
    }
}