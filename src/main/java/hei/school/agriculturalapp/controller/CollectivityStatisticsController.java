package hei.school.agriculturalapp.controller;

import hei.school.agriculturalapp.exception.NotFoundException;
import hei.school.agriculturalapp.model.CollectivityLocalStatistics;
import hei.school.agriculturalapp.model.CollectivityOverallStatistics;
import hei.school.agriculturalapp.service.CollectivityStatisticsService;
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
@RequiredArgsConstructor
public class CollectivityStatisticsController {

    private final CollectivityStatisticsService statisticsService;

    @GetMapping("/collectivites/{id}/statistics")
    public ResponseEntity<?> getLocalStatistics(
            @PathVariable String id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        try {
            List<CollectivityLocalStatistics> stats = statisticsService.getLocalStatistics(id, from, to);
            return ResponseEntity.ok(stats);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Database error: " + e.getMessage()));
        }
    }

    @GetMapping("/collectivities/statistics")
    public ResponseEntity<?> getOverallStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        try {
            List<CollectivityOverallStatistics> stats = statisticsService.getOverallStatistics(from, to);
            return ResponseEntity.ok(stats);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Database error: " + e.getMessage()));
        }
    }
}