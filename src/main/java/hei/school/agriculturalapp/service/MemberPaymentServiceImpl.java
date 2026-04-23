package hei.school.agriculturalapp.service;

import hei.school.agriculturalapp.dto.CreateMemberPayment;
import hei.school.agriculturalapp.exception.BadRequestException;
import hei.school.agriculturalapp.model.MemberPayment;
import hei.school.agriculturalapp.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberPaymentServiceImpl implements MemberPaymentService {

    private final List<AccountStrategy> strategies;
    private final PaymentRepository paymentRepository;

    @Override
    public List<MemberPayment> savePayments(String memberId, List<CreateMemberPayment> requests) throws SQLException, BadRequestException {
        List<MemberPayment> results = new ArrayList<>();

        for (CreateMemberPayment req : requests) {
            AccountStrategy strategy = strategies.stream()
                    .filter(s -> s.supports(req.getPaymentMode()))
                    .findFirst()
                    .orElseThrow(() -> new BadRequestException("Payment mode not supported: " + req.getPaymentMode()));

            strategy.credit(req.getAccountCreditedIdentifier(), req.getAmount());

            MemberPayment saved = paymentRepository.saveMemberPayment(memberId, req);
            results.add(saved);
        }
        return results;
    }
}