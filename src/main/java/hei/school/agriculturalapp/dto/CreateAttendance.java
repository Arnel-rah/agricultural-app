package hei.school.agriculturalapp.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAttendance {
    private String memberIdentifier;
    private String attendanceStatus;
}