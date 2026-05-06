package hei.school.agriculturalapp.controller;

import hei.school.agriculturalapp.dto.CreateMembershipFee;
import hei.school.agriculturalapp.exception.BadRequestException;
import hei.school.agriculturalapp.exception.NotFoundException;
import hei.school.agriculturalapp.model.MembershipFee;
import hei.school.agriculturalapp.service.MembershipFeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/collectivities/{id}/membershipFees")
@RequiredArgsConstructor
public class MembershipFeeController {

    private final MembershipFeeService membershipFeeService;

    @GetMapping
    public ResponseEntity<?> getMembershipFees(@PathVariable String id) {
        try {
            List<MembershipFee> fees = membershipFeeService.getMembershipFees(id);
            return ResponseEntity.ok(fees);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Database error: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createMembershipFees(@PathVariable String id, @RequestBody List<CreateMembershipFee> requests) {
        try {
            List<MembershipFee> fees = membershipFeeService.createMembershipFees(id, requests);
            return ResponseEntity.status(HttpStatus.CREATED).body(fees);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Database error: " + e.getMessage()));
        }
    }
}