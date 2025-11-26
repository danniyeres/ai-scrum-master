package org.example.aiscrummaster.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.example.aiscrummaster.model.Team;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder@JsonIgnoreProperties(ignoreUnknown = true)

public class TeamMemberDto {

    private Long id;

    private String fullName;
    private String email;
    private String telegramId;
    private String role;

    @JsonIgnore
    private Team team;
}
