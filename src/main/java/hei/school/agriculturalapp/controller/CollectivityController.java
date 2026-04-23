package hei.school.agriculturalapp.controller;

import hei.school.agriculturalapp.dto.CreateCollectivity;
import hei.school.agriculturalapp.model.Collectivity;
import hei.school.agriculturalapp.model.FinancialAccount;
import hei.school.agriculturalapp.service.CollectivityService;
import hei.school.agriculturalapp.service.FinancialAccountService;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/collectivities")
@AllArgsConstructor
public class CollectivityController {

    private final CollectivityService collectivityService;
    private final FinancialAccountService financialAccountService;

    @GetMapping("/{id}/financialAccounts")
    public ResponseEntity<?> getFinancialAccounts(
            @PathVariable String id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate at) {
        try {
            List<FinancialAccount> accounts = financialAccountService.getAccountsByCollectivity(id, at);
            if (accounts.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Collectivity not found or has no accounts.");
            }
            return ResponseEntity.ok(accounts);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createCollectivities(@RequestBody List<CreateCollectivity> requests) {
        try {
            List<Collectivity> created = collectivityService.createCollectivities(requests);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/identification")
    public ResponseEntity<?> identify(@PathVariable String id, @RequestBody Collectivity request) {
        try {
            Collectivity updated = collectivityService.assignOfficialIdentification(
                    id, request.getUniqueName(), request.getOfficialNumber());
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> getCollectivityById(@PathVariable String id) {
        return collectivityService.getDetailedById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}