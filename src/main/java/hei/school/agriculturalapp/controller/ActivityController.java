package hei.school.agriculturalapp.controller;

import hei.school.agriculturalapp.dto.CreateActivity;
import hei.school.agriculturalapp.dto.CreateAttendance;
import hei.school.agriculturalapp.exception.BadRequestException;
import hei.school.agriculturalapp.exception.NotFoundException;
import hei.school.agriculturalapp.model.Activity;
import hei.school.agriculturalapp.model.Attendance;
import hei.school.agriculturalapp.service.ActivityService;
import hei.school.agriculturalapp.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/collectivities/{id}/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;
    private final AttendanceService attendanceService;

    @PostMapping
    public ResponseEntity<?> createActivities(@PathVariable("id") String collectivityId, @RequestBody List<CreateActivity> requests) {
        try {
            List<Activity> activities = activityService.createActivities(collectivityId, requests);
            return ResponseEntity.status(HttpStatus.CREATED).body(activities);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Database error: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getActivities(@PathVariable("id") String collectivityId) {
        try {
            List<Activity> activities = activityService.getActivities(collectivityId);
            return ResponseEntity.ok(activities);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Database error: " + e.getMessage()));
        }
    }

    @GetMapping("/{activityId}/attendance")
    public ResponseEntity<?> getAttendance(@PathVariable("id") String collectivityId, @PathVariable("activityId") String activityId) {
        try {
            List<Attendance> attendances = attendanceService.getAttendances(collectivityId, activityId);
            return ResponseEntity.ok(attendances);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Database error: " + e.getMessage()));
        }
    }

    @PostMapping("/{activityId}/attendance")
    public ResponseEntity<?> createAttendance(@PathVariable("id") String collectivityId, @PathVariable("activityId") String activityId, @RequestBody List<CreateAttendance> requests) {
        try {
            List<Attendance> attendances = attendanceService.createAttendances(collectivityId, activityId, requests);
            return ResponseEntity.status(HttpStatus.CREATED).body(attendances);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Database error: " + e.getMessage()));
        }
    }
}