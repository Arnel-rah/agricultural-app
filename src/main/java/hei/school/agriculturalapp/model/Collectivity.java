package hei.school.agriculturalapp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Collectivity {
    private String id;
    private String name;
    private String location;
    private String agriculturalSpecialty;
    private String registrationNumber;
    private LocalDate creationDate;
    private Boolean federationApproval;
    private String federationId;
    private CollectivityStructure structure;
    private List<Member> members;
}