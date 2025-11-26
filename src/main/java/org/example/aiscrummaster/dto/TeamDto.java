package org.example.aiscrummaster.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.example.aiscrummaster.model.TeamMember;

import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder@JsonIgnoreProperties(ignoreUnknown = true)

public class TeamDto {
    private Long id;
    private String name;

    private List<TeamMember> teamMembers;
}
