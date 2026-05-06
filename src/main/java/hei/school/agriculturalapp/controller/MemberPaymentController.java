package hei.school.agriculturalapp.controller;

import hei.school.agriculturalapp.dto.CreateMemberPayment;
import hei.school.agriculturalapp.exception.BadRequestException;
import hei.school.agriculturalapp.model.MemberPayment;
import hei.school.agriculturalapp.service.MemberPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/members/{id}/payments")
@RequiredArgsConstructor
public class MemberPaymentController {

    private final MemberPaymentService memberPaymentService;

    @PostMapping
    public ResponseEntity<?> createPayments(
            @PathVariable("id") String memberId,
            @RequestBody List<CreateMemberPayment> requests) {
        try {
            List<MemberPayment> payments = memberPaymentService.savePayments(memberId, requests);
            return ResponseEntity.status(HttpStatus.CREATED).body(payments);
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Database error: " + e.getMessage()));
        }
    }
}