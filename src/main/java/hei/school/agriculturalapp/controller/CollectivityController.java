package hei.school.agriculturalapp.controller;

import hei.school.agriculturalapp.dto.CreateCollectivity;
import hei.school.agriculturalapp.exception.BadRequestException;
import hei.school.agriculturalapp.exception.NotFoundException;
import hei.school.agriculturalapp.model.Collectivity;
import hei.school.agriculturalapp.service.CollectivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/collectivities")
@RequiredArgsConstructor
public class CollectivityController {

    private final CollectivityService collectivityService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getCollectivityById(@PathVariable String id) {
        try {
            Collectivity collectivity = collectivityService.getCollectivityById(id);
            return ResponseEntity.ok(collectivity);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Database error: " + e.getMessage()));
        }
    }
}