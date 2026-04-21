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
    private String gender;
    private String phoneNumber;
    private boolean registrationFeePaid;
    private boolean membershipDuesPaid;
    private String admissionStatus;
    private Integer collectivityId;
    private List<Integer> refereeIds;


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Member member = (Member) o;
        return registrationFeePaid == member.registrationFeePaid && membershipDuesPaid == member.membershipDuesPaid && Objects.equals(id, member.id) && Objects.equals(firstName, member.firstName) && Objects.equals(lastName, member.lastName) && Objects.equals(birthDate, member.birthDate) && Objects.equals(gender, member.gender) && Objects.equals(phoneNumber, member.phoneNumber) && Objects.equals(admissionStatus, member.admissionStatus) && Objects.equals(collectivityId, member.collectivityId) && Objects.equals(refereeIds, member.refereeIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName, birthDate, gender, phoneNumber, registrationFeePaid, membershipDuesPaid, admissionStatus, collectivityId, refereeIds);
    }


    @Override
    public String toString() {
        return "Member{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", birthDate=" + birthDate +
                ", gender='" + gender + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", registrationFeePaid=" + registrationFeePaid +
                ", membershipDuesPaid=" + membershipDuesPaid +
                ", admissionStatus='" + admissionStatus + '\'' +
                ", collectivityId=" + collectivityId +
                ", refereeIds=" + refereeIds +
                '}';
    }

}


