package hei.school.agriculturalapp.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class CreateCollectivity {
    private String location;
    private List<String> members;
    private Boolean federationApproval;
    private CreateCollectivityStructure structure;
}