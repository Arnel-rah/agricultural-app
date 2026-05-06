package hei.school.agriculturalapp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Attendance {
    private String id;
    private String activityId;
    private String memberId;
    private String attendanceStatus;
    private String memberFirstName;
    private String memberLastName;
    private String memberEmail;
    private String memberOccupation;
}