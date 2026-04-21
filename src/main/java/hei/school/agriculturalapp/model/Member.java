package hei.school.agriculturalapp.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

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

    public Member() {}

    public Member(Integer id, String firstName, String lastName, LocalDate birthDate, String gender, String phoneNumber, boolean registrationFeePaid, boolean membershipDuesPaid, String admissionStatus, Integer collectivityId, List<Integer> refereeIds) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.registrationFeePaid = registrationFeePaid;
        this.membershipDuesPaid = membershipDuesPaid;
        this.admissionStatus = admissionStatus;
        this.collectivityId = collectivityId;
        this.refereeIds = refereeIds;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public boolean isRegistrationFeePaid() { return registrationFeePaid; }
    public void setRegistrationFeePaid(boolean registrationFeePaid) { this.registrationFeePaid = registrationFeePaid; }
    public boolean isMembershipDuesPaid() { return membershipDuesPaid; }
    public void setMembershipDuesPaid(boolean membershipDuesPaid) { this.membershipDuesPaid = membershipDuesPaid; }
    public String getAdmissionStatus() { return admissionStatus; }
    public void setAdmissionStatus(String admissionStatus) { this.admissionStatus = admissionStatus; }
    public Integer getCollectivityId() { return collectivityId; }
    public void setCollectivityId(Integer collectivityId) { this.collectivityId = collectivityId; }
    public List<Integer> getRefereeIds() { return refereeIds; }
    public void setRefereeIds(List<Integer> refereeIds) { this.refereeIds = refereeIds; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
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
        return "Member{id=" + id + ", name='" + firstName + " " + lastName + "'}";
    }
}