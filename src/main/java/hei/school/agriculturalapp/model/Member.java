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

    private String address;
    private String profession;
    private String email;
    private List<Member> referees;
    private String occupation;

    public Member(String firstName, String lastName, LocalDate birthDate, String gender, String phoneNumber, boolean registrationFeePaid, boolean membershipDuesPaid, Integer collectivityId, List<Integer> refereeIds) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.registrationFeePaid = registrationFeePaid;
        this.membershipDuesPaid = membershipDuesPaid;
        this.collectivityId = collectivityId;
        this.refereeIds = refereeIds;
        this.admissionStatus = "PENDING";
    }

    public Member(Integer id, String firstName, String lastName, LocalDate birthDate, String gender, String address, String profession, String phoneNumber, String email, String occupation, List<Member> referees) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.gender = gender;
        this.address = address;
        this.profession = profession;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.occupation = occupation;
        this.referees = referees;
    }

    public Member(Integer id, String firstName, String lastName, LocalDate birthDate, String gender, String address, String profession, String phoneNumber, String email, String occupation) {
        this(id, firstName, lastName, birthDate, gender, address, profession, phoneNumber, email, occupation, null);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Member member = (Member) o;
        return registrationFeePaid == member.registrationFeePaid && membershipDuesPaid == member.membershipDuesPaid && Objects.equals(id, member.id) && Objects.equals(firstName, member.firstName) && Objects.equals(lastName, member.lastName) && Objects.equals(birthDate, member.birthDate) && Objects.equals(gender, member.gender) && Objects.equals(phoneNumber, member.phoneNumber) && Objects.equals(admissionStatus, member.admissionStatus) && Objects.equals(collectivityId, member.collectivityId) && Objects.equals(refereeIds, member.refereeIds) && Objects.equals(address, member.address) && Objects.equals(profession, member.profession) && Objects.equals(email, member.email) && Objects.equals(occupation, member.occupation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName, birthDate, gender, phoneNumber, registrationFeePaid, membershipDuesPaid, admissionStatus, collectivityId, refereeIds, address, profession, email, occupation);
    }

    @Override
    public String toString() {
        return "Member{" + "id=" + id + ", firstName='" + firstName + '\'' + ", lastName='" + lastName + '\'' + ", birthDate=" + birthDate + ", gender='" + gender + '\'' + ", phoneNumber='" + phoneNumber + '\'' + ", registrationFeePaid=" + registrationFeePaid + ", membershipDuesPaid=" + membershipDuesPaid + ", admissionStatus='" + admissionStatus + '\'' + ", collectivityId=" + collectivityId + ", refereeIds=" + refereeIds + ", address='" + address + '\'' + ", profession='" + profession + '\'' + ", email='" + email + '\'' + ", occupation='" + occupation + '\'' + '}';
    }
}