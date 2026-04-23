package hei.school.agriculturalapp.controller;

import hei.school.agriculturalapp.dto.CreateMembershipFee;
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
    public ResponseEntity<?> getMembershipFees(@PathVariable("id") String collectivityId) {
        try {
            List<MembershipFee> fees = membershipFeeService.getMembershipFees(collectivityId);
            return ResponseEntity.ok(fees);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Database error: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createMembershipFees(
            @PathVariable("id") String collectivityId,
            @RequestBody List<CreateMembershipFee> requests) {
        try {
            List<MembershipFee> createdFees = membershipFeeService.createMembershipFees(collectivityId, requests);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdFees);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Collectivity not found: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }
}