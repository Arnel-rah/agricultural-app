package hei.school.agriculturalapp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class Member {
    private Integer id;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private Integer tempId;
    private String gender;
    private String phoneNumber;
    private boolean registrationFeePaid;
    private boolean membershipDuesPaid;
    private String admissionStatus;
    private Integer collectivityId;
    private List<Integer> refereeIds;
    private String address;
    private String profession;
    private String email;
    private List<Member> referees;
    private String occupation;
}