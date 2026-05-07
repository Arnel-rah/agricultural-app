package edu.hei.school.agricultural.service;

import edu.hei.school.agricultural.controller.dto.ActivityMemberAttendance;
import edu.hei.school.agricultural.controller.dto.CreateActivityMemberAttendance;
import edu.hei.school.agricultural.controller.dto.MemberDescription;
import edu.hei.school.agricultural.entity.Activity;
import edu.hei.school.agricultural.entity.Collectivity;
import edu.hei.school.agricultural.entity.Member;
import edu.hei.school.agricultural.exception.BadRequestException;
import edu.hei.school.agricultural.exception.NotFoundException;
import edu.hei.school.agricultural.repository.ActivityMemberAttendanceRepository;
import edu.hei.school.agricultural.repository.ActivityRepository;
import edu.hei.school.agricultural.repository.CollectivityRepository;
import edu.hei.school.agricultural.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static java.util.UUID.randomUUID;

@Service
@RequiredArgsConstructor
public class AttendanceService {
    private final ActivityRepository activityRepository;
    private final CollectivityRepository collectivityRepository;
    private final MemberRepository memberRepository;
    private final ActivityMemberAttendanceRepository attendanceRepository;

    public List<ActivityMemberAttendance> addAttendance(String collectivityId, String activityId, List<CreateActivityMemberAttendance> attendances) {
        Collectivity collectivity = collectivityRepository.findById(collectivityId)
                .orElseThrow(() -> new NotFoundException("Collectivity.id=" + collectivityId + " not found"));

        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new NotFoundException("Activity.id=" + activityId + " not found"));

        List<ActivityMemberAttendance> result = new ArrayList<>();

        for (CreateActivityMemberAttendance attendance : attendances) {
            Member member = memberRepository.findById(attendance.getMemberIdentifier())
                    .orElseThrow(() -> new NotFoundException("Member.id=" + attendance.getMemberIdentifier() + " not found"));

            String status = attendance.getAttendanceStatus();
            if (!status.equals("ATTENDED") && !status.equals("MISSING") && !status.equals("UNDEFINED")) {
                throw new BadRequestException("Invalid attendance status: " + status);
            }

            attendanceRepository.saveAttendance(activityId, member.getId(), status);

            MemberDescription memberDesc = MemberDescription.builder()
                    .id(member.getId())
                    .firstName(member.getFirstName())
                    .lastName(member.getLastName())
                    .email(member.getEmail())
                    .occupation(member.getOccupation() != null ? member.getOccupation().name() : null)
                    .build();

            ActivityMemberAttendance resultAttendance = ActivityMemberAttendance.builder()
                    .id(randomUUID().toString())
                    .memberDescription(memberDesc)
                    .attendanceStatus(status)
                    .build();

            result.add(resultAttendance);
        }

        return result;
    }

    public List<ActivityMemberAttendance> getAttendance(String collectivityId, String activityId) {
        Collectivity collectivity = collectivityRepository.findById(collectivityId)
                .orElseThrow(() -> new NotFoundException("Collectivity.id=" + collectivityId + " not found"));

        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new NotFoundException("Activity.id=" + activityId + " not found"));

        List<Member> attendedMembers = attendanceRepository.findAttendanceByActivityIdAndStatus(activityId, "ATTENDED");
        List<ActivityMemberAttendance> result = new ArrayList<>();

        for (Member member : attendedMembers) {
            MemberDescription memberDesc = MemberDescription.builder()
                    .id(member.getId())
                    .firstName(member.getFirstName())
                    .lastName(member.getLastName())
                    .email(member.getEmail())
                    .occupation(member.getOccupation() != null ? member.getOccupation().name() : null)
                    .build();

            ActivityMemberAttendance attendance = ActivityMemberAttendance.builder()
                    .id(randomUUID().toString())
                    .memberDescription(memberDesc)
                    .attendanceStatus("ATTENDED")
                    .build();

            result.add(attendance);
        }

        return result;
    }
}