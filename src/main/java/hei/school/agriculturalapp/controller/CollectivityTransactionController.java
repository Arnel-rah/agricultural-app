package hei.school.agriculturalapp.controller;

import hei.school.agriculturalapp.model.CollectivityTransaction;
import hei.school.agriculturalapp.service.CollectivityTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/collectivities/{id}/transactions")
@RequiredArgsConstructor
public class CollectivityTransactionController {

    private final CollectivityTransactionService transactionService;

    @GetMapping
    public ResponseEntity<?> getTransactions(
            @PathVariable("id") String collectivityId,
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        try {
            List<CollectivityTransaction> transactions = transactionService.getTransactions(collectivityId, from, to);
            return ResponseEntity.ok(transactions);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().equals("Collectivity not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Database error: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }
}