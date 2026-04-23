package hei.school.agriculturalapp.controller;

import hei.school.agriculturalapp.dto.CreateMember;
import hei.school.agriculturalapp.dto.CreateMemberPayment;
import hei.school.agriculturalapp.model.Member;
import hei.school.agriculturalapp.model.MemberPayment;
import hei.school.agriculturalapp.service.MemberService;
import hei.school.agriculturalapp.service.MemberPaymentService;
import hei.school.agriculturalapp.validator.PaymentValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;
    private final MemberPaymentService paymentService;
    private final PaymentValidator paymentValidator;

    // Injection des trois composants nécessaires
    public MemberController(MemberService memberService,
                            MemberPaymentService paymentService,
                            PaymentValidator paymentValidator) {
        this.memberService = memberService;
        this.paymentService = paymentService;
        this.paymentValidator = paymentValidator;
    }

    /**
     * Create new members
     */
    @PostMapping
    public ResponseEntity<?> createMembers(@RequestBody List<CreateMember> requests) {
        try {
            List<Member> createdMembers = memberService.createMembers(requests);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdMembers);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Database error: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred."));
        }
    }

    @PostMapping("/{memberId}/payments")
    public ResponseEntity<?> createPayments(
            @PathVariable String memberId,
            @RequestBody List<CreateMemberPayment> requests) {
        try {
            paymentValidator.accept(requests);
            List<MemberPayment> createdPayments = paymentService.savePayments(memberId, requests);

            return ResponseEntity.status(HttpStatus.CREATED).body(createdPayments);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Payment processing failed: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}