package edu.hei.school.agricultural.controller;

import edu.hei.school.agricultural.controller.dto.CollectivityActivity;
import edu.hei.school.agricultural.controller.dto.CreateCollectivityActivity;
import edu.hei.school.agricultural.exception.BadRequestException;
import edu.hei.school.agricultural.exception.NotFoundException;
import edu.hei.school.agricultural.security.ApiKeyValidator;
import edu.hei.school.agricultural.service.ActivityService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequiredArgsConstructor
public class ActivityController {
    private final ActivityService activityService;
    private final ApiKeyValidator apiKeyValidator;

    @GetMapping("/collectivities/{id}/activities")
    public ResponseEntity<?> getActivities(@PathVariable String id, HttpServletRequest request) {
        try {
            return ResponseEntity.status(OK).body(activityService.getActivities(id));
        } catch (NotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/collectivities/{id}/activities")
    public ResponseEntity<?> addActivities(@PathVariable String id,
                                           @RequestBody List<CreateCollectivityActivity> activities,
                                           HttpServletRequest request) {
        try {
            return ResponseEntity.status(OK).body(activityService.addActivities(id, activities));
        } catch (BadRequestException e) {
            return ResponseEntity.status(BAD_REQUEST).body(e.getMessage());
        } catch (NotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}