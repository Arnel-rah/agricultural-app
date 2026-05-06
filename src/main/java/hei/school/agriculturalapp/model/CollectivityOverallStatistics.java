package hei.school.agriculturalapp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CollectivityOverallStatistics {
    private Collectivity collectivityInformation;
    private Integer newMembersNumber;
    private Double overallMemberCurrentDuePercentage;
}