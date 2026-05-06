package hei.school.agriculturalapp.service;

import hei.school.agriculturalapp.dto.CreateAttendance;
import hei.school.agriculturalapp.exception.BadRequestException;
import hei.school.agriculturalapp.exception.NotFoundException;
import hei.school.agriculturalapp.model.Activity;
import hei.school.agriculturalapp.model.Attendance;
import hei.school.agriculturalapp.repository.AttendanceRepository;
import hei.school.agriculturalapp.repository.CollectivityRepository;
import hei.school.agriculturalapp.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final ActivityService activityService;
    private final CollectivityRepository collectivityRepository;
    private final MemberRepository memberRepository;

    public List<Attendance> createAttendances(String collectivityId, String activityId, List<CreateAttendance> requests) throws SQLException {
        if (!collectivityRepository.existsById(collectivityId)) {
            throw new NotFoundException("Collectivity not found");
        }

        Activity activity = activityService.getActivityById(activityId);
        if (activity == null) {
            throw new NotFoundException("Activity not found");
        }

        for (CreateAttendance req : requests) {
            boolean hasExisting = attendanceRepository.hasExistingAttendance(activityId, req.getMemberIdentifier());
            if (hasExisting) {
                throw new BadRequestException("Attendance already confirmed for member: " + req.getMemberIdentifier());
            }

            if (!memberRepository.existsById(req.getMemberIdentifier())) {
                throw new BadRequestException("Member not found: " + req.getMemberIdentifier());
            }
        }

        attendanceRepository.saveAttendance(activityId, requests);
        return attendanceRepository.findByActivityId(activityId);
    }

    public List<Attendance> getAttendances(String collectivityId, String activityId) throws SQLException {
        if (!collectivityRepository.existsById(collectivityId)) {
            throw new NotFoundException("Collectivity not found");
        }

        Activity activity = activityService.getActivityById(activityId);
        if (activity == null) {
            throw new NotFoundException("Activity not found");
        }

        return attendanceRepository.findByActivityId(activityId);
    }

    public void initializeActivityAttendance(String collectivityId, String activityId) throws SQLException {
        if (!collectivityRepository.existsById(collectivityId)) {
            throw new NotFoundException("Collectivity not found");
        }

        Activity activity = activityService.getActivityById(activityId);
        if (activity == null) {
            throw new NotFoundException("Activity not found");
        }

        List<String> concernedMembers = memberRepository.getAllMembers().stream()
                .filter(m -> isMemberConcerned(m.getOccupation(), activity.getMemberOccupationConcerned()))
                .map(m -> m.getId())
                .collect(Collectors.toList());

        if (!concernedMembers.isEmpty()) {
            attendanceRepository.initializeUndefinedAttendance(activityId, activity, concernedMembers);
        }
    }

    private boolean isMemberConcerned(String memberOccupation, List<String> concernedOccupations) {
        if (concernedOccupations == null || concernedOccupations.isEmpty()) return true;
        return concernedOccupations.contains(memberOccupation);
    }
}