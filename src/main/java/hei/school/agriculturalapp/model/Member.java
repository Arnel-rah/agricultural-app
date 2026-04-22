package hei.school.agriculturalapp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    private String gender;
    private String phoneNumber;
    private String profession;
    private String email;
    private String address;
    private String occupation;
    private List<Member> referees;


    @JsonIgnore
    private boolean registrationFeePaid;

    @JsonIgnore
    private boolean membershipDuesPaid;

    @JsonIgnore
    private String admissionStatus;

    @JsonIgnore
    private Integer collectivityId;

    @JsonIgnore
    private List<Integer> refereeIds;

    @JsonIgnore
    private Integer tempId;

}